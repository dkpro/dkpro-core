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
package de.tudarmstadt.ukp.dkpro.core.mallet;

import cc.mallet.types.TokenSequence;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.morpha.MorphaLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.Type;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MalletUtilsTest
{
    private static final String TXT_DIR = "src/test/resources/txt";
    private static final String TXT_FILE_PATTERN = "[+]*.txt";

    /**
     * Generate a JCas with two token annotations.
     *
     * @return a {@link JCas} with two tokens.
     * @throws UIMAException
     */
    private static JCas jCasWithTokens()
            throws UIMAException
    {
        JCas jCas = JCasFactory.createJCas();
        jCas.setDocumentText("token1 token2");
        DocumentMetaData metaData = DocumentMetaData.create(jCas);
        metaData.setDocumentId("tokensTest");
        metaData.addToIndexes(jCas);

        Token token1 = new Token(jCas, 0, 6);
        Token token2 = new Token(jCas, 7, 13);

        token1.addToIndexes(jCas);
        token2.addToIndexes(jCas);
        return jCas;
    }

    /**
     * Generate a JCas with two tokens and two lemmas.
     *
     * @return a {@link JCas}
     * @throws UIMAException
     */
    private static JCas jcasWithLemmas()
            throws UIMAException
    {
        JCas jCas = JCasFactory.createJCas();
        jCas.setDocumentText("token1 token2");
        DocumentMetaData metaData = DocumentMetaData.create(jCas);
        metaData.setDocumentId("lemmasTest");
        metaData.addToIndexes(jCas);

        Token token1 = new Token(jCas, 0, 6);
        Token token2 = new Token(jCas, 7, 13);
        Lemma lemma1 = new Lemma(jCas, 0, 6);
        lemma1.setValue("lemma1");
        Lemma lemma2 = new Lemma(jCas, 7, 13);
        lemma2.setValue("lemma2");

        token1.setLemma(lemma1);
        token2.setLemma(lemma2);

        token1.addToIndexes(jCas);
        token2.addToIndexes(jCas);
        lemma1.addToIndexes(jCas);
        lemma2.addToIndexes(jCas);
        return jCas;
    }

    /**
     * Create a JCas with one sentence.
     *
     * @return a {@link JCas} with a sentence annotation.
     * @throws UIMAException
     * @see #jCasWithTokens()
     */
    private static JCas jcasWithSentence()
            throws UIMAException
    {
        JCas jCas = jCasWithTokens();

        Sentence sentence = new Sentence(jCas, 0, 13);
        sentence.addToIndexes();
        return jCas;
    }

    @Test
    public void testGenerateSequenceUseLemmas()
            throws ResourceInitializationException, FeaturePathException
    {
        boolean useLemmas = true;
        String language = "en";
        String typeName = Token.class.getName();
        int minTokenLength = 5;
        int minDocumentLength = 200;

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, TXT_DIR,
                TextReader.PARAM_PATTERNS, TXT_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, language);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);
        AnalysisEngineDescription lemmatizer = createEngineDescription(MorphaLemmatizer.class);

        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter, lemmatizer)) {
            Type tokenType = CasUtil.getType(jcas.getCas(), typeName);
            TokenSequence ts = MalletUtils.generateTokenSequence(
                    jcas, tokenType, useLemmas, minTokenLength);
            assertTrue(ts.size() > minDocumentLength);
            ts.forEach((cc.mallet.types.Token token) ->
                    assertTrue(token.getText().length() >= minTokenLength));
        }
    }

    @Test
    public void testGenerateSequence()
            throws ResourceInitializationException, FeaturePathException
    {
        boolean useLemmas = false;
        String language = "en";
        String typeName = Token.class.getName();
        int minTokenLength = 5;
        int minDocumentLength = 300;

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, TXT_DIR,
                TextReader.PARAM_PATTERNS, TXT_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, language);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter)) {
            Type tokenType = CasUtil.getType(jcas.getCas(), typeName);
            TokenSequence ts = MalletUtils.generateTokenSequence(
                    jcas, tokenType, useLemmas, minTokenLength);
            assertTrue(ts.size() > minDocumentLength);
            ts.forEach((cc.mallet.types.Token token) ->
                    assertTrue(token.getText().length() >= minTokenLength));
        }
    }

    @Test
    public void testGenerateSequenceFeaturePath()
            throws FeaturePathException, UIMAException
    {
        String featurePath = Token.class.getName();
        int expectedSize = 2;
        String expectedFirstToken = "token1";
        String expectedLastToken = "token2";

        JCas jCas = jCasWithTokens();

        TokenSequence ts = MalletUtils
                .generateTokenSequence(jCas, featurePath, Optional.empty(), OptionalInt.empty());
        assertEquals(expectedSize, ts.size());
        assertEquals(expectedFirstToken, ts.get(0).getText());
        assertEquals(expectedLastToken, ts.get(ts.size() - 1).getText());
    }

    @Test
    public void testGenerateSequenceFeaturePathLemmas()
            throws UIMAException, FeaturePathException
    {
        String featurePath = Token.class.getName() + "/lemma/value";
        int expectedSize = 2;
        String expectedFirstLemma = "lemma1";
        String expectedLastLemma = "lemma2";

        JCas jCas = jcasWithLemmas();

        TokenSequence ts = MalletUtils
                .generateTokenSequence(jCas, featurePath, Optional.empty(), OptionalInt.empty());
        assertEquals(expectedSize, ts.size());
        assertEquals(expectedFirstLemma, ts.get(0).getText());
        assertEquals(expectedLastLemma, ts.get(ts.size() - 1).getText());
    }

    @Test
    public void testGenerateSequenceFeaturePathCovering()
            throws FeaturePathException, UIMAException
    {
        String featurePath = Token.class.getName();
        int expectedSize = 2;
        String expectedFirstToken = "token1";
        String expectedLastToken = "token2";
        Optional<String> covering = Optional.of(Sentence.class.getTypeName());

        JCas jCas = jcasWithSentence();

        List<TokenSequence> sequences = MalletUtils
                .generateTokenSequences(jCas, featurePath, covering, OptionalInt.empty());
        assertEquals(1, sequences.size());
        TokenSequence ts = sequences.get(0);
        assertEquals(expectedSize, ts.size());
        assertEquals(expectedFirstToken, ts.get(0).getText());
        assertEquals(expectedLastToken, ts.get(ts.size() - 1).getText());
    }

    @Test
    public void testCharacterSequence()
            throws UIMAException
    {
        JCas jcas = jCasWithTokens();
        int expectedSize = 13;
        String expectedFirst = "t";
        String expectedLast = "2";
        TokenSequence ts = MalletUtils.characterSequence(jcas);
        assertEquals(expectedSize, ts.size());
        assertEquals(expectedFirst, ts.get(0).getText());
        assertEquals(expectedLast, ts.get(12).getText());
    }

    @Test
    public void testCharacterSequenceWithCovering()
            throws UIMAException
    {
        String covering = Sentence.class.getTypeName();
        JCas jCas = jcasWithSentence();
        int expectedSequences = 1;
        int expectedSize = 13;
        String expectedFirst = "t";
        String expectedLast = "2";

        List<TokenSequence> tokenSequences = MalletUtils.characterSequences(jCas, covering);
        assertEquals(expectedSequences, tokenSequences.size());
        TokenSequence ts = tokenSequences.get(0);
        assertEquals(expectedSize, ts.size());
        assertEquals(expectedFirst, ts.get(0).getText());
        assertEquals(expectedLast, ts.get(12).getText());
    }
}