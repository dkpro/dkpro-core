/*******************************************************************************
 * Copyright 2016
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.mallet.wordembeddings;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

public class WordEmbeddingsEstimatorTest
{
    @Test
    public void test()
            throws UIMAException, IOException
    {
        File text = new File("src/test/resources/txt/*");
        File embeddingsFile = new File("target/dummy.vec");
        int expectedLength = 699;
        int dimensions = 50;

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, text,
                TextReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);
        AnalysisEngineDescription embeddings = createEngineDescription(
                WordEmbeddingsEstimator.class,
                WordEmbeddingsEstimator.PARAM_TARGET_LOCATION, embeddingsFile,
                WordEmbeddingsEstimator.PARAM_SINGULAR_TARGET, true,
                WordEmbeddingsEstimator.PARAM_MODEL_ENTITY_TYPE, Sentence.class.getCanonicalName());
        SimplePipeline.runPipeline(reader, segmenter, embeddings);

        List<String> output = Files.readAllLines(embeddingsFile.toPath());
        assertEquals(expectedLength, output.size());
        output.stream()
                .map(line -> line.split(" "))
                .peek(line -> assertEquals(dimensions + 1, line.length))
                .map(line -> Arrays.copyOfRange(line, 1, dimensions))
                /* fine if each value can be parsed to a double */
                .forEach(array -> Arrays.stream(array).forEach(Double::parseDouble));

        embeddingsFile.delete();
    }
}