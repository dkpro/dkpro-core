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
package org.dkpro.core.mallet.lda.io;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.dkpro.core.io.text.TextReader;
import org.dkpro.core.mallet.lda.MalletLdaTopicModelInferencer;
import org.dkpro.core.mallet.lda.MalletLdaUtil;
import org.dkpro.core.tokit.BreakIteratorSegmenter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 */
public class MalletLdaTopicProportionsWriterTest
{

    @Test
    public void testSingularTarget(@TempDir File tempDir)
            throws UIMAException, IOException, URISyntaxException
    {
        File modelFile = new File(tempDir, "model");
        File targetFile = new File(tempDir, "topics.txt");
        MalletLdaUtil.trainModel(modelFile);

        int expectedLines = 2;
        String expectedLine0Regex = "dummy1.txt\t(0\\.[0-9]{4}\\t){9}0\\.[0-9]{4}";
        String expectedLine1Regex = "dummy2.txt\t(0\\.[0-9]{4}\\t){9}0\\.[0-9]{4}";

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, MalletLdaUtil.CAS_DIR,
                TextReader.PARAM_PATTERNS, MalletLdaUtil.CAS_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, MalletLdaUtil.LANGUAGE);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription inferencer = createEngineDescription(
                MalletLdaTopicModelInferencer.class,
                MalletLdaTopicModelInferencer.PARAM_MODEL_LOCATION, modelFile);

        AnalysisEngineDescription writer = createEngineDescription(
                MalletLdaTopicProportionsWriter.class,
                MalletLdaTopicProportionsWriter.PARAM_TARGET_LOCATION, targetFile,
                MalletLdaTopicProportionsWriter.PARAM_OVERWRITE, true,
                MalletLdaTopicProportionsWriter.PARAM_SINGULAR_TARGET, true);

        SimplePipeline.runPipeline(reader, segmenter, inferencer, writer);
        List<String> lines = FileUtils.readLines(targetFile);
        assertTrue(lines.get(0).matches(expectedLine0Regex));
        assertTrue(lines.get(1).matches(expectedLine1Regex));
        assertEquals(expectedLines, lines.size());
    }

    @Test
    public void testMultipleTargets(@TempDir File tempDir)
            throws IOException, UIMAException
    {
        File targetDir = tempDir;
        File expectedFile0 = new File(targetDir, "dummy1.txt.topics");
        File expectedFile1 = new File(targetDir, "dummy2.txt.topics");

        int expectedLines = 1;
        String expectedLine0Regex = "dummy1.txt\t(0\\.[0-9]{4}\\t){9}0\\.[0-9]{4}";
        String expectedLine1Regex = "dummy2.txt\t(0\\.[0-9]{4}\\t){9}0\\.[0-9]{4}";

        File modelFile = new File(tempDir, "model");
        MalletLdaUtil.trainModel(modelFile);

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, MalletLdaUtil.CAS_DIR,
                TextReader.PARAM_PATTERNS, MalletLdaUtil.CAS_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, MalletLdaUtil.LANGUAGE);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription inferencer = createEngineDescription(
                MalletLdaTopicModelInferencer.class,
                MalletLdaTopicModelInferencer.PARAM_MODEL_LOCATION, modelFile);

        AnalysisEngineDescription writer = createEngineDescription(
                MalletLdaTopicProportionsWriter.class,
                MalletLdaTopicProportionsWriter.PARAM_TARGET_LOCATION, targetDir,
                MalletLdaTopicProportionsWriter.PARAM_OVERWRITE, true,
                MalletLdaTopicProportionsWriter.PARAM_SINGULAR_TARGET, false,
                MalletLdaTopicProportionsWriter.PARAM_WRITE_DOCID, true);

        SimplePipeline.runPipeline(reader, segmenter, inferencer, writer);

        assertTrue(expectedFile0.exists());
        assertTrue(expectedFile1.exists());

        List<String> lines = FileUtils.readLines(expectedFile0);
        assertTrue(lines.get(0).matches(expectedLine0Regex));
        assertEquals(expectedLines, lines.size());
        lines = FileUtils.readLines(expectedFile1);
        assertEquals(expectedLines, lines.size());
        assertTrue(lines.get(0).matches(expectedLine1Regex));
    }

    @Test
    public void testMultipleTargetsNoDocids(@TempDir File tempDir)
            throws IOException, UIMAException
    {
        File targetDir = tempDir;
        File expectedFile0 = new File(targetDir, "dummy1.txt.topics");
        File expectedFile1 = new File(targetDir, "dummy2.txt.topics");
        File modelFile = new File(tempDir, "model");
        MalletLdaUtil.trainModel(modelFile);

        int expectedLines = 1;
        String expectedLine0Regex = "(0\\.[0-9]{4}\\t){9}0\\.[0-9]{4}";
        String expectedLine1Regex = "(0\\.[0-9]{4}\\t){9}0\\.[0-9]{4}";

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, MalletLdaUtil.CAS_DIR,
                TextReader.PARAM_PATTERNS, MalletLdaUtil.CAS_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, MalletLdaUtil.LANGUAGE);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription inferencer = createEngineDescription(
                MalletLdaTopicModelInferencer.class,
                MalletLdaTopicModelInferencer.PARAM_MODEL_LOCATION, modelFile);

        AnalysisEngineDescription writer = createEngineDescription(
                MalletLdaTopicProportionsWriter.class,
                MalletLdaTopicProportionsWriter.PARAM_TARGET_LOCATION, targetDir,
                MalletLdaTopicProportionsWriter.PARAM_OVERWRITE, true,
                MalletLdaTopicProportionsWriter.PARAM_SINGULAR_TARGET, false,
                MalletLdaTopicProportionsWriter.PARAM_WRITE_DOCID, false);

        SimplePipeline.runPipeline(reader, segmenter, inferencer, writer);

        assertTrue(expectedFile0.exists());
        assertTrue(expectedFile1.exists());

        List<String> lines = FileUtils.readLines(expectedFile0);
        assertTrue(lines.get(0).matches(expectedLine0Regex));
        assertEquals(expectedLines, lines.size());
        lines = FileUtils.readLines(expectedFile1);
        assertEquals(expectedLines, lines.size());
        assertTrue(lines.get(0).matches(expectedLine1Regex));
    }
}
