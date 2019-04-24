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
package de.tudarmstadt.ukp.dkpro.core.mallet.wordembeddings;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.embeddings.VectorizerUtils;
import org.dkpro.core.io.text.TextReader;
import org.dkpro.core.tokit.BreakIteratorSegmenter;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.mallet.type.WordEmbedding;

public class MalletEmbeddingsAnnotatorTest
{
    private File modelFile;
    private File binaryModelFile;

    private static final String TXT_DIR = "src/test/resources/txt";
    private static final String TXT_FILE_PATTERN = "[+]*.txt";

    @Before
    public void setUp()
            throws URISyntaxException
    {
        modelFile = new File(getClass().getResource("/dummy.vec").toURI());
        binaryModelFile = new File(getClass().getResource("/dummy.binary").toURI());
    }

    @Test
    public void test()
            throws ResourceInitializationException
    {
        // tag::example[]
        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, TXT_DIR,
                TextReader.PARAM_PATTERNS, TXT_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription inferencer = createEngineDescription(
                MalletEmbeddingsAnnotator.class,
                MalletEmbeddingsAnnotator.PARAM_MODEL_LOCATION, modelFile);
        //end::example[]

        testEmbeddingAnnotations(reader, segmenter, inferencer);
    }

    @Test
    public void testBinary()
            throws ResourceInitializationException
    {
        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, TXT_DIR,
                TextReader.PARAM_PATTERNS, TXT_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription inferencer = createEngineDescription(
                MalletEmbeddingsAnnotator.class,
                MalletEmbeddingsAnnotator.PARAM_MODEL_LOCATION, binaryModelFile,
                MalletEmbeddingsAnnotator.PARAM_MODEL_IS_BINARY, true);

        testEmbeddingAnnotations(reader, segmenter, inferencer);
    }

    @Test
    public void testUnknownTokensText()
            throws ResourceInitializationException
    {
        int dim = 50;
        float[] unkVector = VectorizerUtils.randomVector(dim);
        int minTokenLength = 3; // minimum token length in test vector file

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, TXT_DIR,
                TextReader.PARAM_PATTERNS, TXT_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription inferencer = createEngineDescription(
                MalletEmbeddingsAnnotator.class,
                MalletEmbeddingsAnnotator.PARAM_MODEL_LOCATION, modelFile,
                MalletEmbeddingsAnnotator.PARAM_ANNOTATE_UNKNOWN_TOKENS, true);

        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter, inferencer)) {
            for (Token token : select(jcas, Token.class)) {
                if (token.getCoveredText().length() < minTokenLength) {
                    float[] vector = selectCovered(WordEmbedding.class, token).get(0)
                            .getWordEmbedding()
                            .toArray();
                    assertTrue(Arrays.equals(vector, unkVector));
                }
            }
        }
    }

    @Test
    public void testUnknownTokensTextRandom()
            throws ResourceInitializationException
    {
        int dim = 50;
        int minTokenLength = 3; // minimum token length in test vector file

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, TXT_DIR,
                TextReader.PARAM_PATTERNS, TXT_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription inferencer = createEngineDescription(
                MalletEmbeddingsAnnotator.class,
                MalletEmbeddingsAnnotator.PARAM_MODEL_LOCATION, modelFile,
                MalletEmbeddingsAnnotator.PARAM_ANNOTATE_UNKNOWN_TOKENS, true);

        float[] randomVector = null;
        boolean isFirst = true;
        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter, inferencer)) {
            for (Token token : select(jcas, Token.class)) {
                if (token.getCoveredText().length() < minTokenLength) {
                    /* token should be unknown */
                    float[] vector = selectCovered(WordEmbedding.class, token).get(0)
                            .getWordEmbedding()
                            .toArray();
                    assertEquals(dim, vector.length);

                    if (isFirst) {
                        randomVector = vector.clone();
                        isFirst = false;
                    }
                    else {
                        assertTrue(Arrays.equals(vector, randomVector));
                    }
                }
            }
        }
    }

    @Test(expected = ResourceInitializationException.class)
    public void testLowercaseCaseless()
            throws UIMAException, IOException
    {
        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, TXT_DIR,
                TextReader.PARAM_PATTERNS, TXT_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription inferencer = createEngineDescription(
                MalletEmbeddingsAnnotator.class,
                MalletEmbeddingsAnnotator.PARAM_MODEL_LOCATION, modelFile,
                MalletEmbeddingsAnnotator.PARAM_LOWERCASE, true);
        SimplePipeline.runPipeline(reader, inferencer);
    }

    private static void testEmbeddingAnnotations(CollectionReaderDescription reader,
            AnalysisEngineDescription segmenter, AnalysisEngineDescription inferencer)
    {
        int expectedEmbeddingsPerToken = 1;
        int minTokenLength = 3; // minimum token length in test vector file

        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter, inferencer)) {
            for (Token token : select(jcas, Token.class)) {
                if (token.getCoveredText().length() >= minTokenLength) {
                    assertEquals(expectedEmbeddingsPerToken,
                            selectCovered(WordEmbedding.class, token).size());
                }
                else {
                    assertTrue(selectCovered(WordEmbedding.class, token).isEmpty());
                }
            }
        }
    }
}
