/*******************************************************************************
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.core.mallet.topicmodel.io;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.mallet.topicmodel.MalletTopicModelEstimator;
import de.tudarmstadt.ukp.dkpro.core.mallet.topicmodel.MalletTopicModelInferencer;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

/**
 *
 * @author Carsten Schnober
 *
 */
public class MalletTopicModelFileWriterTest
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
    public void test()
        throws UIMAException, IOException
    {
        File targetFile = new File("target/topics.txt");
        targetFile.deleteOnExit();

        int expectedLines = 2;
        String expectedLine0Regex = "dummy1.txt\t(0\\.[0-9]{4},){9}0\\.[0-9]{4}";
        String expectedLine1Regex = "dummy2.txt\t(0\\.[0-9]{4},){9}0\\.[0-9]{4}";

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, CAS_DIR,
                TextReader.PARAM_PATTERNS, CAS_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, LANGUAGE);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription inferencer = createEngineDescription(
                MalletTopicModelInferencer.class,
                MalletTopicModelInferencer.PARAM_MODEL_LOCATION, MODEL_FILE,
                MalletTopicModelInferencer.PARAM_USE_LEMMA, USE_LEMMAS);

        AnalysisEngineDescription writer = createEngineDescription(
                MalletTopicModelFileWriter.class,
                MalletTopicModelFileWriter.PARAM_TARGET_LOCATION, targetFile);

        SimplePipeline.runPipeline(reader, segmenter, inferencer, writer);
        List<String> lines = FileUtils.readLines(targetFile);
        assertTrue(lines.get(0).matches(expectedLine0Regex));
        assertTrue(lines.get(1).matches(expectedLine1Regex));
        assertEquals(expectedLines, lines.size());
    }
}
