/*
 * Copyright 2017
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
package org.dkpro.core.mallet.lda;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.dkpro.core.io.text.TextReader;
import org.dkpro.core.tokit.BreakIteratorSegmenter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import cc.mallet.topics.ParallelTopicModel;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;

public class MalletLdaTopicModelTrainerTest
{
    private static final String TXT_DIR = "src/test/resources/txt";
    private static final String TXT_FILE_PATTERN = "[+]*.txt";

    @org.junit.jupiter.api.Test
    public void testEstimator(@TempDir File tempDir)
            throws Exception
    {
        File modelFile = new File(tempDir, "model");

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
                MalletLdaTopicModelTrainer.PARAM_TARGET_LOCATION, modelFile,
                MalletLdaTopicModelTrainer.PARAM_N_ITERATIONS, nIterations,
                MalletLdaTopicModelTrainer.PARAM_N_TOPICS, nTopics);
        SimplePipeline.runPipeline(reader, segmenter, estimator);
        // end::example[]

        assertTrue(modelFile.exists());
        ParallelTopicModel model = ParallelTopicModel.read(modelFile);
        assertEquals(nTopics, model.getNumTopics());
    }

    @Test
    public void testEstimatorSentence(@TempDir File tempDir)
            throws Exception
    {
        File modelFile = new File(tempDir, "model");
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
                MalletLdaTopicModelTrainer.PARAM_TARGET_LOCATION, modelFile,
                MalletLdaTopicModelTrainer.PARAM_N_ITERATIONS, nIterations,
                MalletLdaTopicModelTrainer.PARAM_N_TOPICS, nTopics,
                MalletLdaTopicModelTrainer.PARAM_COVERING_ANNOTATION_TYPE, entity);
        SimplePipeline.runPipeline(reader, segmenter, estimator);

        assertTrue(modelFile.exists());
        ParallelTopicModel model = ParallelTopicModel.read(modelFile);
        assertEquals(nTopics, model.getNumTopics());
    }

    @Test
    public void testEstimatorAlphaBeta(@TempDir File tempDir)
            throws Exception
    {
        File modelFile = new File(tempDir, "model");
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
                MalletLdaTopicModelTrainer.PARAM_TARGET_LOCATION, modelFile,
                MalletLdaTopicModelTrainer.PARAM_N_ITERATIONS, nIterations,
                MalletLdaTopicModelTrainer.PARAM_N_TOPICS, nTopics,
                MalletLdaTopicModelTrainer.PARAM_ALPHA_SUM, alpha,
                MalletLdaTopicModelTrainer.PARAM_BETA, beta);
        SimplePipeline.runPipeline(reader, segmenter, estimator);

        assertTrue(modelFile.exists());
        ParallelTopicModel model = ParallelTopicModel.read(modelFile);
        assertEquals(nTopics, model.getNumTopics());
    }
}
