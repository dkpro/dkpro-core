/*******************************************************************************
 * Copyright 2011
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.tei;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.imscwb.ImsCwbWriter;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextWriter;

public class TeiReaderTest
{
    @Test
    public void digibibTest()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                TeiReader.class,
                TeiReader.PARAM_OMIT_IGNORABLE_WHITESPACE, true,
                TeiReader.PARAM_LANGUAGE, "de",
                TeiReader.PARAM_SOURCE_LOCATION, "classpath:/digibib",
                TeiReader.PARAM_PATTERNS, new String[] { "[+]*.xml" });

        AnalysisEngine writer = createEngine(TextWriter.class,
        		TextWriter.PARAM_USE_DOCUMENT_ID, true,
        		TextWriter.PARAM_TARGET_LOCATION, "target/digibibTest/");

        Map<String, Integer> actualSizes = new LinkedHashMap<String, Integer>();
        for (JCas jcas : new JCasIterable(reader)) {
        	DocumentMetaData meta = DocumentMetaData.get(jcas);
        	String text = jcas.getDocumentText();
        	System.out.printf("%s - %d%n", meta.getDocumentId(), text.length());
			actualSizes.put(meta.getDocumentId(), text.length());

			writer.process(jcas);
        }

        Map<String, Integer> expectedSizes = new LinkedHashMap<String, Integer>();
        expectedSizes.put("Literatur-Balde,-Jacob.xml#1", 152);
        expectedSizes.put("Literatur-Balde,-Jacob.xml#2", 14378);
        expectedSizes.put("Literatur-Balde,-Jacob.xml#3", 532);
        expectedSizes.put("Literatur-Balde,-Jacob.xml#4", 1322);
        expectedSizes.put("Literatur-Balde,-Jacob.xml#5", 26588);
        expectedSizes.put("Literatur-Besser,-Johann-von.xml#1", 279);
        expectedSizes.put("Literatur-Besser,-Johann-von.xml#2", 3846);
        expectedSizes.put("Literatur-Besser,-Johann-von.xml#3", 22363);
        expectedSizes.put("Literatur-Besser,-Johann-von.xml#4", 3576);
        expectedSizes.put("Literatur-Besser,-Johann-von.xml#5", 3369);
        expectedSizes.put("Literatur-Besser,-Johann-von.xml#6", 3903);
        expectedSizes.put("Literatur-Besser,-Johann-von.xml#7", 2035);
        expectedSizes.put("Literatur-Kobell,-Franz-von.xml#1", 164);
        expectedSizes.put("Literatur-Kobell,-Franz-von.xml#2", 2078);
        expectedSizes.put("Literatur-Kobell,-Franz-von.xml#3", 50730);
        expectedSizes.put("Literatur-Marcel,-Gabriel.xml#1", 52696);
        expectedSizes.put("Literatur-Meister,-Johann-Gottlieb.xml#1", 41418);

        assertEquals(expectedSizes, actualSizes);
    }

    @Test
    public void brownReaderTest()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                TeiReader.class,
                TeiReader.PARAM_LANGUAGE, "en",
                TeiReader.PARAM_SOURCE_LOCATION, "classpath:/brown_tei/",
                TeiReader.PARAM_PATTERNS, new String[] { "[+]*.xml" });

        String firstSentence = "The Fulton County Grand Jury said Friday an investigation of Atlanta's recent primary election produced `` no evidence '' that any irregularities took place . ";

        int i = 0;
        for (JCas jcas : new JCasIterable(reader)) {
        	DocumentMetaData meta = DocumentMetaData.get(jcas);
        	String text = jcas.getDocumentText();
        	System.out.printf("%s - %d%n", meta.getDocumentId(), text.length());

            if (i == 0) {
                assertEquals(2239, JCasUtil.select(jcas, Token.class).size());
                assertEquals(2239, JCasUtil.select(jcas, POS.class).size());
                assertEquals(98, JCasUtil.select(jcas, Sentence.class).size());

                assertEquals(firstSentence, JCasUtil.select(jcas, Sentence.class).iterator().next().getCoveredText());
            }
            i++;
        }

        assertEquals(3, i);
    }

    @Test
    public void brownReaderTest2()
        throws Exception
    {
    	File reference = new File("src/test/resources/brown_ims.txt");
    	File output = new File("target/test-output/brown_ims.txt");

        CollectionReaderDescription reader = createReaderDescription(
                TeiReader.class,
                TeiReader.PARAM_LANGUAGE, "en",
                TeiReader.PARAM_SOURCE_LOCATION, "classpath:/brown_tei/",
                TeiReader.PARAM_PATTERNS, new String[] { "[+]*.xml" });

        AnalysisEngineDescription writer = createEngineDescription(ImsCwbWriter.class,
        		ImsCwbWriter.PARAM_TARGET_LOCATION, output);

        SimplePipeline.runPipeline(reader, writer);

        assertEquals(
        		FileUtils.readFileToString(reference, "UTF-8"),
        		FileUtils.readFileToString(output, "UTF-8"));
    }

    @Test
    public void brownReaderTest3()
        throws Exception
    {
        File reference = new File("src/test/resources/brown_ims.gz.txt");
        File output = new File("target/test-output/brown_ims.gz.txt");

        CollectionReaderDescription reader = createReaderDescription(
                TeiReader.class,
                TeiReader.PARAM_LANGUAGE, "en",
                TeiReader.PARAM_SOURCE_LOCATION, "classpath:/brown_tei_gzip/",
                TeiReader.PARAM_PATTERNS, new String[] { "[+]*.xml.gz" });

        AnalysisEngineDescription writer = createEngineDescription(ImsCwbWriter.class,
                ImsCwbWriter.PARAM_TARGET_LOCATION, output);

        SimplePipeline.runPipeline(reader, writer);

        assertEquals(
                FileUtils.readFileToString(reference, "UTF-8"),
                FileUtils.readFileToString(output, "UTF-8"));
    }

    @Test
    public void brownReaderTest_noSentences()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
        		TeiReader.class,
        		TeiReader.PARAM_LANGUAGE, "en",
        		TeiReader.PARAM_SOURCE_LOCATION, "classpath:/brown_tei/",
        		TeiReader.PARAM_PATTERNS, new String[] { "[+]*.xml" },
                TeiReader.PARAM_READ_SENTENCE, false);

        int i = 0;
        for (JCas jcas : new JCasIterable(reader)) {
        	DocumentMetaData meta = DocumentMetaData.get(jcas);
        	String text = jcas.getDocumentText();
        	System.out.printf("%s - %d%n", meta.getDocumentId(), text.length());

            if (i == 0) {
                assertEquals(2239, JCasUtil.select(jcas, Token.class).size());
                assertEquals(2239, JCasUtil.select(jcas, POS.class).size());
                assertEquals(0, JCasUtil.select(jcas, Sentence.class).size());
            }
            i++;
        }

        assertEquals(3, i);
    }

    @Test
    public void brownReaderTest_noToken_noPOS()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
        		TeiReader.class,
        		TeiReader.PARAM_LANGUAGE, "en",
        		TeiReader.PARAM_SOURCE_LOCATION, "classpath:/brown_tei/",
        		TeiReader.PARAM_PATTERNS, new String[] { "[+]*.xml" },
                TeiReader.PARAM_READ_TOKEN, false,
                TeiReader.PARAM_READ_POS, false
        );

        int i = 0;
        for (JCas jcas : new JCasIterable(reader)) {
        	DocumentMetaData meta = DocumentMetaData.get(jcas);
        	String text = jcas.getDocumentText();
        	System.out.printf("%s - %d%n", meta.getDocumentId(), text.length());

            if (i == 0) {
                assertEquals(0, JCasUtil.select(jcas, Token.class).size());
                assertEquals(0, JCasUtil.select(jcas, POS.class).size());
                assertEquals(98, JCasUtil.select(jcas, Sentence.class).size());
            }
            i++;
        }

        assertEquals(3, i);
    }

    @Test(expected=IllegalStateException.class)
    public void brownReaderTest_expectedException()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
        		TeiReader.class,
        		TeiReader.PARAM_LANGUAGE, "en",
        		TeiReader.PARAM_SOURCE_LOCATION, "classpath:/brown_tei/",
        		TeiReader.PARAM_PATTERNS, new String[] { "[+]*.xml" },
                TeiReader.PARAM_READ_POS, true,
                TeiReader.PARAM_READ_TOKEN, false);

        for (JCas jcas : new JCasIterable(reader)) {
            // should never get here
            System.out.println(jcas.getDocumentText());
        }
    }
}
