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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.tudarmstadt.ukp.dkpro.core.datasets.internal.DefaultDataset;
import de.tudarmstadt.ukp.dkpro.core.datasets.internal.ud.UDDataset;

public class DatasetLoader
{
    private final Log LOG = LogFactory.getLog(getClass());
    
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

    public Dataset loadNEMGP()
        throws IOException
    {
        DefaultDataset ds = new DefaultDataset("NEMGP", "de");
        File dataDir = new File(cacheRoot, ds.getName());

        DataPackage license = DataPackage.LICENSE_CC_BY_SA_3_0;
        
        DataPackage data = new DataPackage.Builder()
                .url("http://www.thomas-zastrow.de/nlp/nemgp_trainingdata_01.txt.zip")
                .sha1("f2a1fd54df9232741a3a1892d1ffb0a4d7205991")
                .target("nemgp_trainingdata_01.txt.zip")
                .postAction((d) -> { unzip(new File(dataDir, d.getTarget()), dataDir); })
                .build();
        
        fetch(dataDir, license, data);

        ds.setLicenseFile(new File(dataDir, license.getTarget()));
        ds.setTrainingFiles(new File(dataDir, "nemgp_trainingdata_01.txt"));

        return ds;
    }

    public Dataset loadGermEval2014NER()
        throws IOException
    {
        String baseUrl = "https://sites.google.com/site/germeval2014ner/data/";
        
        DefaultDataset ds = new DefaultDataset("germeval2014ner", "de");
        File dataDir = new File(cacheRoot, ds.getName());

        DataPackage license = DataPackage.LICENSE_CC_BY_4_0;
        
        DataPackage dev = new DataPackage.Builder()
                .url(baseUrl + "NER-de-dev.tsv?attredirects=0&d=1")
                .sha1("70aba5d247f51ec22e0bcc671c7fb325e4ff4277")
                .target("NER-de-dev.tsv")
                .build();

        DataPackage test = new DataPackage.Builder()
                .url(baseUrl + "NER-de-test.tsv?attredirects=0&d=1")
                .sha1("214deaf091e01567af2e958aac87863bf685342a")
                .target("NER-de-train.tsv")
                .build();

        DataPackage train = new DataPackage.Builder()
                .url(baseUrl + "NER-de-train.tsv?attredirects=0&d=1")
                .sha1("7644cb09676050c0a2836e06fa0aeb8509b9e1cb")
                .target("NER-de-test.tsv")
                .build();

        fetch(dataDir, license, dev, test, train);
        
        ds.setDevelopmentFiles(new File(dataDir, dev.getTarget()));
        ds.setTrainingFiles(new File(dataDir, train.getTarget()));
        ds.setTestFiles(new File(dataDir, test.getTarget()));
        ds.setLicenseFile(new File(dataDir, license.getTarget()));

        return ds;
    }
    
    /**
     * Dependency TreeBank of Ancient Greek and Latin
     * https://perseusdl.github.io/treebank_data/ 
     */
    public Dataset loadAncientGreekAndLatingDependencyTreebank()
            throws IOException
        {
            DefaultDataset ds = new DefaultDataset("perseus", "grc/la");
            File dataDir = new File(cacheRoot, ds.getName());

            String commit = "f56a35f65ef15ac454f6fbd2cfc6ea97bf2ca9b8";

            DataPackage data = new DataPackage.Builder()
                    .url("https://github.com/PerseusDL/treebank_data/archive/" + commit + ".zip")
                    .sha1("140eee6d2e3e83745f95d3d5274d9e965d898980")
                    .target("Perseus.zip")
                    .postAction((d) -> { unzip(new File(dataDir, d.getTarget()), dataDir); })
                    .build();
            
            fetch(dataDir, data);
            
            return ds;
        }

    public Dataset loadEnglishBrownCorpus()
        throws IOException
    {
        DefaultDataset ds = new DefaultDataset("brownCorpus-TEI-XML", "en");
        File dataDir = new File(cacheRoot, ds.getName());

        DataPackage data = new DataPackage.Builder()
                .url("https://raw.githubusercontent.com/nltk/nltk_data/gh-pages/packages/corpora/brown_tei.zip")
                .sha1("1e4eadeb358f6f7e6ac9b3677a82f4353bbe91ed")
                .target("brown.zip")
                .postAction((d) -> { unzip(new File(dataDir, d.getTarget()), dataDir); })
                .build();
        
        fetch(dataDir, data);
        
        File license = new File(dataDir, "LICENSE.txt");
        ds.setLicenseFile(license);

        FileUtils.writeStringToFile(license, "May be used for non-commercial purposes.", "UTF-8");

        /*
         * Archive contains the corpus twice. Once as single-fat file and split up in many smaller
         * files. We delete the fat-file.
         */
        new File(dataDir + "/brown_tei", "Corpus.xml").delete();

        return ds;
    }

