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

import static java.util.Arrays.asList;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FilenameUtils;
import de.tudarmstadt.ukp.dkpro.core.datasets.ActionDescription;
import de.tudarmstadt.ukp.dkpro.core.datasets.ArtifactDescription;
import de.tudarmstadt.ukp.dkpro.core.datasets.DatasetDescription;
import de.tudarmstadt.ukp.dkpro.core.datasets.internal.DatasetDescriptionImpl;
import de.tudarmstadt.ukp.dkpro.core.datasets.internal.util.AntFileFilter;

public class Explode
    extends Action_ImplBase
{
    @Override
    public void apply(ActionDescription aAction, DatasetDescription aDataset,
            ArtifactDescription aPack, Path aCachedFile)
                throws Exception
    {
        DatasetDescriptionImpl dsi = (DatasetDescriptionImpl) aDataset;
        
        try (InputStream is = new BufferedInputStream(Files.newInputStream(aCachedFile))) {
            InputStream uncompressed;
            
            try {
                uncompressed = new BufferedInputStream(
                        new CompressorStreamFactory().createCompressorInputStream(is));
            }
            catch (CompressorException e) {
                // If the compressor is not detected, we may be dealing with an archive format that
                // compresses internally, e.g. ZIP.
                uncompressed = is;
            }

            ArchiveInputStream archive = new ArchiveStreamFactory()
                    .createArchiveInputStream(uncompressed);
            extract(aAction, aCachedFile, archive, dsi.getOwner().resolve(dsi));
        }
    }

    private void extract(ActionDescription aAction, Path aArchive, ArchiveInputStream aArchiveStream, Path aTarget)
        throws IOException
    {
        // We always extract archives into a subfolder. Figure out the name of the folder.
        String base = getBase(aArchive.getFileName().toString());
        
        Map<String, Object> cfg = aAction.getConfiguration();
        List<String> includes = coerceToList(cfg.get("includes"));
        List<String> excludes = coerceToList(cfg.get("excludes"));
        int strip = cfg.containsKey("strip") ? (int) cfg.get("strip") : 0;
        
        AntFileFilter filter = new AntFileFilter(includes, excludes);
        
        ArchiveEntry entry = null;
        while ((entry = aArchiveStream.getNextEntry()) != null) {
            String name = entry.getName();

            boolean skip = false;

            if (strip > 0) {
                Path p = Paths.get(name);
                if (p.getNameCount() <= strip) {
//                    if (entry.isDirectory()) {
                        skip = true;
//                    }
//                    else {
//                        throw new IllegalStateException(
//                                "Stripped folder contains file: [" + name + "]");
//                    }
                }
                else {
                    p = p.subpath(strip, p.getNameCount());
                    name = p.toString();
                }
            }
            
            Path out = aTarget.resolve(base).resolve(name);
            
            if (!skip && !filter.accept(name)) {
                skip = true;
            }
            
            if (!skip) {
                if (entry.isDirectory()) {
                    Files.createDirectories(out);
                }
                else {
                    Files.createDirectories(out.getParent());
                    Files.copy(aArchiveStream, out);
                }
            }
        }
    }
    
    public static String getBase(String aFilename)
    {
        // We always extract archives into a subfolder. Figure out the name of the folder.
        String base = aFilename;
        while (base.contains(".")) {
            base = FilenameUtils.removeExtension(base);
        }
        return base;
    }
    
    @SuppressWarnings("unchecked")
    public static List<String> coerceToList(Object aRaw)
    {
        List<String> cooked;
        if (aRaw == null) {
            return null;
        }
        else if (aRaw instanceof String) {
            cooked = asList((String) aRaw);
        }
        else if (aRaw instanceof List) {
            cooked = (List<String>) aRaw;
        }
        else {
            throw new IllegalArgumentException("Cannot coerce to String list: [" + aRaw + "]");
        }
        return cooked;
    }
}
