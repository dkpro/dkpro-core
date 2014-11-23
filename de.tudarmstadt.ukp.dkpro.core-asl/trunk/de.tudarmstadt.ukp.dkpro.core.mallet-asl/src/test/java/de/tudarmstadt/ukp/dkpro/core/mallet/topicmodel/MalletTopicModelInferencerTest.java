/*******************************************************************************
 * Copyright 2014
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

package de.tudarmstadt.ukp.dkpro.core.mallet.topicmodel;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

/**
 *
 * @author Carsten Schnober
 *
 */
public class MalletTopicModelInferencerTest
{
    private static final int N_THREADS = 4;
    private static final File MODEL_FILE = new File("target/mallet/model");
    private static final String CAS_DIR = "src/test/resources/txt";
    private static final String CAS_FILE_PATTERN = "[+]*.txt";

    private static final int N_TOPICS = 10;
    private static final int N_ITERATIONS = 50;
    private static final boolean USE_LEMMAS = false;
    private static final String LANGUAGE = "en";

    @Before
    public void setUp()
        throws UIMAException, IOException
    {
        /* Generate model */
        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, CAS_DIR,
                TextReader.PARAM_PATTERNS, CAS_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, LANGUAGE);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription estimator = createEngineDescription(
                MalletTopicModelEstimator.class,
                MalletTopicModelEstimator.PARAM_N_THREADS, N_THREADS,
                MalletTopicModelEstimator.PARAM_TARGET_LOCATION, MODEL_FILE,
                MalletTopicModelEstimator.PARAM_N_ITERATIONS, N_ITERATIONS,
                MalletTopicModelEstimator.PARAM_N_TOPICS, N_TOPICS,
                MalletTopicModelEstimator.PARAM_USE_LEMMA, USE_LEMMAS);
        SimplePipeline.runPipeline(reader, segmenter, estimator);

        MODEL_FILE.deleteOnExit();
    }

    @Test
    public void testInferencer()
        throws ResourceInitializationException
    {
        List<Double[]> allExpectedTopicProportions = new ArrayList<>(2);
        allExpectedTopicProportions.add(new Double[] { 0.0518783542039356, 0.13059033989266547,
                0.11449016100178891, 0.06618962432915922, 0.0518783542039356, 0.1037567084078712,
                0.11449016100178891, 0.148479427549195, 0.08944543828264759, 0.12880143112701253 });
        allExpectedTopicProportions
                .add(new Double[] { 0.13009708737864079, 0.09320388349514563, 0.05631067961165048,
                        0.08932038834951456, 0.11650485436893204, 0.1669902912621359,
                        0.06601941747572816, 0.08155339805825243, 0.1650485436893204,
                        0.03495145631067961 });

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, CAS_DIR,
                TextReader.PARAM_PATTERNS, CAS_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, LANGUAGE);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription inferencer = createEngineDescription(
                MalletTopicModelInferencer.class,
                MalletTopicModelInferencer.PARAM_MODEL_LOCATION, MODEL_FILE,
                MalletTopicModelInferencer.PARAM_USE_LEMMA, USE_LEMMAS);

        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter, inferencer)) {
            for (TopicDistribution td : select(jcas, TopicDistribution.class)) {
                for (int i = 0; i < td.getTopicProportions().toArray().length; i++) {
                    double proportion = td.getTopicProportions().toArray()[i];
                    assertTrue(proportion > 0.0);
                    assertTrue(proportion < 1.0);
                }
            }
        }
    }
}
