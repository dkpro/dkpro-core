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
package de.tudarmstadt.ukp.dkpro.core.api.datasets.internal.actions;

import static java.util.Arrays.asList;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.impl.FileVolumeManager;
import com.github.junrar.rarfile.FileHeader;

import de.tudarmstadt.ukp.dkpro.core.api.datasets.ActionDescription;
import de.tudarmstadt.ukp.dkpro.core.api.datasets.ArtifactDescription;
import de.tudarmstadt.ukp.dkpro.core.api.datasets.DatasetDescription;
import de.tudarmstadt.ukp.dkpro.core.api.datasets.internal.DatasetDescriptionImpl;
import de.tudarmstadt.ukp.dkpro.core.api.datasets.internal.util.AntFileFilter;

public class Explode
    extends Action_ImplBase
{
    @Override
    public void apply(ActionDescription aAction, DatasetDescription aDataset,
            ArtifactDescription aPack, Path aCachedFile)
                throws Exception
    {
        DatasetDescriptionImpl dsi = (DatasetDescriptionImpl) aDataset;
        
        Map<String, Object> cfg = aAction.getConfiguration();
        // Sometimes, we have to explode a file that was created as the result of exploding the
        // main artifact. Thus, we can override the target
        Path targetFile = cfg.containsKey("file")
                ? dsi.getOwner().resolve(dsi).resolve((String) cfg.get("file")) : aCachedFile;        
        
                // Apache Commons Compress does not handle RAR files, so we handle them separately
        if (targetFile.toString().toLowerCase(Locale.ENGLISH).endsWith(".rar")) {
            extractRar(aAction, targetFile, dsi.getOwner().resolve(dsi));
        }
        if (targetFile.toString().toLowerCase(Locale.ENGLISH).endsWith(".7z")) {
            // 7z does not support streaming in Apache Commons Compress
            extract7z(aAction, targetFile, dsi.getOwner().resolve(dsi));
        }
        else {
            // Auto-detect the archive format using Apache Commons Compress 
            try (InputStream is = new BufferedInputStream(Files.newInputStream(targetFile))) {
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
                extract(aAction, targetFile, archive, dsi.getOwner().resolve(dsi));
            }
        }
    }

    private void extract7z(ActionDescription aAction, Path aCachedFile, Path aTarget)
        throws IOException, RarException
    {
        // We always extract archives into a subfolder. Figure out the name of the folder.
        String base = getBase(aCachedFile.getFileName().toString());
        
        Map<String, Object> cfg = aAction.getConfiguration();
        int strip = cfg.containsKey("strip") ? (int) cfg.get("strip") : 0;
        
        AntFileFilter filter = new AntFileFilter(coerceToList(cfg.get("includes")),
                coerceToList(cfg.get("excludes")));
        
        try (SevenZFile archive = new SevenZFile(aCachedFile.toFile())) {
            SevenZArchiveEntry entry = archive.getNextEntry();
            while (entry != null) {
                String name = stripLeadingFolders(entry.getName(), strip);
                
                if (name == null) {
                    // Stripped to null - nothing left to extract - continue;
                    continue;
                }
                
                if (filter.accept(name)) {
                    Path out = aTarget.resolve(base).resolve(name);
                    if (entry.isDirectory()) {
                        Files.createDirectories(out);
                    }
                    else {
                        Files.createDirectories(out.getParent());
                        try (OutputStream os = Files.newOutputStream(out)) {
                            InputStream is = new SevenZEntryInputStream(archive, entry);
                            IOUtils.copyLarge(is, os);
                        }
                    }
                }
                
                entry = archive.getNextEntry();
            }
        }
    }

    private void extractRar(ActionDescription aAction, Path aCachedFile, Path aTarget)
        throws IOException, RarException
    {
        // We always extract archives into a subfolder. Figure out the name of the folder.
        String base = getBase(aCachedFile.getFileName().toString());
        
        Map<String, Object> cfg = aAction.getConfiguration();
        int strip = cfg.containsKey("strip") ? (int) cfg.get("strip") : 0;
        
        AntFileFilter filter = new AntFileFilter(coerceToList(cfg.get("includes")),
                coerceToList(cfg.get("excludes")));
        
        try (Archive archive = new Archive(new FileVolumeManager(aCachedFile.toFile()))) {
            FileHeader fh = archive.nextFileHeader();
            while (fh != null) {
                String name = stripLeadingFolders(fh.getFileNameString(), strip);
                
                if (name == null) {
                    // Stripped to null - nothing left to extract - continue;
                    continue;
                }
                
                if (filter.accept(name)) {
                    Path out = aTarget.resolve(base).resolve(name);
                    if (fh.isDirectory()) {
                        Files.createDirectories(out);
                    }
                    else {
                        Files.createDirectories(out.getParent());
                        try (OutputStream os = Files.newOutputStream(out)) {
                            archive.extractFile(fh, os);
                        }
                    }
                }
                
                
                fh = archive.nextFileHeader();
            }
        }
    }

    private void extract(ActionDescription aAction, Path aArchive, ArchiveInputStream aAStream,
            Path aTarget)
                throws IOException
    {
        // We always extract archives into a subfolder. Figure out the name of the folder.
        String base = getBase(aArchive.getFileName().toString());
        
        Map<String, Object> cfg = aAction.getConfiguration();
        int strip = cfg.containsKey("strip") ? (int) cfg.get("strip") : 0;
        
        AntFileFilter filter = new AntFileFilter(coerceToList(cfg.get("includes")),
                coerceToList(cfg.get("excludes")));
        
        ArchiveEntry entry = null;
        while ((entry = aAStream.getNextEntry()) != null) {
            String name = stripLeadingFolders(entry.getName(), strip);
            
            if (name == null) {
                // Stripped to null - nothing left to extract - continue;
                continue;
            }
            
            if (filter.accept(name)) {
                Path out = aTarget.resolve(base).resolve(name);
                if (entry.isDirectory()) {
                    Files.createDirectories(out);
                }
                else {
                    Files.createDirectories(out.getParent());
                    Files.copy(aAStream, out);
                }
            }
        }
    }
    
    private String stripLeadingFolders(String aName, int aLevels)
    {
        if (aLevels > 0) {
            Path p = Paths.get(aName);
            if (p.getNameCount() <= aLevels) {
                return null;
            }
            else {
                p = p.subpath(aLevels, p.getNameCount());
                aName = p.toString();
                return aName;
            }
        }
        else {
            return aName;
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
    
    private static class SevenZEntryInputStream
        extends InputStream
    {
        private SevenZFile archive;
        private SevenZArchiveEntry entry;
        private int totalRead;
        
        public SevenZEntryInputStream(SevenZFile aArchive, SevenZArchiveEntry aEnty)
        {
            archive = aArchive;
            entry = aEnty;
        }
        
        @Override
        public int read()
            throws IOException
        {
            if (totalRead < entry.getSize()) {
                totalRead++;
                return archive.read();
            }
            else {
                return -1;
            }
        }
        
        @Override
        public int read(byte[] aB, int aOff, int aLen)
            throws IOException
        {
            if (totalRead < entry.getSize()) {
                int blocksize = (int) Math.min(aLen, entry.getSize() - totalRead);
                int read = archive.read(aB, aOff, blocksize);
                totalRead += read;
                return read;
            }
            else {
                return -1;
            }
        }
    }
}
