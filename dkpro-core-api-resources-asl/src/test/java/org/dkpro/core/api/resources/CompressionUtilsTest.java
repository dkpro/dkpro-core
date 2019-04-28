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
package org.dkpro.core.api.resources;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.dkpro.core.api.resources.CompressionMethod;
import org.dkpro.core.api.resources.CompressionUtils;
import org.junit.Test;

public class CompressionUtilsTest
{
    private static void testCompression(CompressionMethod compressionMethod)
            throws IOException
    {
        String text = StringUtils.repeat("This is a test. ", 100000);

        File file = new File("compressed" + compressionMethod.getExtension());

        OutputStream os = CompressionUtils.getOutputStream(file);
        os.write(text.getBytes());
        os.close();
        InputStream is = CompressionUtils.getInputStream(file.getPath(), new FileInputStream(file));
        assertEquals(text, IOUtils.toString(is));
        is.close();
        file.delete();
    }

    @Test
    public void testUncompressed()
            throws Exception
    {
        CompressionMethod compressionMethod = CompressionMethod.NONE;
        String text = StringUtils.repeat("This is a test. ", 100000);
        testCompression(compressionMethod);
    }

    @Test
    public void testXZ()
            throws Exception
    {
        CompressionMethod compressionMethod = CompressionMethod.XZ;
        testCompression(compressionMethod);
    }

    @Test
    public void testBZip2()
            throws Exception
    {
        CompressionMethod compressionMethod = CompressionMethod.BZIP2;
        testCompression(compressionMethod);
    }

    @Test
    public void testGZip()
            throws Exception
    {
        CompressionMethod compressionMethod = CompressionMethod.GZIP;
        testCompression(compressionMethod);
    }

    @Test
    public void testPrintWriter()
            throws IOException
    {
        CompressionMethod compressionMethod = CompressionMethod.XZ;
        String text = StringUtils.repeat("This is a test. ", 100000);
        File file = new File("compressed" + compressionMethod.getExtension());
        PrintWriter printWriter = new PrintWriter(CompressionUtils.getOutputStream(file));

        printWriter.write(text);
        printWriter.close();

        InputStream is = CompressionUtils.getInputStream(file.getPath(), new FileInputStream(file));
        assertEquals(text, IOUtils.toString(is));
        is.close();
        file.delete();
    }
}
