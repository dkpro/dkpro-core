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
package de.tudarmstadt.ukp.dkpro.core.api.io.sequencegenerator;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class StringSequenceGeneratorTest
{
    /**
     * Generate a JCas with two token annotations.
     *
     * @return a {@link JCas} with two tokens.
     * @throws UIMAException
     */
    protected static JCas jCasWithTokens()
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
    protected static JCas jCasWithLemmas()
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

    protected static JCas jcasWithNamedEntity()
            throws UIMAException
    {
        JCas jCas = JCasFactory.createJCas();
        jCas.setDocumentText("token1 token2");
        DocumentMetaData metaData = DocumentMetaData.create(jCas);
        metaData.setDocumentId("lemmasTest");
        metaData.addToIndexes(jCas);

        Token token1 = new Token(jCas, 0, 6);
        Token token2 = new Token(jCas, 7, 13);
        NamedEntity ne = new NamedEntity(jCas, 0, 6);
        ne.setValue("TEST");
        ne.addToIndexes(jCas);

        token1.addToIndexes(jCas);
        token2.addToIndexes(jCas);
        return jCas;
    }

    /**
     * Create a JCas with one sentence.
     *
     * @return a {@link JCas} with a sentence annotation.
     * @throws UIMAException
     * @see #jCasWithTokens()
     */
    protected static JCas jCasWithSentence()
            throws UIMAException
    {
        JCas jCas = jCasWithTokens();

        Sentence sentence = new Sentence(jCas, 0, 13);
        sentence.addToIndexes();
        return jCas;
    }

    @Test
    public void testGenerateSequenceFeaturePath()
            throws FeaturePathException, UIMAException, IOException
    {
        String featurePath = Token.class.getName();
        int expectedSize = 2;
        String expectedFirstToken = "Token1";
        String expectedLastToken = "Token2";

        JCas jCas = jCasWithTokens();

        StringSequenceGenerator sequenceGenerator = new PhraseSequenceGenerator.Builder()
                .featurePath(featurePath)
                .buildStringSequenceGenerator();

        String[] sequence = sequenceGenerator.tokenSequences(jCas).get(0);
        assertEquals(expectedSize, sequence.length);
        assertEquals(expectedFirstToken, sequence[0]);
        assertEquals(expectedLastToken, sequence[sequence.length - 1]);
    }

    @Test
    public void testGenerateSequenceFeaturePathLowercase()
            throws FeaturePathException, UIMAException, IOException
    {
        String featurePath = Token.class.getName();
        int expectedSize = 2;
        String expectedFirstToken = "token1";
        String expectedLastToken = "token2";

        JCas jCas = jCasWithTokens();

        StringSequenceGenerator sequenceGenerator = new PhraseSequenceGenerator.Builder()
                .featurePath(featurePath)
                .lowercase(true)
                .buildStringSequenceGenerator();

        String[] sequence = sequenceGenerator.tokenSequences(jCas).get(0);
        assertEquals(expectedSize, sequence.length);
        assertEquals(expectedFirstToken, sequence[0]);
        assertEquals(expectedLastToken, sequence[sequence.length - 1]);
    }

    @Test
    public void testGenerateSequenceFeaturePathLemmas()
            throws UIMAException, FeaturePathException, IOException
    {
        String featurePath = Token.class.getName() + "/lemma/value";
        int expectedSize = 2;
        String expectedFirstLemma = "lemma1";
        String expectedLastLemma = "lemma2";

        JCas jCas = jCasWithLemmas();

        StringSequenceGenerator sequenceGenerator = new PhraseSequenceGenerator.Builder()
                .featurePath(featurePath)
                .buildStringSequenceGenerator();

        String[] sequence = sequenceGenerator.tokenSequences(jCas).get(0);
        assertEquals(expectedSize, sequence.length);
        assertEquals(expectedFirstLemma, sequence[0]);
        assertEquals(expectedLastLemma, sequence[sequence.length - 1]);
    }

    @Test
    public void testFeaturePathNamedEntities()
            throws UIMAException, IOException, FeaturePathException
    {
        String featurePath = NamedEntity.class.getCanonicalName();
        int expectedSize = 1;
        String expectedNamedEntity = "token1";
        JCas jCas = jcasWithNamedEntity();
        StringSequenceGenerator sequenceGenerator = new PhraseSequenceGenerator.Builder()
                .featurePath(featurePath)
                .buildStringSequenceGenerator();

        String[] sequence = sequenceGenerator.tokenSequences(jCas).get(0);
        assertEquals(expectedSize, sequence.length);
        assertEquals(expectedNamedEntity, sequence[0]);

    }

    @Test
    public void testGenerateSequenceFeaturePathCovering()
            throws FeaturePathException, UIMAException, IOException
    {
        String featurePath = Token.class.getName();
        int expectedSize = 2;
        String expectedFirstToken = "Token1";
        String expectedLastToken = "Token2";
        String covering = Sentence.class.getTypeName();

        JCas jCas = jCasWithSentence();

        StringSequenceGenerator sequenceGenerator = new PhraseSequenceGenerator.Builder()
                .featurePath(featurePath)
                .lowercase(false)
                .coveringType(covering)
                .buildStringSequenceGenerator();

        List<String[]> sequences = sequenceGenerator.tokenSequences(jCas);
        assertEquals(1, sequences.size());
        String[] sequence = sequences.get(0);
        Assert.assertEquals(expectedSize, sequence.length);
        Assert.assertEquals(expectedFirstToken, sequence[0]);
        Assert.assertEquals(expectedLastToken, sequence[sequence.length - 1]);
    }

    @Test
    public void testFilterRegex()
            throws UIMAException, IOException, FeaturePathException
    {
        JCas jCas = jCasWithTokens();
        String filterRegex = ".*1";

        int expectedSize = 1;
        String expectedToken = "Token2";

        StringSequenceGenerator sequenceGenerator = new PhraseSequenceGenerator.Builder()
                .filterRegex(filterRegex)
                .buildStringSequenceGenerator();

        List<String[]> sequences = sequenceGenerator.tokenSequences(jCas);
        assertEquals(1, sequences.size());
        String[] sequence = sequences.get(0);
        assertEquals(expectedSize, sequence.length);
        assertEquals(expectedToken, sequence[0]);
    }

    @Test
    public void testFilterRegexReplace()
            throws UIMAException, IOException, FeaturePathException
    {
        JCas jCas = jCasWithTokens();
        String filterRegex = ".*1";
        String replacement = "REPLACED";

        int expectedSize = 2;
        String expectedToken2 = "Token2";

        StringSequenceGenerator sequenceGenerator = new PhraseSequenceGenerator.Builder()
                .filterRegex(filterRegex)
                .filterRegexReplacement(replacement)
                .buildStringSequenceGenerator();

        List<String[]> sequences = sequenceGenerator.tokenSequences(jCas);
        assertEquals(1, sequences.size());
        String[] sequence = sequences.get(0);
        assertEquals(expectedSize, sequence.length);
        assertEquals(replacement, sequence[0]);
        assertEquals(expectedToken2, sequence[1]);
    }

    @Test
    public void testFilterRegexMultiple()
            throws UIMAException, IOException, FeaturePathException
    {
        JCas jCas = jCasWithTokens();
        String filterRegex1 = ".*1";
        String filterRegex2 = "xyz";

        int expectedSize = 1;
        String expectedToken = "Token2";

        StringSequenceGenerator sequenceGenerator = new PhraseSequenceGenerator.Builder()
                .filterRegex(filterRegex1)
                .filterRegex(filterRegex2)
                .buildStringSequenceGenerator();
        List<String[]> sequences = sequenceGenerator.tokenSequences(jCas);
        assertEquals(1, sequences.size());
        String[] sequence = sequences.get(0);
        assertEquals(expectedSize, sequence.length);
        assertEquals(expectedToken, sequence[0]);
    }

    @Test
    public void testFilterStopwordsURL()
            throws UIMAException, FeaturePathException, IOException
    {
        JCas jcas = jCasWithTokens();
        URL stopwordsFile = this.getClass().getResource("/stopwords.txt");
        int expectedSize = 1;
        String expectedFirst = "Token2";

        StringSequenceGenerator sequenceGenerator = new PhraseSequenceGenerator.Builder()
                .stopwordsURL(stopwordsFile)
                .buildStringSequenceGenerator();

        String[] sequence = sequenceGenerator.tokenSequences(jcas).get(0);

        assertEquals(expectedSize, sequence.length);
        assertEquals(expectedFirst, sequence[0]);
    }

    @Test
    public void testFilterStopwordsFileString()
            throws UIMAException, FeaturePathException, IOException
    {
        JCas jcas = jCasWithTokens();
        String stopwordsFile = "src/test/resources/stopwords.txt";
        int expectedSize = 1;
        String expectedFirst = "Token2";

        StringSequenceGenerator sequenceGenerator = new PhraseSequenceGenerator.Builder()
                .stopwordsFile(stopwordsFile)
                .buildStringSequenceGenerator();

        String[] sequence = sequenceGenerator.tokenSequences(jcas).get(0);

        assertEquals(expectedSize, sequence.length);
        assertEquals(expectedFirst, sequence[0]);
    }

    @Test
    public void testFilterStopwordsFile()
            throws UIMAException, FeaturePathException, IOException
    {
        JCas jcas = jCasWithTokens();
        File stopwordsFile = new File("src/test/resources/stopwords.txt");
        int expectedSize = 1;
        String expectedFirst = "Token2";

        StringSequenceGenerator sequenceGenerator = new PhraseSequenceGenerator.Builder()
                .stopwordsFile(stopwordsFile)
                .buildStringSequenceGenerator();

        String[] sequence = sequenceGenerator.tokenSequences(jcas).get(0);

        assertEquals(expectedSize, sequence.length);
        assertEquals(expectedFirst, sequence[0]);
    }

    /* test character sequences*/
    @Test
    public void testCharacterSequence()
            throws UIMAException, FeaturePathException, IOException
    {
        JCas jcas = jCasWithTokens();
        int expectedSize = 13;
        String expectedFirst = "T";
        String expectedLast = "2";

        StringSequenceGenerator sequenceGenerator = new PhraseSequenceGenerator.Builder()
                .characters(true)
                .buildStringSequenceGenerator();

        String[] sequence = sequenceGenerator.tokenSequences(jcas).get(0);

        assertEquals(expectedSize, sequence.length);
        assertEquals(expectedFirst, sequence[0]);
        assertEquals(expectedLast, sequence[expectedSize - 1]);
    }

    @Test
    public void testCharacterSequenceLowercase()
            throws UIMAException, FeaturePathException, IOException
    {
        JCas jcas = jCasWithTokens();
        int expectedSize = 13;
        String expectedFirst = "t";
        String expectedLast = "2";
        StringSequenceGenerator sequenceGenerator = new PhraseSequenceGenerator.Builder()
                .lowercase(true)
                .characters(true)
                .buildStringSequenceGenerator();

        String[] sequence = sequenceGenerator.tokenSequences(jcas).get(0);

        assertEquals(expectedSize, sequence.length);
        assertEquals(expectedFirst, sequence[0]);
        assertEquals(expectedLast, sequence[expectedSize - 1]);
    }

    @Test
    public void testCharacterSequenceWithCovering()
            throws UIMAException, FeaturePathException, IOException
    {
        String covering = Sentence.class.getTypeName();
        JCas jCas = jCasWithSentence();
        int expectedSequences = 1;
        int expectedSize = 13;
        String expectedFirst = "T";
        String expectedLast = "2";

        StringSequenceGenerator sequenceGenerator = new PhraseSequenceGenerator.Builder()
                .coveringType(covering)
                .characters(true)
                .buildStringSequenceGenerator();

        List<String[]> sequences = sequenceGenerator.tokenSequences(jCas);
        assertEquals(expectedSequences, sequences.size());
        String[] sequence = sequences.get(0);
        assertEquals(expectedSize, sequence.length);
        assertEquals(expectedFirst, sequence[0]);
        assertEquals(expectedLast, sequence[expectedSize - 1]);
    }
}
