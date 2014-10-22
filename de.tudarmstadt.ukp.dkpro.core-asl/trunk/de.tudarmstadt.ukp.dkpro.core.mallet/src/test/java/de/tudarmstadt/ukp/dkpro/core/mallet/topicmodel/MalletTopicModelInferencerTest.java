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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
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
    private static final String MODEL_FILE = "target/mallet/model";
    private static final String CAS_DIR = "src/test/resources/txt";
    private static final String CAS_FILE_PATTERN = "[+]*.txt";

    private static final int N_TOPICS = 10;
    private static final int N_ITERATIONS = 50;
    private static final boolean USE_LEMMAS = false;
    private static final String LANGUAGE = "en";

    @Before
    public void setUp()
    {
        /* Generate model */
        try {
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
        }
        catch (UIMAException | IOException e) {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }

    }

    @Test
    public void testInferencer()
    {
        int expectedAssignments = 0;

        try {
            CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                    TextReader.PARAM_SOURCE_LOCATION, CAS_DIR,
                    TextReader.PARAM_PATTERNS, CAS_FILE_PATTERN,
                    TextReader.PARAM_LANGUAGE, LANGUAGE);
            AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

            AnalysisEngineDescription inferencer = createEngineDescription(
                    MalletTopicModelInferencer.class,
                    MalletTopicModelInferencer.PARAM_MODEL_LOCATION, MODEL_FILE,
                    MalletTopicModelInferencer.PARAM_USE_LEMMA, USE_LEMMAS);
            // TODO: extend test for PARAM_MIN_TOPIC_PROB; adapt expectedAssignments

            for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter, inferencer)) {
                for (TopicDistribution td : select(jcas, TopicDistribution.class)) {
//                    System.err.println("Topic Proportions: ");
//                    System.err.println(td.getTopicProportions());
//                    System.err.println("Topic Assignment: ");
//                    System.err.println(td.getTopicAssignment());

                    assertEquals(expectedAssignments, td.getTopicAssignment().size());
                }
            }
        }
        catch (UIMAException e) {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
    }
}
