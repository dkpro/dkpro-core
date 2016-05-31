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
package de.tudarmstadt.ukp.dkpro.core.mallet.internal;

import cc.mallet.types.TokenSequence;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class TokenSequenceGeneratorTest
{

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
        jCas.setDocumentText("Token1 Token2");
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
    public void testGenerateSequenceFeaturePath()
            throws FeaturePathException, UIMAException
    {
        String featurePath = Token.class.getName();
        int expectedSize = 2;
        String expectedFirstToken = "Token1";
        String expectedLastToken = "Token2";

        JCas jCas = jCasWithTokens();

        TokenSequenceGenerator tsg = new TokenSequenceGenerator(featurePath);

        TokenSequence ts = tsg.tokenSequences(jCas).get(0);
        assertEquals(expectedSize, ts.size());
        assertEquals(expectedFirstToken, ts.get(0).getText());
        assertEquals(expectedLastToken, ts.get(ts.size() - 1).getText());
    }

    @Test
    public void testGenerateSequenceFeaturePathLowercase()
            throws FeaturePathException, UIMAException
    {
        String featurePath = Token.class.getName();
        int expectedSize = 2;
        String expectedFirstToken = "token1";
        String expectedLastToken = "token2";

        JCas jCas = jCasWithTokens();

        TokenSequenceGenerator tsg = new TokenSequenceGenerator(featurePath);
        tsg.setLowercase(true);

        TokenSequence ts = tsg.tokenSequences(jCas).get(0);
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

        TokenSequenceGenerator tsg = new TokenSequenceGenerator(featurePath);

        TokenSequence ts = tsg.tokenSequences(jCas).get(0);
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
        String expectedFirstToken = "Token1";
        String expectedLastToken = "Token2";
        Optional<String> covering = Optional.of(Sentence.class.getTypeName());

        JCas jCas = jcasWithSentence();

        TokenSequenceGenerator tsg = new TokenSequenceGenerator(featurePath);
        tsg.setLowercase(false);

        List<TokenSequence> sequences = tsg.tokenSequences(jCas, covering);
        assertEquals(1, sequences.size());
        TokenSequence ts = sequences.get(0);
        assertEquals(expectedSize, ts.size());
        assertEquals(expectedFirstToken, ts.get(0).getText());
        assertEquals(expectedLastToken, ts.get(ts.size() - 1).getText());
    }

    @Test
    public void testCharacterSequence()
            throws UIMAException, FeaturePathException
    {
        JCas jcas = jCasWithTokens();
        int expectedSize = 13;
        String expectedFirst = "T";
        String expectedLast = "2";

        TokenSequenceGenerator tsg = new TokenSequenceGenerator();
        tsg.setUseCharacters(true);

        TokenSequence ts = tsg.tokenSequences(jcas).get(0);

        assertEquals(expectedSize, ts.size());
        assertEquals(expectedFirst, ts.get(0).getText());
        assertEquals(expectedLast, ts.get(expectedSize - 1).getText());
    }

    @Test
    public void testCharacterSequenceLowercase()
            throws UIMAException, FeaturePathException
    {
        JCas jcas = jCasWithTokens();
        int expectedSize = 13;
        String expectedFirst = "t";
        String expectedLast = "2";
        TokenSequenceGenerator tsg = new TokenSequenceGenerator();
        tsg.setUseCharacters(true);
        tsg.setLowercase(true);

        TokenSequence ts = tsg.tokenSequences(jcas).get(0);

        assertEquals(expectedSize, ts.size());
        assertEquals(expectedFirst, ts.get(0).getText());
        assertEquals(expectedLast, ts.get(expectedSize - 1).getText());
    }

    @Test
    public void testCharacterSequenceWithCovering()
            throws UIMAException, FeaturePathException
    {
        String covering = Sentence.class.getTypeName();
        JCas jCas = jcasWithSentence();
        int expectedSequences = 1;
        int expectedSize = 13;
        String expectedFirst = "T";
        String expectedLast = "2";

        TokenSequenceGenerator tsg = new TokenSequenceGenerator();
        tsg.setUseCharacters(true);

        List<TokenSequence> tokenSequences = tsg.tokenSequences(jCas, Optional.of(covering));
        assertEquals(expectedSequences, tokenSequences.size());
        TokenSequence ts = tokenSequences.get(0);
        assertEquals(expectedSize, ts.size());
        assertEquals(expectedFirst, ts.get(0).getText());
        assertEquals(expectedLast, ts.get(expectedSize - 1).getText());
    }
}