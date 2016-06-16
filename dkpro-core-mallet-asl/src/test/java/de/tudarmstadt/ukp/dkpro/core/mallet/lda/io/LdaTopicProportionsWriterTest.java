/*
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.core.mallet.lda.io;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.mallet.lda.LdaTopicModelEstimator;
import de.tudarmstadt.ukp.dkpro.core.mallet.lda.LdaTopicModelInferencer;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class LdaTopicProportionsWriterTest
{
    private static final File MODEL_FILE = new File("target/mallet/model");
    private static final String CAS_DIR = "src/test/resources/txt";
    private static final String CAS_FILE_PATTERN = "[+]*.txt";

    private static final int N_TOPICS = 10;
    private static final int N_ITERATIONS = 50;
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
                LdaTopicModelEstimator.class,
                LdaTopicModelEstimator.PARAM_TARGET_LOCATION, MODEL_FILE,
                LdaTopicModelEstimator.PARAM_N_ITERATIONS, N_ITERATIONS,
                LdaTopicModelEstimator.PARAM_N_TOPICS, N_TOPICS);
        SimplePipeline.runPipeline(reader, segmenter, estimator);
    }

    @Test
    public void testSingularTarget()
            throws UIMAException, IOException, URISyntaxException
    {
        File targetFile = new File("target/topics.txt");

        int expectedLines = 2;
        String expectedLine0Regex = "dummy1.txt\t(0\\.[0-9]{4}\\t){9}0\\.[0-9]{4}";
        String expectedLine1Regex = "dummy2.txt\t(0\\.[0-9]{4}\\t){9}0\\.[0-9]{4}";

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, CAS_DIR,
                TextReader.PARAM_PATTERNS, CAS_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, LANGUAGE);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription inferencer = createEngineDescription(
                LdaTopicModelInferencer.class,
                LdaTopicModelInferencer.PARAM_MODEL_LOCATION, MODEL_FILE);

        AnalysisEngineDescription writer = createEngineDescription(
                LdaTopicProportionsWriter.class,
                LdaTopicProportionsWriter.PARAM_TARGET_LOCATION, targetFile,
                LdaTopicProportionsWriter.PARAM_OVERWRITE, true,
                LdaTopicProportionsWriter.PARAM_SINGULAR_TARGET, true);

        SimplePipeline.runPipeline(reader, segmenter, inferencer, writer);
        List<String> lines = FileUtils.readLines(targetFile);
        assertTrue(lines.get(0).matches(expectedLine0Regex));
        assertTrue(lines.get(1).matches(expectedLine1Regex));
        assertEquals(expectedLines, lines.size());
        targetFile.delete();
    }

    @Test
    public void testMultipleTargets()
            throws IOException, UIMAException
    {
        File targetDir = new File("target");
        File expectedFile0 = new File(targetDir, "dummy1.txt.topics");
        File expectedFile1 = new File(targetDir, "dummy2.txt.topics");

        int expectedLines = 1;
        String expectedLine0Regex = "dummy1.txt\t(0\\.[0-9]{4}\\t){9}0\\.[0-9]{4}";
        String expectedLine1Regex = "dummy2.txt\t(0\\.[0-9]{4}\\t){9}0\\.[0-9]{4}";

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, CAS_DIR,
                TextReader.PARAM_PATTERNS, CAS_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, LANGUAGE);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription inferencer = createEngineDescription(
                LdaTopicModelInferencer.class,
                LdaTopicModelInferencer.PARAM_MODEL_LOCATION, MODEL_FILE);

        AnalysisEngineDescription writer = createEngineDescription(
                LdaTopicProportionsWriter.class,
                LdaTopicProportionsWriter.PARAM_TARGET_LOCATION, targetDir,
                LdaTopicProportionsWriter.PARAM_OVERWRITE, true,
                LdaTopicProportionsWriter.PARAM_SINGULAR_TARGET, false,
                LdaTopicProportionsWriter.PARAM_WRITE_DOCID, true);

        SimplePipeline.runPipeline(reader, segmenter, inferencer, writer);

        assertTrue(expectedFile0.exists());
        assertTrue(expectedFile1.exists());

        List<String> lines = FileUtils.readLines(expectedFile0);
        assertTrue(lines.get(0).matches(expectedLine0Regex));
        assertEquals(expectedLines, lines.size());
        lines = FileUtils.readLines(expectedFile1);
        assertEquals(expectedLines, lines.size());
        assertTrue(lines.get(0).matches(expectedLine1Regex));

        expectedFile0.delete();
        expectedFile1.delete();
    }

    @Test
    public void testMultipleTargetsNoDocids()
            throws IOException, UIMAException
    {
        File targetDir = new File("target");
        File expectedFile0 = new File(targetDir, "dummy1.txt.topics");
        File expectedFile1 = new File(targetDir, "dummy2.txt.topics");

        int expectedLines = 1;
        String expectedLine0Regex = "(0\\.[0-9]{4}\\t){9}0\\.[0-9]{4}";
        String expectedLine1Regex = "(0\\.[0-9]{4}\\t){9}0\\.[0-9]{4}";

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, CAS_DIR,
                TextReader.PARAM_PATTERNS, CAS_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, LANGUAGE);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription inferencer = createEngineDescription(
                LdaTopicModelInferencer.class,
                LdaTopicModelInferencer.PARAM_MODEL_LOCATION, MODEL_FILE);

        AnalysisEngineDescription writer = createEngineDescription(
                LdaTopicProportionsWriter.class,
                LdaTopicProportionsWriter.PARAM_TARGET_LOCATION, targetDir,
                LdaTopicProportionsWriter.PARAM_OVERWRITE, true,
                LdaTopicProportionsWriter.PARAM_SINGULAR_TARGET, false,
                LdaTopicProportionsWriter.PARAM_WRITE_DOCID, false);

        SimplePipeline.runPipeline(reader, segmenter, inferencer, writer);

        assertTrue(expectedFile0.exists());
        assertTrue(expectedFile1.exists());

        List<String> lines = FileUtils.readLines(expectedFile0);
        assertTrue(lines.get(0).matches(expectedLine0Regex));
        assertEquals(expectedLines, lines.size());
        lines = FileUtils.readLines(expectedFile1);
        assertEquals(expectedLines, lines.size());
        assertTrue(lines.get(0).matches(expectedLine1Regex));

        expectedFile0.delete();
        expectedFile1.delete();
    }
}
