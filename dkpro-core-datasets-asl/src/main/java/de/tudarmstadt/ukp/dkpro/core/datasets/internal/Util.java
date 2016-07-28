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
package de.tudarmstadt.ukp.dkpro.core.datasets.internal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.CloseShieldInputStream;

public class Util
{
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
        try (ArchiveInputStream tarIn = new TarArchiveInputStream(new GzipCompressorInputStream(
                new BufferedInputStream(new FileInputStream(aArchive))))) {
            ArchiveEntry entry = null;
            while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                File out = new File(aTarget, entry.getName());
                // if (entry.getName().endsWith("stats.xml")) {
                // // Some stats.xml files are actually invalid XML and show as annoying errors
                // // in Eclipse.
                // continue;
                // }
                if (entry.isDirectory()) {
                    FileUtils.forceMkdir(out);
                }
                else {
                    FileUtils.copyInputStreamToFile(new CloseShieldInputStream(tarIn), out);
                }
            }
        }
    }

    public static void unzip(File target, File dataDir)
        throws IOException
    {
        BufferedOutputStream dest = null;
        FileInputStream fis = new FileInputStream(target);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {

            if (entry.isDirectory()) {
                new File(dataDir, entry.getName()).mkdirs();
                continue;
            }

            int count;
            byte data[] = new byte[1024];
            // write the files to the disk
            File file = new File(dataDir, entry.getName());
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            dest = new BufferedOutputStream(fos, 1024);
            while ((count = zis.read(data, 0, 1024)) != -1) {
                dest.write(data, 0, count);
            }
            dest.flush();
            dest.close();
        }
        zis.close();
    }
}
