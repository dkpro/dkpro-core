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
package org.dkpro.core.frequency.tfidf;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.frequency.tfidf.TfIdfAnnotator.WeightingModeIdf;
import org.dkpro.core.frequency.tfidf.TfIdfAnnotator.WeightingModeTf;
import org.dkpro.core.io.text.TextReader;
import org.dkpro.core.tokit.BreakIteratorSegmenter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.tfidf.type.Tfidf;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TfIdfAnnotatorTest
{
    // assertEquals on doubles needs an epsilon
    protected static final double EPSILON = 0.000001;

    private final static String CONSUMER_TEST_DATA_PATH = "src/test/resources/consumer/";

    protected File model;

    @BeforeEach
    public void buildModel(@TempDir File tempDir)
        throws Exception
    {
        model = new File(tempDir, "model");

        // write the model
        CollectionReaderDescription reader = createReaderDescription( //
                TextReader.class, //
                TextReader.PARAM_SOURCE_LOCATION, CONSUMER_TEST_DATA_PATH,  //
                TextReader.PARAM_PATTERNS, INCLUDE_PREFIX + "*.txt");

        AnalysisEngineDescription aggregate = createEngineDescription(
                createEngineDescription( //
                        BreakIteratorSegmenter.class), //
                createEngineDescription( //
                        TfIdfWriter.class,  //
                        TfIdfWriter.PARAM_FEATURE_PATH, Token.class,  //
                        TfIdfWriter.PARAM_TARGET_LOCATION, model));

        SimplePipeline.runPipeline(reader, aggregate);
    }

    @Test
    public void tfidfTest_normal_constantOne()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription( //
                TextReader.class, //
                TextReader.PARAM_SOURCE_LOCATION, CONSUMER_TEST_DATA_PATH,  //
                TextReader.PARAM_PATTERNS, INCLUDE_PREFIX + "*.txt");

        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription tfidfAnnotator = createEngineDescription(
                TfIdfAnnotator.class, //
                TfIdfAnnotator.PARAM_FEATURE_PATH, Token.class, //
                TfIdfAnnotator.PARAM_TFDF_PATH, model,  //
                TfIdfAnnotator.PARAM_TF_MODE, WeightingModeTf.NORMAL,  //
                TfIdfAnnotator.PARAM_IDF_MODE, WeightingModeIdf.CONSTANT_ONE);

        Map<String, Double> expectedDoc1 = new HashMap<String, Double>();
        expectedDoc1.put("example", 1.0);
        expectedDoc1.put("sentence", 1.0);
        expectedDoc1.put("funny", 1.0);

        Map<String, Double> expectedDoc2 = new HashMap<String, Double>();
        expectedDoc2.put("example", 2.0);
        expectedDoc2.put("sentence", 1.0);

        for (JCas jcas : new JCasIterable(reader, segmenter, tfidfAnnotator)) {
            testIt(jcas, expectedDoc1, expectedDoc2);
        }
    }

    @Test
    public void tfidfTest_binary_binary()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription( //
                TextReader.class, //
                TextReader.PARAM_SOURCE_LOCATION, CONSUMER_TEST_DATA_PATH,  //
                TextReader.PARAM_PATTERNS, INCLUDE_PREFIX + "*.txt");

        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription tfidfAnnotator = createEngineDescription( //
                TfIdfAnnotator.class, //
                TfIdfAnnotator.PARAM_FEATURE_PATH, Token.class, //
                TfIdfAnnotator.PARAM_TFDF_PATH, model,  //
                TfIdfAnnotator.PARAM_TF_MODE, WeightingModeTf.BINARY, //
                TfIdfAnnotator.PARAM_IDF_MODE, WeightingModeIdf.BINARY);

        Map<String, Double> expectedDoc1 = new HashMap<String, Double>();
        expectedDoc1.put("example", 1.0);
        expectedDoc1.put("sentence", 1.0);
        expectedDoc1.put("funny", 1.0);

        Map<String, Double> expectedDoc2 = new HashMap<String, Double>();
        expectedDoc2.put("example", 1.0);
        expectedDoc2.put("sentence", 1.0);

        for (JCas jcas : new JCasIterable(reader, segmenter, tfidfAnnotator)) {
            testIt(jcas, expectedDoc1, expectedDoc2);
        }
    }

    @Test
    public void tfidfTest_normal_log()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription( //
                TextReader.class, //
                TextReader.PARAM_SOURCE_LOCATION, CONSUMER_TEST_DATA_PATH,  //
                TextReader.PARAM_PATTERNS, INCLUDE_PREFIX + "*.txt");

        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription tfidfAnnotator = createEngineDescription( //
                TfIdfAnnotator.class, //
                TfIdfAnnotator.PARAM_FEATURE_PATH, Token.class, //
                TfIdfAnnotator.PARAM_TFDF_PATH, model,  //
                TfIdfAnnotator.PARAM_TF_MODE, WeightingModeTf.NORMAL,  //
                TfIdfAnnotator.PARAM_IDF_MODE, WeightingModeIdf.LOG);

        Map<String, Double> expectedDoc1 = new HashMap<String, Double>();
        expectedDoc1.put("example", 0.0);
        expectedDoc1.put("sentence", 0.0);
        expectedDoc1.put("funny", Math.log(2));

        Map<String, Double> expectedDoc2 = new HashMap<String, Double>();
        expectedDoc2.put("example", 0.0);
        expectedDoc2.put("sentence", 0.0);

        for (JCas jcas : new JCasIterable(reader, segmenter, tfidfAnnotator)) {
            testIt(jcas, expectedDoc1, expectedDoc2);
        }
    }

    private void testIt(JCas jcas, Map<String, Double> expectedDoc1,
            Map<String, Double> expectedDoc2)
    {
        if (DocumentMetaData.get(jcas).getDocumentTitle().equals("test1.txt")) {
            int i = 0;
            for (Tfidf tfidf : select(jcas, Tfidf.class)) {
                assertEquals(expectedDoc1.get(tfidf.getTerm()).doubleValue(),
                        tfidf.getTfidfValue(), EPSILON, tfidf.getTerm());
                i++;
            }
            assertEquals(3, i);
        }
        else if (DocumentMetaData.get(jcas).getDocumentTitle().equals("test2.txt")) {
            int i = 0;
            for (Tfidf tfidf : select(jcas, Tfidf.class)) {
                assertEquals(expectedDoc2.get(tfidf.getTerm()).doubleValue(),
                        tfidf.getTfidfValue(), EPSILON, tfidf.getTerm());
                i++;
            }
            assertEquals(3, i);
        }
        else {
            fail("There should be no other documents in that directory.");
        }
    }
}
