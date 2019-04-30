/*
 * Copyright 2017
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
package org.dkpro.core.api.datasets.internal;

import static java.util.Arrays.asList;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.dkpro.core.api.datasets.Dataset;
import org.dkpro.core.api.datasets.DatasetDescription;
import org.dkpro.core.api.datasets.DatasetFactory;
import org.dkpro.core.api.datasets.FileRole;
import org.dkpro.core.api.datasets.Split;
import org.dkpro.core.api.datasets.internal.util.AntFileFilter;

public class LoadedDataset
    implements Dataset
{
    private DatasetFactory factory;
    private DatasetDescription description;
    private Split defaultSplit;
    
    public LoadedDataset(DatasetFactory aFactory, DatasetDescription aDescription)
    {
        super();
        factory = aFactory;
        description = aDescription;
        
        File[] train =  getFiles(FileRole.TRAINING);
        File[] dev =  getFiles(FileRole.DEVELOPMENT);
        File[] test =  getFiles(FileRole.TESTING);
        
        if (train.length > 0 || dev.length > 0 || test.length > 0) {
            defaultSplit = new SplitImpl(train, test, dev);
        }
    }

    @Override
    public String getName()
    {
        return description.getId();
    }

    @Override
    public String getLanguage()
    {
        return description.getLanguage();
    }
    
    @Override
    public String getEncoding()
    {
        return description.getEncoding();
    }

    @Override
    public File[] getDataFiles()
    {
        Set<File> all = new HashSet<>();
        
        // Collect all data files
        all.addAll(asList(getFiles(FileRole.DATA)));

        // If no files are marked as data files, try aggregating over test/dev/train sets
        if (all.isEmpty()) {
            Split split = getDefaultSplit();
            if (split != null) {
                all.addAll(asList(split.getTrainingFiles()));
                all.addAll(asList(split.getTestFiles()));
                all.addAll(asList(split.getDevelopmentFiles()));
            }
        }
        
        // Sort to ensure stable order
        File[] result = all.toArray(all.toArray(new File[all.size()]));
        Arrays.sort(result, (a, b) -> { return a.getPath().compareTo(b.getPath()); });
        
        return result;
    }
    
    @Override
    public File[] getLicenseFiles()
    {
        return getFiles(FileRole.LICENSE);
    }

    @Override
    public Split getDefaultSplit()
    {
        return defaultSplit;
    }

    @Override
    public File getFile(String aPath)
    {
        Path baseDir = factory.resolve(description);
        return new File(baseDir.toFile(), aPath);
    }
    
    private File[] getFiles(String aRole)
    {
        List<File> files = new ArrayList<>();
        
        List<String> patterns = description.getRoles().get(aRole);
        if (patterns == null) {
            return new File[0];
        }
        
        for (String pattern : patterns) {
            Path baseDir = factory.resolve(description);
            
            Collection<File> matchedFiles = FileUtils.listFiles(baseDir.toFile(),
                    new AntFileFilter(baseDir, asList(pattern), null), TrueFileFilter.TRUE);
            
            files.addAll(matchedFiles);
        }
        
        File[] all = files.toArray(new File[files.size()]);
        Arrays.sort(all, (File a, File b) -> { return a.getName().compareTo(b.getName()); });
        
        return all;
    }
}
