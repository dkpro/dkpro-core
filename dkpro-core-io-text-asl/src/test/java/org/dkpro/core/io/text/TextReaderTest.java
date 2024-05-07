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
package org.dkpro.core.io.text;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.CasUtil.select;
import static org.assertj.core.api.Assertions.assertThat;
import static org.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.CasUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class TextReaderTest
{
    private static final String FILE1 = "test1.txt";
    private static final String FILE2 = "test2.txt";
    private static final List<String> FILES = Arrays.asList(FILE1, FILE2);

    @Test
    void thatBomIsNotIncludedInDocumentText(@TempDir Path aTemp)  throws Exception {
        var documentText = "text";
        var tempFile = aTemp.resolve("test.txt");

        try (var os = Files.newOutputStream(tempFile)) {
            // Write BOM
            os.write(0xEF);
            os.write(0xBB);
            os.write(0xBF);
            // Write text
            os.write(documentText.getBytes(UTF_8));
        }
        
        var reader = createReader( //
                TextReader.class, //
                TextReader.PARAM_SOURCE_LOCATION, tempFile.toString());
        
        var jcas = JCasFactory.createJCas();
        reader.getNext(jcas.getCasImpl());
        
        assertThat(jcas.getDocumentText()).isEqualTo(documentText);
    }
    
    @Test
    public void fileSystemReaderTest() throws Exception
    {
        var reader = createReaderDescription( //
                TextReader.class, //
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/texts", //
                TextReader.PARAM_PATTERNS, "[+]*.txt");

        for (var jcas : new JCasIterable(reader)) {
            var md = DocumentMetaData.get(jcas);
            dumpMetaData(md);

            assertEquals(1,
                    CasUtil.select(jcas.getCas(), jcas.getDocumentAnnotationFs().getType()).size());
            assertTrue(FILES.contains(md.getDocumentId()));

            assertTrue(!FILE1.equals(md.getDocumentId())
                    || ("This is a test.".equals(jcas.getDocumentText()) && 15 == md.getEnd()));

            assertTrue(!FILE2.equals(md.getDocumentId())
                    || "This is a second test.".equals(jcas.getDocumentText()));
        }
    }

    @Test
    public void fileSystemReaderAbsolutePathTest() throws Exception
    {
        var reader = createReaderDescription( //
                TextReader.class, //
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/texts",
                TextReader.PARAM_PATTERNS, INCLUDE_PREFIX + "*.txt");

        for (var jcas : new JCasIterable(reader)) {
            var md = DocumentMetaData.get(jcas);
            dumpMetaData(md);

            assertEquals(1, select(jcas.getCas(), jcas.getDocumentAnnotationFs().getType()).size());

            assertTrue(FILES.contains(md.getDocumentId()));

            assertTrue(!FILE1.equals(md.getDocumentId())
                    || ("This is a test.".equals(jcas.getDocumentText()) && 15 == md.getEnd()));

            assertTrue(!FILE2.equals(md.getDocumentId())
                    || "This is a second test.".equals(jcas.getDocumentText()));
        }
    }

    @Test
    public void fileSystemReaderTest3() throws Exception
    {
        var reader = createReaderDescription( //
                TextReader.class, //
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/name with space", //
                TextReader.PARAM_PATTERNS, new String[] { INCLUDE_PREFIX + "*.txt" });

        for (var jcas : new JCasIterable(reader)) {
            var md = DocumentMetaData.get(jcas);
            dumpMetaData(md);

            assertEquals(1, select(jcas.getCas(), jcas.getDocumentAnnotationFs().getType()).size());

            assertTrue(FILES.contains(md.getDocumentId()));

            assertTrue(!FILE1.equals(md.getDocumentId())
                    || ("This is a test.".equals(jcas.getDocumentText()) && 15 == md.getEnd()));

            assertTrue(!FILE2.equals(md.getDocumentId())
                    || "This is a second test.".equals(jcas.getDocumentText()));
        }
    }

    @Test
    public void fileSystemReaderTest2() throws Exception
    {
        var reader = createReaderDescription( //
                TextReader.class, //
                TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/texts", //
                TextReader.PARAM_PATTERNS, new String[0]);

        for (var jcas : new JCasIterable(reader)) {
            var md = DocumentMetaData.get(jcas);
            dumpMetaData(md);

            assertEquals(1, select(jcas.getCas(), jcas.getDocumentAnnotationFs().getType()).size());

            assertTrue(FILES.contains(md.getDocumentId()));

            assertTrue(!FILE1.equals(md.getDocumentId())
                    || "This is a test.".equals(jcas.getDocumentText()));

            assertTrue(!FILE2.equals(md.getDocumentId())
                    || "This is a second test.".equals(jcas.getDocumentText()));
        }
    }

    @Test
    public void fileSystemReaderTest4() throws Exception
    {
        var reader = createReaderDescription( //
                TextReader.class, //
                TextReader.PARAM_SOURCE_LOCATION, "classpath:texts", //
                TextReader.PARAM_PATTERNS, new String[0]);

        for (var jcas : new JCasIterable(reader)) {
            var md = DocumentMetaData.get(jcas);
            dumpMetaData(md);

            assertEquals(1, select(jcas.getCas(), jcas.getDocumentAnnotationFs().getType()).size());

            assertTrue(FILES.contains(md.getDocumentId()));

            assertTrue(!FILE1.equals(md.getDocumentId())
                    || "This is a test.".equals(jcas.getDocumentText()));

            assertTrue(!FILE2.equals(md.getDocumentId())
                    || "This is a second test.".equals(jcas.getDocumentText()));
        }
    }

    private void dumpMetaData(final DocumentMetaData aMetaData)
    {
        System.out.println("Collection ID: " + aMetaData.getCollectionId());
        System.out.println("ID           : " + aMetaData.getDocumentId());
        System.out.println("Base URI     : " + aMetaData.getDocumentBaseUri());
        System.out.println("URI          : " + aMetaData.getDocumentUri());
    }
}
