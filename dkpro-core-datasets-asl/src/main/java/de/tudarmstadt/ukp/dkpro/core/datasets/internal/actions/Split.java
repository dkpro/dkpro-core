/*
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.datasets.internal.actions;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.AntPathMatcher;

import de.tudarmstadt.ukp.dkpro.core.datasets.ActionDescription;
import de.tudarmstadt.ukp.dkpro.core.datasets.ArtifactDescription;
import de.tudarmstadt.ukp.dkpro.core.datasets.ArtifactRole;
import de.tudarmstadt.ukp.dkpro.core.datasets.DatasetDescription;
import de.tudarmstadt.ukp.dkpro.core.datasets.internal.DatasetDescriptionImpl;

public class Split
    extends Action_ImplBase
{
    private final Log LOG = LogFactory.getLog(getClass());
    
    @Override
    public void apply(ActionDescription aAction, DatasetDescription aDataset,
            ArtifactDescription aPack, Path aCachedFile)
                throws Exception
    {
        Map<String, Object> cfg = aAction.getConfiguration();
        List<String> includes = Explode.coerceToList(cfg.get("includes"));
        List<String> excludes = Explode.coerceToList(cfg.get("excludes"));
        double trainRatio = cfg.containsKey("train") ? (double) cfg.get("train") : -1;
        double testRatio = cfg.containsKey("test") ? (double) cfg.get("test") : -1;
        
        DatasetDescriptionImpl dsi = (DatasetDescriptionImpl) aDataset;
        Path dataDir = dsi.getOwner().resolve(dsi);
        
        Collection<File> files = FileUtils.listFiles(dataDir.toFile(),
                new AntFileFilter(dataDir, includes, excludes), TrueFileFilter.TRUE);
        
        File[] all = files.toArray(new File[files.size()]);
        Arrays.sort(all, (File a, File b) -> { return a.getName().compareTo(b.getName()); });
        
        // Relativize files
        for (int i = 0; i < all.length; i++) {
            all[i] = dataDir.relativize(all[i].toPath()).toFile();
        }
        
        int trainPivot = (int) Math.round(all.length * trainRatio);
        int testPivot = (int) Math.round(all.length * testRatio) + trainPivot;
        File[] train = (File[]) ArrayUtils.subarray(all, 0, trainPivot);
        File[] test = (File[]) ArrayUtils.subarray(all, trainPivot, testPivot);

        if (testPivot != all.length) {
            LOG.info("Files missing from split: [" + (all.length - testPivot) + "]");
        }
        
        List<String> trainSet = Arrays.stream(train).map(f -> {
            return f.getPath();
        }).collect(Collectors.toList());

        List<String> testSet = Arrays.stream(test).map(f -> {
            return f.getPath();
        }).collect(Collectors.toList());

        dsi.putRoles(ArtifactRole.TRAINING, trainSet);
        dsi.putRoles(ArtifactRole.TESTING, testSet);
    }
    
    public static class AntFileFilter extends AbstractFileFilter
    {
        private AntPathMatcher matcher = new AntPathMatcher();
        
        private Path baseDir;
        private List<String> includes;
        private List<String> excludes;
        
        public AntFileFilter(Path aBaseDir, List<String> aIncludes, List<String> aExcludes)
        {
            baseDir = aBaseDir;
            includes = aIncludes;
            excludes = aExcludes;
        }

        @Override
        public boolean accept(File aFile)
        {
            File relativeFile = baseDir.relativize(aFile.toPath()).toFile();
            
            // If includes are set, we only consider stuff that is included
            if (includes != null) {
                for (String include : includes) {
                    if (!matcher.match(include, relativeFile.getPath())) {
                        return false;
                    }
                }
            }

            // If excludes are set, they are applied after any includes
            if (excludes != null) {
                for (String exclude : excludes) {
                    if (matcher.match(exclude, relativeFile.getPath())) {
                        return false;
                    }
                }
            }

            return true;
        }
    }
}
