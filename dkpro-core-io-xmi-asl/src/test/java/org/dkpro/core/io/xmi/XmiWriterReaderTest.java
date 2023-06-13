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
package org.dkpro.core.io.xmi;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.factory.JCasFactory.createText;
import static org.apache.uima.fit.factory.TypeSystemDescriptionFactory.createTypeSystemDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Collections;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.CasCreationUtils;
import org.dkpro.core.api.io.ResourceCollectionReaderBase;
import org.dkpro.core.io.text.TextReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class XmiWriterReaderTest
{
    private @TempDir File testFolder;

    @Test
    public void thatWritingAndReadingXML1_1works() throws Exception
    {
        File outputFolder = testFolder;
        
        JCas outDocument = createText(contentOf(new File("src/test/resources/texts/chinese.txt"), UTF_8), "zh");
        
        DocumentMetaData dmd = DocumentMetaData.create(outDocument);
        dmd.setDocumentId("output.xmi");
        
        AnalysisEngine writer = createEngine(XmiWriter.class, 
                XmiWriter.PARAM_TARGET_LOCATION, outputFolder,
                XmiWriter.PARAM_STRIP_EXTENSION, true,
                XmiWriter.PARAM_VERSION, "1.1",
                XmiWriter.PARAM_OVERWRITE, true);
        
        writer.process(outDocument);
        
        JCas inDocument = JCasFactory.createJCas();
        
        CollectionReader reader = createReader(XmiReader.class, 
                XmiReader.PARAM_SOURCE_LOCATION, new File(outputFolder, "output.xmi"));
        reader.getNext(inDocument.getCas());
        
        assertThat(outDocument.getDocumentText()).isEqualTo(inDocument.getDocumentText());
    }
    
    @Test
    public void thatWritingAndReadingXML1_0ControlCharactersWorks() throws Exception
    {
        System.out.println(Collections.list(
                getClass().getClassLoader().getResources("META-INF/services/org.xml.sax.driver")));
        XMLReader r = XMLReaderFactory.createXMLReader();
        System.out.printf("http://xml.org/sax/features/xml-1.1: %s%n",
                r.getFeature("http://xml.org/sax/features/xml-1.1"));
        
        File outputFolder = testFolder;

        StringBuilder text = new StringBuilder();
        for (char ch = 0; ch < 0xFFFE; ch++) {
            if (
                    // These are rejected already by UIMA during serialization
                    (0x0000 <= ch && ch < 0x0009) ||
                    (0x000B <= ch && ch < 0x000D) ||
                    (0x000E <= ch && ch < 0x0020) ||
                    (0xD800 <= ch && ch < 0xE000)
            ) {
                text.append(" ");
            }
            else {
                text.append(ch);
            }
        }
        
        JCas outDocument = createText(text.toString(), "en");
        
        DocumentMetaData dmd = DocumentMetaData.create(outDocument);
        dmd.setDocumentId("output.xmi");
        
        AnalysisEngine writer = createEngine(XmiWriter.class, 
                XmiWriter.PARAM_TARGET_LOCATION, outputFolder,
                XmiWriter.PARAM_STRIP_EXTENSION, true,
                XmiWriter.PARAM_VERSION, "1.0",
                XmiWriter.PARAM_OVERWRITE, true);
        
        writer.process(outDocument);
        
        JCas inDocument = JCasFactory.createJCas();
        
        CollectionReader reader = createReader(XmiReader.class, 
                XmiReader.PARAM_SOURCE_LOCATION, new File(outputFolder, "output.xmi"));
        reader.getNext(inDocument.getCas());
        
        String expected = inDocument.getDocumentText();
        String actual = outDocument.getDocumentText();
        
        assertThat(actual.length())
                .isEqualTo(expected.length());
        
        for (int i = 0; i < expected.length(); i++) {
            if (expected.charAt(i) != actual.charAt(i)) {
                System.out.printf("[U+%04X] %d does not match expected %d%n", i,
                        (int) actual.charAt(i), (int) expected.charAt(i));
            }
        }
        
        assertThat(outDocument.getDocumentText()).isEqualTo(inDocument.getDocumentText());
    }
    @Test
    public void thatWritingAndReadingXML1_1ControlCharactersWorks() throws Exception
    {
        System.out.println(Collections.list(
                getClass().getClassLoader().getResources("META-INF/services/org.xml.sax.driver")));
        XMLReader r = XMLReaderFactory.createXMLReader();
        System.out.printf("http://xml.org/sax/features/xml-1.1: %s%n",
                r.getFeature("http://xml.org/sax/features/xml-1.1"));
        
        File outputFolder = testFolder;

        StringBuilder text = new StringBuilder();
        for (char ch = 0; ch < 0xFFFE; ch++) {
            if (
                    // These are rejected already by UIMA during serialization
                    ch == 0x0000 ||
                    (0xD800 <= ch && ch < 0xE000) ||
                    // These are rejected during parsing by the XML parser
                    (0x007f <= ch && ch <= 0x0084) ||
                    (0x0086 <= ch && ch <= 0x009F) ||
                    // These are normalized to " "
                    ch == 0x0085 || ch == 0x2028
            ) {
                text.append(" ");
            }
            else {
                text.append(ch);
            }
        }
        
        JCas outDocument = createText(text.toString(), "en");
        
        DocumentMetaData dmd = DocumentMetaData.create(outDocument);
        dmd.setDocumentId("output.xmi");
        
        AnalysisEngine writer = createEngine(XmiWriter.class, 
                XmiWriter.PARAM_TARGET_LOCATION, outputFolder,
                XmiWriter.PARAM_STRIP_EXTENSION, true,
                XmiWriter.PARAM_VERSION, "1.1",
                XmiWriter.PARAM_OVERWRITE, true);
        
        writer.process(outDocument);
        
        JCas inDocument = JCasFactory.createJCas();
        
        CollectionReader reader = createReader(XmiReader.class, 
                XmiReader.PARAM_SOURCE_LOCATION, new File(outputFolder, "output.xmi"));
        reader.getNext(inDocument.getCas());
        
        String expected = inDocument.getDocumentText();
        String actual = outDocument.getDocumentText();
        
        assertThat(actual.length())
                .isEqualTo(expected.length());
        
        for (int i = 0; i < expected.length(); i++) {
            if (expected.charAt(i) != actual.charAt(i)) {
                System.out.printf("[U+%04X] %d does not match expected %d%n", i,
                        (int) actual.charAt(i), (int) expected.charAt(i));
            }
        }
        
        assertThat(outDocument.getDocumentText()).isEqualTo(inDocument.getDocumentText());
    }
    
    @Test
    public void test() throws Exception
    {
        write();
        read();
    }

    public void write() throws Exception
    {
        CollectionReader textReader = CollectionReaderFactory.createReader(
                TextReader.class,
                ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION, "src/test/resources/texts",
                ResourceCollectionReaderBase.PARAM_PATTERNS, "latin.txt",
                ResourceCollectionReaderBase.PARAM_LANGUAGE, "latin");

        AnalysisEngine xmiWriter = AnalysisEngineFactory.createEngine(
                XmiWriter.class,
                XmiWriter.PARAM_TARGET_LOCATION, testFolder);

        runPipeline(textReader, xmiWriter);

        assertTrue(new File(testFolder, "latin.txt.xmi").exists());
    }

    public void read() throws Exception
    {
        CollectionReader xmiReader = CollectionReaderFactory.createReader(
                XmiReader.class,
                ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION, testFolder,
                ResourceCollectionReaderBase.PARAM_PATTERNS, "*.xmi");

        CAS cas = CasCreationUtils.createCas(createTypeSystemDescription(), null, null);
        xmiReader.getNext(cas);

        String refText = contentOf(new File("src/test/resources/texts/latin.txt"), UTF_8);
        assertEquals(refText, cas.getDocumentText());
        assertEquals("latin", cas.getDocumentLanguage());
    }
}
