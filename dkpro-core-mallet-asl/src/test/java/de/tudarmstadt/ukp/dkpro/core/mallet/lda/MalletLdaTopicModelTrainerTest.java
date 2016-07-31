/*
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.mallet.lda;

import cc.mallet.topics.ParallelTopicModel;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Test;

import java.io.File;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MalletLdaTopicModelTrainerTest
{
    private static final File MODEL_FILE = new File("target/mallet/model");
    private static final String TXT_DIR = "src/test/resources/txt";
    private static final String TXT_FILE_PATTERN = "[+]*.txt";

    @Test
    public void testEstimator()
            throws Exception
    {
        // tag::example[]
        int nTopics = 10;
        int nIterations = 50;
        String language = "en";

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, TXT_DIR,
                TextReader.PARAM_PATTERNS, TXT_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, language);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription estimator = createEngineDescription(
                MalletLdaTopicModelTrainer.class,
                MalletLdaTopicModelTrainer.PARAM_TARGET_LOCATION, MODEL_FILE,
                MalletLdaTopicModelTrainer.PARAM_N_ITERATIONS, nIterations,
                MalletLdaTopicModelTrainer.PARAM_N_TOPICS, nTopics);
        SimplePipeline.runPipeline(reader, segmenter, estimator);
        // end::example[]

        assertTrue(MODEL_FILE.exists());
        ParallelTopicModel model = ParallelTopicModel.read(MODEL_FILE);
        assertEquals(nTopics, model.getNumTopics());
        MODEL_FILE.delete();
    }

    @Test
    public void testEstimatorSentence()
            throws Exception
    {
        int nTopics = 10;
        int nIterations = 50;
        String language = "en";
        String entity = Sentence.class.getName();

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, TXT_DIR,
                TextReader.PARAM_PATTERNS, TXT_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, language);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription estimator = createEngineDescription(
                MalletLdaTopicModelTrainer.class,
                MalletLdaTopicModelTrainer.PARAM_TARGET_LOCATION, MODEL_FILE,
                MalletLdaTopicModelTrainer.PARAM_N_ITERATIONS, nIterations,
                MalletLdaTopicModelTrainer.PARAM_N_TOPICS, nTopics,
                MalletLdaTopicModelTrainer.PARAM_COVERING_ANNOTATION_TYPE, entity);
        SimplePipeline.runPipeline(reader, segmenter, estimator);

        assertTrue(MODEL_FILE.exists());
        ParallelTopicModel model = ParallelTopicModel.read(MODEL_FILE);
        assertEquals(nTopics, model.getNumTopics());
        MODEL_FILE.delete();
    }

    @Test
    public void testEstimatorAlphaBeta()
            throws Exception
    {
        int nTopics = 10;
        int nIterations = 50;
        float alpha = nTopics / 50.0f;
        float beta = 0.01f;
        String language = "en";

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, TXT_DIR,
                TextReader.PARAM_PATTERNS, TXT_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, language);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription estimator = createEngineDescription(
                MalletLdaTopicModelTrainer.class,
                MalletLdaTopicModelTrainer.PARAM_TARGET_LOCATION, MODEL_FILE,
                MalletLdaTopicModelTrainer.PARAM_N_ITERATIONS, nIterations,
                MalletLdaTopicModelTrainer.PARAM_N_TOPICS, nTopics,
                MalletLdaTopicModelTrainer.PARAM_ALPHA_SUM, alpha,
                MalletLdaTopicModelTrainer.PARAM_BETA, beta);
        SimplePipeline.runPipeline(reader, segmenter, estimator);

        assertTrue(MODEL_FILE.exists());
        ParallelTopicModel model = ParallelTopicModel.read(MODEL_FILE);
        assertEquals(nTopics, model.getNumTopics());
        MODEL_FILE.delete();
    }
}
