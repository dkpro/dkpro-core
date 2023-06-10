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
package org.dkpro.core.api.io;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.factory.ExternalResourceFactory.createResourceDescription;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.dkpro.core.api.io.ResourceCollectionReaderBase.PARAM_PATTERNS;
import static org.dkpro.core.api.io.ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCreationUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class ResourceCollectionReaderBaseTest
{
    @BeforeAll
    public static void before()
    {
        // Route logging through log4j
        System.setProperty("org.apache.uima.logger.class", "org.apache.uima.util.impl.Log4jLogger_impl");
    }
    
    @Test
    public void testClasspath()
        throws Exception
    {
        CollectionReader reader = createReader(DummyReader.class,
                ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION,
                "classpath*:/org/", ResourceCollectionReaderBase.PARAM_PATTERNS,
                new String[] { "[+]**/FileSetCollectionReaderBase.class",
                        "[-]**/ResourceCollectionReaderBase.class" });

        searchForResourceCollectionReaderBase(reader);
    }

    @Test
    public void testZip()
        throws Exception
    {
        CollectionReader reader = createReader(DummyReader.class,
                ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION,
                "jar:file:src/test/resources/testfiles.zip!",
                ResourceCollectionReaderBase.PARAM_PATTERNS, new String[] {
                        "[+]**/FileSetCollectionReaderBase.class",
                        "[-]**/ResourceCollectionReaderBase.class" });

        searchForResourceCollectionReaderBase(reader);
    }

    @Test
    public void testZip2()
        throws Exception
    {
        CollectionReader reader = createReader(DummyReader.class,
                ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION,
                "jar:file:src/test/resources/testfiles.zip",
                ResourceCollectionReaderBase.PARAM_PATTERNS, new String[] {
                        "[+]**/FileSetCollectionReaderBase.class",
                        "[-]**/ResourceCollectionReaderBase.class" });

        searchForResourceCollectionReaderBase(reader);
    }

    @Test
    public void testZip3()
        throws Exception
    {
        CollectionReader reader = createReader(DummyReader.class,
                ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION,
                "jar:file:src/test/resources/testfiles.zip",
                ResourceCollectionReaderBase.PARAM_PATTERNS, new String[] {
                        "[+]**/FileSetCollectionReaderBase.class",
                        "[-]test*/ResourceCollectionReaderBase.class" });

        searchForResourceCollectionReaderBase(reader);
    }

    @Test
    public void testZip4()
        throws Exception
    {
        URL url = new File("src/test/resources/testfiles.zip").toURI().toURL();
        String path = "jar:" + url.toString();
        CollectionReader reader = createReader(DummyReader.class,
                ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION, path,
                ResourceCollectionReaderBase.PARAM_PATTERNS, new String[] {
                        "[+]**/FileSetCollectionReaderBase.class",
                        "[-]test*/ResourceCollectionReaderBase.class" });

        searchForResourceCollectionReaderBase(reader);
    }

    @Test
    public void testZipNoPattern()
        throws Exception
    {
        CollectionReader reader = createReader(DummyReader.class,
                ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION,
                "jar:file:src/test/resources/testfiles.zip!/testfiles/FileSetCollectionReaderBase.class");

        searchForResourceCollectionReaderBase(reader);
    }

    @Disabled("This does not work because the underlying Spring component does not do remote.")
    @Test
    public void testRemoteZip()
        throws Exception
    {
        CollectionReader reader = createReader(
                DummyReader.class,
                ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION,
                "jar:http://search.maven.org/remotecontent?filepath=org/annolab/tt4j/org.annolab.tt4j/1.0.16/org.annolab.tt4j-1.0.16.jar!",
                ResourceCollectionReaderBase.PARAM_PATTERNS, new String[] {
                        "[+]**/FileSetCollectionReaderBase.class",
                        "[-]**/ResourceCollectionReaderBase.class" });

        searchForResourceCollectionReaderBase(reader);
    }

    @Test
    public void testFile()
        throws Exception
    {
        CollectionReader reader = createReader(DummyReader.class,
                ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION,
                "file:src/main/java/org/",
                ResourceCollectionReaderBase.PARAM_PATTERNS, new String[] {
                        "[+]**/FileSetCollectionReaderBase.java",
                        "[-]**/ResourceCollectionReaderBase.java" });

        searchForResourceCollectionReaderBase(reader);
    }

    @Test
    public void testFileNoPrefix()
        throws Exception
    {
        CollectionReader reader = createReader(DummyReader.class,
                ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION,
                "file:src/main/java/org/",
                ResourceCollectionReaderBase.PARAM_PATTERNS, new String[] {
                        "**/FileSetCollectionReaderBase.java",
                        "[-]**/ResourceCollectionReaderBase.java" });

        searchForResourceCollectionReaderBase(reader);
    }

    @Test
    public void testFileNoPattern1()
        throws Exception
    {
        CollectionReader reader = createReader(DummyReader.class,
                ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION,
                "file:src/main/java/org/**/FileSetCollectionReaderBase.java");

        searchForResourceCollectionReaderBase(reader);
    }

    @Test
    public void testFileNoPattern2()
        throws Exception
    {
        CollectionReader reader = createReader(DummyReader.class,
                ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION,
                "file:src/main/java/org/dkpro/core/api/io/FileSetCollectionReaderBase.java");

        searchForResourceCollectionReaderBase(reader);
    }

    @Test
    public void testFileNoPattern3()
        throws Exception
    {
        CollectionReader reader = createReader(DummyReader.class,
                ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION,
                "s*/main/java/org/dkpro/core/api/io/FileSetCollectionReaderBase.java");

        searchForResourceCollectionReaderBase(reader);
    }

    @Test
    public void testFileNoPattern4()
        throws Exception
    {
        CollectionReader reader = createReader(DummyReader.class,
                PARAM_SOURCE_LOCATION,
                "file:s*/main/java/org/dkpro/core/api/io/FileSetCollectionReaderBase.java");

        searchForResourceCollectionReaderBase(reader);
    }

    @Test
    public void testFileNoSource()
        throws Exception
    {
        CollectionReader reader = createReader(DummyReader.class,
                PARAM_PATTERNS, "src/main/java/org/**/FileSetCollectionReaderBase.java");

        searchForResourceCollectionReaderBase(reader);
    }
    
    @Test
    public void testBrokenPattern()
        throws Exception
    {
        assertThatExceptionOfType(ResourceInitializationException.class).isThrownBy(() -> {
            CollectionReader reader = createReader(DummyReader.class,
                    PARAM_SOURCE_LOCATION, "file:src/main/java/org/",
                    PARAM_PATTERNS, new String[] { "[?]**/FileSetCollectionReaderBase.java" });
            searchForResourceCollectionReaderBase(reader);
        });
    }

    @Test
    public void testExternalLoaderLocator()
        throws Exception
    {
        ExternalResourceDescription locator = createResourceDescription(
                ResourceLoaderLocator.class);
        CollectionReader reader = createReader(DummyReader.class,
                ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION,
                "file:src/main/java/org/",
                ResourceCollectionReaderBase.PARAM_PATTERNS, new String[] {
                        "[+]**/FileSetCollectionReaderBase.java",
                        "[-]**/ResourceCollectionReaderBase.java" },
                ResourceCollectionReaderBase.KEY_RESOURCE_RESOLVER, locator);

        searchForResourceCollectionReaderBase(reader);
    }

    public void searchForResourceCollectionReaderBase(CollectionReader aReader)
        throws Exception
    {
        String goodNeedle = "FileSetCollectionReaderBase";
        String badNeedle = "ResourceCollectionReaderBase";

        boolean found = false;
        CAS cas = CasCreationUtils.createCas(aReader.getProcessingResourceMetaData());
        while (aReader.hasNext()) {
            aReader.getNext(cas);
            DocumentMetaData meta = DocumentMetaData.get(cas);
            System.out.printf("Found  : [%s]%n", meta.getDocumentUri());
            System.out.printf("  Base : [%s]%n", meta.getDocumentBaseUri());
            System.out.printf("  ColID: [%s]%n", meta.getCollectionId());
            System.out.printf("  DocID: [%s]%n", meta.getDocumentId());
            System.out.println();
            
            assertTrue(meta.getDocumentBaseUri().length() == 0
                    || meta.getDocumentBaseUri().endsWith("/"));
            
            if (meta.getDocumentUri().contains(goodNeedle)) {
                found = true;
                break;
            }
            if (meta.getDocumentUri().contains(badNeedle)) {
                fail("Bad needle [" + badNeedle + "] found even though it is excluded...");
            }
            cas.reset();
        }
        cas.release();

        assertTrue(found, "Good needle [" + goodNeedle + "] not found...");
    }

    public static final class DummyReader
        extends ResourceCollectionReaderBase
    {
        @Override
        public void getNext(CAS aCAS)
            throws IOException, CollectionException
        {
            Resource res = nextFile();
            initCas(aCAS, res);
        }

        @Override
        public void close()
            throws IOException
        {
            // Ignore
        }
    }
}
