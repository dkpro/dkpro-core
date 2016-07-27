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
import de.tudarmstadt.ukp.dkpro.core.mallet.lda.LdaTopicModelInferencer;
import de.tudarmstadt.ukp.dkpro.core.mallet.lda.LdaTopicModelTrainer;
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
import java.util.List;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class LdaTopicsProportionsSortedWriterTest
{
    private static final File MODEL_FILE = new File("target/mallet/model");
    private static final String CAS_DIR = "src/test/resources/txt";
    private static final String CAS_FILE_PATTERN = "[+]*.txt";

    private static final int N_TOPICS = 10;
    private static final int N_ITERATIONS = 50;
    private static final String LANGUAGE = "en";

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp()
            throws Exception
    { /* Generate model */
        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, CAS_DIR,
                TextReader.PARAM_PATTERNS, CAS_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, LANGUAGE);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription estimator = createEngineDescription(
                LdaTopicModelTrainer.class,
                LdaTopicModelTrainer.PARAM_TARGET_LOCATION, MODEL_FILE,
                LdaTopicModelTrainer.PARAM_N_ITERATIONS, N_ITERATIONS,
                LdaTopicModelTrainer.PARAM_N_TOPICS, N_TOPICS);
        SimplePipeline.runPipeline(reader, segmenter, estimator);
    }

    @Test
    public void test()
            throws UIMAException, IOException
    {
        File targetFile = new File("target/topics.txt");

        int expectedLines = 2;
        int nTopicsOutput = 3;
        String expectedLine0Regex = "dummy1.txt(\t[0-9]+:0\\.[0-9]{4}){" + nTopicsOutput + "}";
        String expectedLine1Regex = "dummy2.txt(\t[0-9]+:0\\.[0-9]{4}){" + nTopicsOutput + "}";

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, CAS_DIR,
                TextReader.PARAM_PATTERNS, CAS_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, LANGUAGE);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription inferencer = createEngineDescription(
                LdaTopicModelInferencer.class,
                LdaTopicModelInferencer.PARAM_MODEL_LOCATION, MODEL_FILE);

        AnalysisEngineDescription writer = createEngineDescription(
                LdaTopicsProportionsSortedWriter.class,
                LdaTopicsProportionsSortedWriter.PARAM_TARGET_LOCATION, targetFile,
                LdaTopicsProportionsSortedWriter.PARAM_N_TOPICS, nTopicsOutput);

        SimplePipeline.runPipeline(reader, segmenter, inferencer, writer);
        List<String> lines = FileUtils.readLines(targetFile);
        assertTrue(lines.get(0).matches(expectedLine0Regex));
        assertTrue(lines.get(1).matches(expectedLine1Regex));
        assertEquals(expectedLines, lines.size());

        /* assert first field */
        lines.stream()
                .map(line -> line.split("\t"))
                .forEach(fields -> assertTrue(fields[0].startsWith("dummy")));
        targetFile.delete();
    }
}