    /**
     * Georgetown University Multilayer Corpus https://corpling.uis.georgetown.edu/gum/
     */
    public Dataset loadEnglishGUMCorpus()
        throws IOException
    {
        String commit = "a92eca9847185627c282d9c4c0f9a2d521149c79";
        
        DefaultDataset ds = new DefaultDataset("GeorgetownUniversityMultilayerCorpus", "en");
        File dataDir = new File(cacheRoot, ds.getName());

        DataPackage data = new DataPackage.Builder()
                .url("https://github.com/amir-zeldes/gum/archive/" + commit + ".zip")
                .sha1("a2c368f7a5cdb045219011557bec0fea1b56468e")
                .target("gum.zip")
                .postAction((d) -> { unzip(new File(dataDir, d.getTarget()), dataDir); })
                .build();

        fetch(dataDir, data);
        
        File license = new File(dataDir, "gum-" + commit + "/LICENSE.txt");
        ds.setLicenseFile(license);
        
        File conll2006Path = new File(dataDir, "gum-" + commit + "/dep");
        File[] all = conll2006Path.listFiles(file -> { return file.getName().endsWith(".conll10"); });
        Arrays.sort(all, (File a, File b) -> { return a.getName().compareTo(b.getName()); });
        
        int pivot = all.length / 2;
        File[] train = (File[]) ArrayUtils.subarray(all, 0, pivot);
        File[] test = (File[]) ArrayUtils.subarray(all, pivot, all.length);
        
        ds.setTrainingFiles(train);
        ds.setTestFiles(test);

        return ds;
    }

    /**
     * Deep-sequoia is a corpus of French sentences annotated with both surface and deep syntactic
     * dependency structures
     * https://deep-sequoia.inria.fr
     */
    public Dataset loadFrenchDeepSequoiaCorpus()
        throws IOException
    {
        DefaultDataset ds = new DefaultDataset("Sequoia_0.7", "en");
        File dataDir = new File(cacheRoot, ds.getName());

        DataPackage data = new DataPackage.Builder()
                .url("http://talc2.loria.fr/deep-sequoia/sequoia-7.0.tgz")
                .sha1("9f53475f809ef1032a92adedf262226da1615051")
                .target("sequoia.tgz")
                .postAction((d) -> { untgz(new File(dataDir, d.getTarget()), dataDir); })
                .build();
        
        fetch(dataDir, data);

        return ds;
    }
    
    /**
     * German Hamburg Dependency Treebank 
     * Contains annotated text from the German technical news website <i>www.heise.de</i>  
     * https://corpora.uni-hamburg.de/drupal/de/islandora/object/treebank:hdt 
     */
    public Dataset loadGermanHamburgDependencyTreebank()
        throws IOException
    {
        DefaultDataset ds = new DefaultDataset("HamburgDependencyTreebank", "de");
        File dataDir = new File(cacheRoot, ds.getName());

        DataPackage data = new DataPackage.Builder()
                .url("https://corpora.uni-hamburg.de:8443/fedora/objects/file:hdt_hdt-conll/datastreams/hdt-conll-tar-xz/content?asOfDateTime=2016-02-17T15:38:47.643Z&amp;download=true")
                .sha1("6594e5cd48966db7dac04f2b5ff948eb2bcadf37")
                .target("hamburgDepTreebank.tar.xz")
                .postAction((d) -> { untarxz(new File(dataDir, d.getTarget()), dataDir); })
                .build();

        fetch(dataDir, data);

        return ds;
    }

    public List<Dataset> loadUniversalDependencyTreebankV1_3()
        throws IOException
    {
        File dataDir = new File(cacheRoot, "ud-treebanks-v1.3");

        DataPackage data = new DataPackage.Builder()
                .url("https://lindat.mff.cuni.cz/repository/xmlui/bitstream/handle/11234/"
                        + "1-1699/ud-treebanks-v1.3.tgz?sequence=1&isAllowed=y")
                .sha1("44367112880cf0af3f293cb3f0cc6ce50c0e65c0")
                .target("ud-treebanks-v1.3.tgz")
                .postAction((d) -> { untgz(new File(dataDir, d.getTarget()), dataDir); })
                .build();
        
        fetch(dataDir, data);

        List<Dataset> sets = new ArrayList<>();
        for (File f : new File(dataDir, "ud-treebanks-v1.3").listFiles()) {
            sets.add(new UDDataset(f));
        }
        return sets;
    }
    
