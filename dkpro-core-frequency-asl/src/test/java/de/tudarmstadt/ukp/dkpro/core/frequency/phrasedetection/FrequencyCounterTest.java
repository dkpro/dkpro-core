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
package de.tudarmstadt.ukp.dkpro.core.frequency.phrasedetection;

import de.tudarmstadt.ukp.dkpro.core.io.text.StringReader;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public class FrequencyCounterTest
{
    @Rule
    public DkproTestContext testContext = new DkproTestContext();

    @Test
    public void testCount()
            throws Exception
    {
        int minCount = 1;

        File targetFile = new File(DkproTestContext.get().getTestOutputFolder(), "counts.txt");
        File expectedFile = new File("src/test/resources/phrasedetection/counts.txt");

        String sentence = "This is a first test that contains a first test example";
        String language = "en";

        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, sentence,
                StringReader.PARAM_LANGUAGE, language);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);
        AnalysisEngineDescription writer = createEngineDescription(FrequencyCounter.class,
                FrequencyCounter.PARAM_TARGET_LOCATION, targetFile,
                FrequencyCounter.PARAM_MIN_COUNT, minCount);

        writer.doFullValidation();
        SimplePipeline.runPipeline(reader, segmenter, writer);

        assertTrue(targetFile.exists());
        assertArrayEquals(
                Files.lines(expectedFile.toPath()).sorted().toArray(),
                Files.lines(targetFile.toPath()).sorted().toArray());
    }

    @Test
    public void testCountSortedAlphabetically()
            throws Exception
    {
        int minCount = 1;

        File targetFile = new File(DkproTestContext.get().getTestOutputFolder(), "counts.txt");
        File expectedFile = new File(
                "src/test/resources/phrasedetection/counts_sorted_alphabetically.txt");

        String sentence = "This is a first test that contains a first test example";
        String language = "en";

        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, sentence,
                StringReader.PARAM_LANGUAGE, language);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);
        AnalysisEngineDescription writer = createEngineDescription(FrequencyCounter.class,
                FrequencyCounter.PARAM_TARGET_LOCATION, targetFile,
                FrequencyCounter.PARAM_MIN_COUNT, minCount,
                FrequencyCounter.PARAM_SORT_BY_ALPHABET, true);

        SimplePipeline.runPipeline(reader, segmenter, writer);

        assertArrayEquals("Alphabetic sorting invalid.",
                Files.readAllBytes(expectedFile.toPath()),
                Files.readAllBytes(targetFile.toPath()));
    }

    @Test
    public void testCountSortedByValue()
            throws Exception
    {
        int minCount = 1;

        File targetFile = new File(DkproTestContext.get().getTestOutputFolder(), "counts.txt");

        String sentence = "This is a first test that contains a first test example";
        String language = "en";

        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, sentence,
                StringReader.PARAM_LANGUAGE, language);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);
        AnalysisEngineDescription writer = createEngineDescription(FrequencyCounter.class,
                FrequencyCounter.PARAM_TARGET_LOCATION, targetFile,
                FrequencyCounter.PARAM_MIN_COUNT, minCount,
                FrequencyCounter.PARAM_SORT_BY_COUNT, true);

        SimplePipeline.runPipeline(reader, segmenter, writer);

        assertTrue(targetFile.exists());

        /* check unigram sorting */
        double[] unigrams = Files.lines(targetFile.toPath())
                .filter(line -> !line.equals(FrequencyCounter.NGRAM_SEPARATOR_LINE))
                .map(line -> line.split(FrequencyCounter.COLUMN_SEPARATOR))
                .filter(fields -> !fields[0].contains(FrequencyCounter.BIGRAM_SEPARATOR))
                .map(fields -> fields[1])
                .mapToDouble(Double::parseDouble)
                .toArray();
        for (int i = 0; i < unigrams.length - 1; i++) {
            assertTrue(unigrams[i] >= unigrams[i + 1]);
        }

        /* check bigram sorting */
        double[] bigrams = Files.lines(targetFile.toPath())
                .filter(line -> !line.equals(FrequencyCounter.NGRAM_SEPARATOR_LINE))
                .map(line -> line.split(FrequencyCounter.COLUMN_SEPARATOR))
                .filter(fields -> fields[0].contains(FrequencyCounter.BIGRAM_SEPARATOR))
                .map(fields -> fields[1])
                .mapToDouble(Double::parseDouble)
                .toArray();
        for (int i = 0; i < bigrams.length - 1; i++) {
            assertTrue(bigrams[i] >= bigrams[i + 1]);
        }
    }

    @Test(expected = ResourceInitializationException.class)
    public void testSortBoth()
            throws IOException, UIMAException
    {
        String sentence = "This is a first test that contains a first test example";
        String language = "en";

        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, sentence,
                StringReader.PARAM_LANGUAGE, language);
        AnalysisEngineDescription writer = createEngineDescription(FrequencyCounter.class,
                FrequencyCounter.PARAM_SORT_BY_COUNT, true,
                FrequencyCounter.PARAM_SORT_BY_ALPHABET, true);

        SimplePipeline.runPipeline(reader, writer);
    }
}