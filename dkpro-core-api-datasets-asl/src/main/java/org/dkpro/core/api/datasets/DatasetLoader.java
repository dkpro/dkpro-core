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
package org.dkpro.core.api.datasets;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dkpro.core.api.datasets.internal.ud.UDDataset;

@Deprecated
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

    public List<Dataset> loadUniversalDependencyTreebankV1_3() throws IOException
    {
        File dataDir = new File(cacheRoot, "ud-treebanks-v1.3");

        DataPackage data = new DataPackage.Builder()
                .url("https://lindat.mff.cuni.cz/repository/xmlui/bitstream/handle/11234/"
                        + "1-1699/ud-treebanks-v1.3.tgz?sequence=1&isAllowed=y")
                .sha1("44367112880cf0af3f293cb3f0cc6ce50c0e65c0").target("ud-treebanks-v1.3.tgz")
                .postAction((d) -> {
                    untgz(new File(dataDir, d.getTarget()), dataDir);
                }).build();

        fetch(dataDir, data);

        List<Dataset> sets = new ArrayList<>();
        for (File f : new File(dataDir, "ud-treebanks-v1.3").listFiles()) {
            sets.add(new UDDataset(f));
        }
        return sets;
    }

    private void fetch(File aTarget, DataPackage... aPackages) throws IOException
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
                    LOG.info("Local SHA1 hash verified on [" + cachedFile + "] - [" + actual + "]");
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
                    LOG.info("Local MD5 hash verified on [" + cachedFile + "] - [" + actual + "]");
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
            File postActionCompleteMarker = new File(cachedFile.getPath() + ".postComplete");
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

    private void untgz(File aArchive, File aTarget) throws IOException
    {
        try (ArchiveInputStream archive = new TarArchiveInputStream(new GzipCompressorInputStream(
                new BufferedInputStream(new FileInputStream(aArchive))))) {
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

            Path base = aTarget.toPath().toAbsolutePath();
            Path out = base.resolve(name).toAbsolutePath();
            
            if (!out.startsWith(base)) {
                // Ignore attempts to write outside the base
                continue;
            }
            
            if (entry.isDirectory()) {
                FileUtils.forceMkdir(out.toFile());
            }
            else {
                FileUtils.copyInputStreamToFile(new CloseShieldInputStream(aArchiveStream),
                        out.toFile());
            }
        }
    }
}