    private void fetch(File aTarget, DataPackage... aPackages)
        throws IOException
    {
        // First validate if local copies are still up-to-date
        boolean reload = false;
        packageValidationLoop: for (DataPackage pack : aPackages) {
            File cachedFile = new File(aTarget, pack.getTarget());
            if (!cachedFile.exists()) {
                continue;
            }
            
            if (pack.getSha1() != null) {
                String actual = getDigest(cachedFile, "SHA1");
                if (!pack.getSha1().equals(actual)) {
                    LOG.info("Local SHA1 hash mismatch on [" + cachedFile + "] - expected ["
                            + pack.getSha1() + "] - actual [" + actual + "]");
                    reload = true;
                    break packageValidationLoop;
                }
                else {
                    LOG.info("Local SHA1 hash verified on [" + cachedFile + "] - expected ["
                            + pack.getSha1() + "] - actual [" + actual + "]");
                }
            }
            
            if (pack.getMd5() != null) {
                String actual = getDigest(cachedFile, "MD5");
                if (!pack.getMd5().equals(actual)) {
                    LOG.info("Local MD5 hash mismatch on [" + cachedFile + "] - expected ["
                            + pack.getMd5() + "] - actual [" + actual + "]");
                    reload = true;
                    break packageValidationLoop;
                }
                else {
                    LOG.info("Local MD5 hash verified on [" + cachedFile + "] - expected ["
                            + pack.getMd5() + "] - actual [" + actual + "]");
                }
            }
            
        }
        
        // If any of the packages are outdated, clear the cache and download again
        if (reload) {
            LOG.info("Clearing local cache for [" + aTarget + "]");
            FileUtils.deleteQuietly(aTarget);
        }
        
        for (DataPackage pack : aPackages) {
            File cachedFile = new File(aTarget, pack.getTarget());
            
            if (cachedFile.exists()) {
                continue;
            }

            
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

            cachedFile.getParentFile().mkdirs();
            URL source = new URL(pack.getUrl());

            LOG.info("Fetching [" + cachedFile + "]");
            
            URLConnection connection = source.openConnection();
            connection.setRequestProperty("User-Agent", "Java");
            
            try (InputStream is = connection.getInputStream()) {
                DigestInputStream md5Filter = new DigestInputStream(is, md5);
                DigestInputStream sha1Filter = new DigestInputStream(md5Filter, sha1);
                FileUtils.copyInputStreamToFile(sha1Filter, cachedFile);

                if (pack.getMd5() != null) {
                    String md5Hex = new String(
                            Hex.encodeHex(md5Filter.getMessageDigest().digest()));
                    if (!pack.getMd5().equals(md5Hex)) {
                        String message = "MD5 mismatch. Expected [" + pack.getMd5() + "] but got ["
                                + md5Hex + "].";
                        LOG.error(message);
                        throw new IOException(message);
                    }
                }
                
                if (pack.getSha1() != null) {
                    String sha1Hex = new String(
                            Hex.encodeHex(sha1Filter.getMessageDigest().digest()));
                    if (!pack.getSha1().equals(sha1Hex)) {
                        String message = "SHA1 mismatch. Expected [" + pack.getSha1()
                                + "] but got [" + sha1Hex + "].";
                        LOG.error(message);
                        throw new IOException(message);
                    }
                }
            }
        }
                 
        // Perform a post-fetch action such as unpacking
        for (DataPackage pack : aPackages) {
            File cachedFile = new File(aTarget, pack.getTarget());
            File postActionCompleteMarker = new File(cachedFile.getPath()+".postComplete");
            if (pack.getPostAction() != null && !postActionCompleteMarker.exists()) {
                try {
                    pack.getPostAction().run(pack);
                    FileUtils.touch(postActionCompleteMarker);
                }
                catch (IOException e) {
                    throw e;
                }
                catch (Exception e) {
                   throw new IllegalStateException(e);
                }
            }
        }
    }
    
    private String getDigest(File aFile, String aDigest) throws IOException
    {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(aDigest);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
        try (InputStream is = new FileInputStream(aFile)) {
            DigestInputStream digestFilter = new DigestInputStream(is, digest);
            IOUtils.copy(digestFilter, new NullOutputStream());
            return new String(Hex.encodeHex(digestFilter.getMessageDigest().digest()));
        }
    }
    
    private void untgz(File aArchive, File aTarget)
        throws IOException
    {
        try (ArchiveInputStream archive = new TarArchiveInputStream(new GzipCompressorInputStream(
                new BufferedInputStream(new FileInputStream(aArchive))))) {
            extract(aArchive, archive, aTarget);
        }
    }
    
    public void untarxz(File aArchive, File aTarget) throws IOException {
        
        try (ArchiveInputStream archive = new TarArchiveInputStream(new XZCompressorInputStream(
                new BufferedInputStream(new FileInputStream(aArchive))))) {
            extract(aArchive, archive, aTarget);
        }
    }

    private void unzip(File aArchive, File aTarget)
        throws IOException
    {
        try (ArchiveInputStream archive = new ZipArchiveInputStream(
                new BufferedInputStream(new FileInputStream(aArchive)))) {
            extract(aArchive, archive, aTarget);
        }
    }
    
    private void extract(File aArchive, ArchiveInputStream aArchiveStream, File aTarget)
        throws IOException
    {
        ArchiveEntry entry = null;
        while ((entry = aArchiveStream.getNextEntry()) != null) {
            String name = entry.getName();
            
            // Ensure that the filename will not break the manifest
            if (name.contains("\n")) {
                throw new IllegalStateException("Filename must not contain line break");
            }
            
            File out = new File(aTarget, name);
            if (entry.isDirectory()) {
                FileUtils.forceMkdir(out);
            }
            else {
                FileUtils.copyInputStreamToFile(new CloseShieldInputStream(aArchiveStream), out);
            }
        }
    }
}
