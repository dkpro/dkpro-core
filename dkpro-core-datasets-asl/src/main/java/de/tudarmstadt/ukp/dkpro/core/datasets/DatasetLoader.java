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
package de.tudarmstadt.ukp.dkpro.core.datasets;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.CloseShieldInputStream;

public class DatasetLoader
{
    private File cacheRoot;
    
    public DatasetLoader()
    {
    }
    
    public DatasetLoader(File aCacheRoot)
    {
        setCacheRoot(aCacheRoot);
    }
    
    public void setCacheRoot(File aCacheRoot)
    {
        cacheRoot = aCacheRoot;
    }
    
    public File getCacheRoot()
    {
        return cacheRoot;
    }
    
    public File loadNEMGP()
            throws IOException
    {
        File dataDir = new File(cacheRoot, "NEMGP");
        
        File target = new File(dataDir, "nemgp_trainingdata_01.txt.zip");
        
        fetch(target,
                "http://www.thomas-zastrow.de/nlp/nemgp_trainingdata_01.txt.zip",
                "FIXME", null);
        
        return target;
    }
    
    public File loadGermEval2014NER()
            throws IOException
    {
        File dataDir = new File(cacheRoot, "germeval2014ner");
        
        fetch(new File(dataDir, "NER-de-dev.tsv"),
                "https://sites.google.com/site/germeval2014ner/data/NER-de-dev.tsv?attredirects=0&d=1",
                "1a427a764c8cbd1bcb64e673da1a7d08", null);
        fetch(new File(dataDir, "NER-de-test.tsv"),
                "https://sites.google.com/site/germeval2014ner/data/NER-de-test.tsv?attredirects=0&d=1",
                "e5f80415426eb4c651ac99550fdc8487", null);
        fetch(new File(dataDir, "NER-de-train.tsv"),
                "https://sites.google.com/site/germeval2014ner/data/NER-de-train.tsv?attredirects=0&d=1",
                "17fdf2ef0ce76896d575f9a5f4b62e14", null);
        
        return dataDir;
    }
    
    public File loadUniversalDependencyTreebankV1_3()
        throws IOException
    {
        File dataDir = cacheRoot;
        
        File target = new File(dataDir, "ud-treebanks-v1.3.tgz");
        
        fetch(target, "https://lindat.mff.cuni.cz/repository/xmlui/bitstream/handle/11234/"
                + "1-1699/ud-treebanks-v1.3.tgz?sequence=1&isAllowed=y", 
                "2ed9122f164ec1a19729983e68a2ce9a", null);
        untar(target, dataDir);
        
        return new File(dataDir, "ud-treebanks-v1.3");
    }

    public static void fetch(File aTarget, String aUrl, String aSha1, String aMd5)
        throws IOException
    {
        if (!aTarget.exists()) {
            aTarget.getParentFile().mkdirs();
            URL source = new URL(aUrl);
            
            MessageDigest md5;
            try {
                md5 = MessageDigest.getInstance("MD5");
            }
            catch (NoSuchAlgorithmException e) {
                throw new IOException(e);
            }
            
            MessageDigest sha1;
            try {
                sha1 = MessageDigest.getInstance("SHA1");
            }
            catch (NoSuchAlgorithmException e) {
                throw new IOException(e);
            }
            
            try (InputStream is = source.openStream()) {
                DigestInputStream md5Filter = new DigestInputStream(is, md5);
                DigestInputStream sha1Filter = new DigestInputStream(md5Filter, sha1);
                FileUtils.copyInputStreamToFile(sha1Filter, aTarget);

                if (aMd5 != null) {
                    String md5Hex = new String(
                            Hex.encodeHex(md5Filter.getMessageDigest().digest()));
                    if (!aMd5.equals(md5Hex)) {
                        throw new IOException(
                                "MD5 mismatch. Expected [" + aMd5 + "] but got [" + md5Hex + "].");
                    }
                }
                if (aSha1 != null) {
                    String sha1Hex = new String(
                            Hex.encodeHex(md5Filter.getMessageDigest().digest()));
                    if (!aSha1.equals(sha1Hex)) {
                        throw new IOException("SHA1 mismatch. Expected [" + aSha1 + "] but got ["
                                + sha1Hex + "].");
                    }
                }
            }
        }
    }
    
    public static void untar(File aArchive, File aTarget)
        throws FileNotFoundException, IOException
    {
        try (ArchiveInputStream tarIn = new TarArchiveInputStream(
                new GzipCompressorInputStream(
                        new BufferedInputStream(new FileInputStream(aArchive))))) {
            ArchiveEntry entry = null;
            while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                File out = new File(aTarget, entry.getName());
//                if (entry.getName().endsWith("stats.xml")) {
//                    // Some stats.xml files are actually invalid XML and show as annoying errors
//                    // in Eclipse.
//                    continue;
//                }
                if (entry.isDirectory()) {
                    FileUtils.forceMkdir(out);
                }
                else {
                    FileUtils.copyInputStreamToFile(new CloseShieldInputStream(tarIn), out);
                }
            }
        }
    }
}
