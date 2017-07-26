/*******************************************************************************
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.api.io.sequencegenerator;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.LexicalPhrase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static de.tudarmstadt.ukp.dkpro.core.api.io.sequencegenerator.StringSequenceGeneratorTest.*;
import static org.junit.Assert.assertEquals;

public class PhraseSequenceGeneratorTest
{
    @Test
    public void testTokenSequences()
            throws Exception
    {
        String featurePath = Token.class.getName();
        int expectedSize = 2;
        String expectedFirstToken = "Token1";
        String expectedLastToken = "Token2";

        JCas jCas = jCasWithTokens();

        PhraseSequenceGenerator sequenceGenerator = new PhraseSequenceGenerator.Builder()
                .featurePath(featurePath)
                .build();

        LexicalPhrase[] sequence = sequenceGenerator.tokenSequences(jCas).get(0);
        assertEquals(expectedSize, sequence.length);
        assertEquals(expectedFirstToken, sequence[0].getText());
        assertEquals(expectedLastToken, sequence[sequence.length - 1].getText());
    }

    @Test
    public void testLemmaSequences()
            throws IOException, FeaturePathException, UIMAException
    {
        String featurePath = Token.class.getName() + "/lemma/value";
        int expectedSize = 2;
        String expectedFirstToken = "lemma1";
        String expectedLastToken = "lemma2";

        JCas jCas = jCasWithLemmas();

        PhraseSequenceGenerator sequenceGenerator = new PhraseSequenceGenerator.Builder()
                .featurePath(featurePath)
                .build();

        LexicalPhrase[] sequence = sequenceGenerator.tokenSequences(jCas).get(0);
        assertEquals(expectedSize, sequence.length);
        assertEquals(expectedFirstToken, sequence[0].getText());
        assertEquals(expectedLastToken, sequence[sequence.length - 1].getText());
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

        PhraseSequenceGenerator sequenceGenerator = new PhraseSequenceGenerator.Builder()
                .featurePath(featurePath)
                .lowercase(false)
                .coveringType(covering)
                .build();

        List<LexicalPhrase[]> sequences = sequenceGenerator.tokenSequences(jCas);
        assertEquals(1, sequences.size());
        LexicalPhrase[] sequence = sequences.get(0);
        Assert.assertEquals(expectedSize, sequence.length);
        Assert.assertEquals(expectedFirstToken, sequence[0].getText());
        Assert.assertEquals(expectedLastToken, sequence[sequence.length - 1].getText());
    }

    @Test
    public void testGenerateSequenceStopwordsURL()
            throws FeaturePathException, UIMAException, IOException
    {
        int expectedSize = 2;
        URL stopwordsFile = this.getClass().getResource("/stopwords.txt");

        String expectedFirstToken = "";
        JCas jCas = jCasWithTokens();

        PhraseSequenceGenerator sequenceGenerator = new PhraseSequenceGenerator.Builder()
                .stopwordsURL(stopwordsFile)
                .lowercase(false)
                .build();

        List<LexicalPhrase[]> sequences = sequenceGenerator.tokenSequences(jCas);
        assertEquals(1, sequences.size());
        LexicalPhrase[] sequence = sequences.get(0);
        Assert.assertEquals(expectedSize, sequence.length);
        Assert.assertEquals(expectedFirstToken, sequence[0].getText());
    }

    @Test
    public void testGenerateSequenceStopwordsFile()
            throws FeaturePathException, UIMAException, IOException
    {
        int expectedSize = 2;
        File stopwordsFile = new File("src/test/resources/stopwords.txt");

        String expectedFirstToken = "";
        JCas jCas = jCasWithTokens();

        PhraseSequenceGenerator sequenceGenerator = new PhraseSequenceGenerator.Builder()
                .stopwordsFile(stopwordsFile)
                .lowercase(false)
                .build();

        List<LexicalPhrase[]> sequences = sequenceGenerator.tokenSequences(jCas);
        assertEquals(1, sequences.size());
        LexicalPhrase[] sequence = sequences.get(0);
        Assert.assertEquals(expectedSize, sequence.length);
        Assert.assertEquals(expectedFirstToken, sequence[0].getText());
    }

    @Test
    public void testGenerateSequenceStopwordsFileString()
            throws FeaturePathException, UIMAException, IOException
    {
        int expectedSize = 2;
        String stopwordsFile = "src/test/resources/stopwords.txt";
        String expectedFirstToken = "";

        JCas jCas = jCasWithTokens();

        PhraseSequenceGenerator sequenceGenerator = new PhraseSequenceGenerator.Builder()
                .stopwordsFile(stopwordsFile)
                .lowercase(false)
                .build();

        List<LexicalPhrase[]> sequences = sequenceGenerator.tokenSequences(jCas);
        assertEquals(1, sequences.size());
        LexicalPhrase[] sequence = sequences.get(0);
        Assert.assertEquals(expectedSize, sequence.length);
        Assert.assertEquals(expectedFirstToken, sequence[0].getText());
    }
}
