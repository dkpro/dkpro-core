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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Test;

import cc.mallet.topics.ParallelTopicModel;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class MalletTopicModelEstimatorTest
{
    private static final int N_THREADS = 4;
    private static final String MODEL_FILE = "target/mallet/model";
    private static final String CAS_DIR = "src/test/resources/txt";
    private static final String CAS_FILE_PATTERN = "[+]*.txt";

    @Test
    public void testParallel()
    {
        int nTopics = 10;
        int nIterations = 50;
        boolean useLemmas = false;
        String language = "en";

        try {
            CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                    TextReader.PARAM_SOURCE_LOCATION, CAS_DIR,
                    TextReader.PARAM_PATTERNS, CAS_FILE_PATTERN,
                    TextReader.PARAM_LANGUAGE, language);
            AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

            AnalysisEngineDescription estimator = createEngineDescription(
                    MalletTopicModelEstimator.class,
                    MalletTopicModelEstimator.PARAM_N_THREADS, N_THREADS,
                    MalletTopicModelEstimator.PARAM_TARGET_LOCATION, MODEL_FILE,
                    MalletTopicModelEstimator.PARAM_N_ITERATIONS, nIterations,
                    MalletTopicModelEstimator.PARAM_N_TOPICS, nTopics,
                    MalletTopicModelEstimator.PARAM_USE_LEMMA, useLemmas);
            SimplePipeline.runPipeline(reader, segmenter, estimator);
        }
        catch (UIMAException | IOException e) {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
        try {
            ParallelTopicModel model = ParallelTopicModel.read(new File(MODEL_FILE));
            assertEquals(nTopics, model.getNumTopics());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
    }
}
