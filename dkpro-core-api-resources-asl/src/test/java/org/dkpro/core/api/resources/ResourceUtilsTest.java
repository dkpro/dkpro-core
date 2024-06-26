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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ResourceUtilsTest
{
    @Test
    public void testGetUrlAsFile()
        throws Exception
    {
        URL url = new URL(
                "jar:file:src/test/resources/testfiles.zip!/testfiles/FileSetCollectionReaderBase.class");
        System.out.println("Original: " + url);
        File file = ResourceUtils.getUrlAsFile(url, false);
        System.out.println("As file: " + file.getPath());
        assertTrue(file.getName().endsWith(".class"));
    }

    @Test
    public void testClasspathAsFolder()
        throws Exception
    {
        File file = ResourceUtils
                .getClasspathAsFolder("classpath:/org/dkpro/core/api", true);

        List<Path> paths = new ArrayList<Path>();
        for (File f : FileUtils.listFiles(file, null, true)) {
            paths.add(Paths.get(f.getAbsolutePath().substring(file.getAbsolutePath().length())));
        }
        Collections.sort(paths);
        assertEquals(asList(Paths.get("/resources/CompressionUtilsTest.class"),
                Paths.get("/resources/MappingProviderTest$1.class"),
                Paths.get("/resources/MappingProviderTest$2.class"),
                Paths.get("/resources/MappingProviderTest.class"),
                Paths.get("/resources/ResourceObjectProviderTest$1.class"),
                Paths.get("/resources/ResourceObjectProviderTest$2.class"),
                Paths.get("/resources/ResourceObjectProviderTest$3.class"),
                Paths.get("/resources/ResourceObjectProviderTest$SharableObjectProvider.class"),
                Paths.get("/resources/ResourceObjectProviderTest.class"),
                Paths.get("/resources/ResourceUtilsTest.class")), paths);
    }

    @Test
    public void testWithSpace(@TempDir File tempDir)
        throws Exception
    {
        File dir = new File(tempDir, "this is a test");
        dir.mkdirs();
        File file = new File(dir, "this is a file name.extension with spaces");

        System.out.println("Original: " + file);
        System.out.println("Original (URL): " + file.toURI().toURL());
        File asFile = ResourceUtils.getUrlAsFile(file.toURI().toURL(), false);
        System.out.println("As file: " + asFile.getPath());
        assertEquals("this is a file name", FilenameUtils.getBaseName(asFile.getPath()));
        assertEquals("extension with spaces", FilenameUtils.getExtension(asFile.getPath()));
    }

    @Test
    public void testGetUrlAsExecutable()
        throws IOException
    {

        URL url = new URL("jar:file:src/test/resources/testfiles.zip!/testfiles/"
                + "FileSetCollectionReaderBase.class");
        File file = ResourceUtils.getUrlAsExecutable(url, false);
        
        assertThat(file.getName()).endsWith("temp");

        URL url2 = new URL("jar:file:src/test/resources/testfiles.zip!/testfiles/"
                + "ResourceCollectionReaderBase.class");
        file = ResourceUtils.getUrlAsExecutable(url2, true);
        
        assertThat(file.getName()).endsWith("temp");
    }
}
