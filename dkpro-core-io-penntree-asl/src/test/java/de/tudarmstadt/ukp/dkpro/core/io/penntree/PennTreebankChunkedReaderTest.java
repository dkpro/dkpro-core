/*
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.io.penntree;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertEquals;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.AssertAnnotations;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;

public class PennTreebankChunkedReaderTest
{
    @Test
    public void testCountsOfAnnotations()
        throws Exception
    {
        JCas jCas = readTestFile("generalTest.pos");

        assertEquals(1, select(jCas, Sentence.class).size());
        assertEquals(32, select(jCas, Token.class).size());
        assertEquals(32, select(jCas, POS.class).size());
        assertEquals(8, select(jCas, Chunk.class).size());
    }

    @Test
    public void testPartOfSpeechTagAssignment()
        throws Exception
    {
        JCas jCas = readTestFile("generalTest.pos");

        String[] posOriginal = { "DT", "NN", "IN", "JJ", "NNS", "VBG", "IN", "NNP", "NNP", "NNP",
                "VBD", "PRP", "VBZ", "VBN", "DT", "$", "CD", "CD", "NN", "NN", "IN", "JJS", "IN",
                "NNP", "NNP", "NNP", "POS", "NN", "CC", "NN", "NNS", "." };

        String[] posMapped = { "POS_DET", "POS_NOUN", "POS_ADP", "POS_ADJ", "POS_NOUN", "POS_VERB", "POS_ADP", "POS_PROPN", "POS_PROPN",
                "POS_PROPN", "POS_VERB", "POS_PRON", "POS_VERB", "POS_VERB", "POS_DET", "POS_PUNCT", "POS_NUM", "POS_NUM", "POS_NOUN",
                "POS_NOUN", "POS_ADP", "POS_ADJ", "POS_ADP", "POS_PROPN", "POS_PROPN", "POS_PROPN", "POS_X", "POS_NOUN", "POS_CONJ", "POS_NOUN",
                "POS_NOUN", "POS_PUNCT" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jCas, POS.class));
    }

    @Test
    public void testTokenBoundaries()
        throws Exception
    {
        JCas jCas = readTestFile("generalTest.pos");

        String[] tokens = { "A", "consortium", "of", "private", "investors", "operating", "as",
                "LJH", "Funding", "Co.", "said", "it", "has", "made", "a", "$", "409", "million",
                "cash", "bid", "for", "most", "of", "L.J.", "Hooker", "Corp.", "'s", "real-estate",
                "and", "shopping-center", "holdings", "." };

        AssertAnnotations.assertToken(tokens, select(jCas, Token.class));
    }

    @Test
    public void testErroneouslyJoinedTokensWithCorrectedTag()
        throws Exception
    {
        JCas jcas = readTestFile("erroneouslyJoinedTokensAndTheirTags.pos");

        String[] posOriginal = { "DT", "NNS", "NNS" };

        String[] posMapped = { "POS_DET", "POS_NOUN", "POS_NOUN" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
    }

    @Test
    public void testDashedWordsTokenization()
        throws Exception
    {
        JCas jcas = readTestFile("generalTest.pos");

        String[] chunks = { 
                "[  0, 12]Chunk(null) (A consortium)",
                "[ 16, 33]Chunk(null) (private investors)",
                "[ 47, 62]Chunk(null) (LJH Funding Co.)", "[ 68, 70]Chunk(null) (it)",
                "[ 80,104]Chunk(null) (a $ 409 million cash bid)", "[109,113]Chunk(null) (most)",
                "[117,149]Chunk(null) (L.J. Hooker Corp. 's real-estate)",
                "[154,178]Chunk(null) (shopping-center holdings)" };

        AssertAnnotations.assertChunks(chunks, select(jcas, Chunk.class));
    }

    /**
     * We annotate only one pos if several exist, the first one mentioned
     */
    @Test
    public void testTokensWithSeveralPossiblePOSTags()
        throws Exception
    {
        JCas jcas = readTestFile("severalPOSToken.pos");

        String[] posOriginal = { "VBG", "NN" };

        String[] posMapped = { "POS_VERB", "POS_NOUN" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
    }

    @Test
    public void testSuppressedTokenAnnotation()
        throws Exception
    {
        // POS/Chunk is set to true, yet it should not be annotated
        JCas jCas = readTestFile("generalTest.pos", false, true, true, true);

        assertEquals(1, select(jCas, Sentence.class).size());
        assertEquals(0, select(jCas, Token.class).size());
        assertEquals(0, select(jCas, POS.class).size());
        assertEquals(0, select(jCas, Chunk.class).size());
    }

    @Test
    public void testSuppressedPosAnnotation()
        throws Exception
    {
        JCas jCas = readTestFile("generalTest.pos", true, false, true, true);

        assertEquals(1, select(jCas, Sentence.class).size());
        assertEquals(32, select(jCas, Token.class).size());
        assertEquals(0, select(jCas, POS.class).size());
        assertEquals(8, select(jCas, Chunk.class).size());
    }

    @Test
    public void testSuppressedSentenceAnnotations()
        throws Exception
    {
        JCas jCas = readTestFile("generalTest.pos", true, true, false, true);

        assertEquals(0, select(jCas, Sentence.class).size());
        assertEquals(32, select(jCas, Token.class).size());
        assertEquals(32, select(jCas, POS.class).size());
        assertEquals(8, select(jCas, Chunk.class).size());
    }

    @Test
    public void testSuppressedChunkAnnotations()
        throws Exception
    {
        JCas jCas = readTestFile("generalTest.pos", true, true, true, false);

        assertEquals(1, select(jCas, Sentence.class).size());
        assertEquals(32, select(jCas, Token.class).size());
        assertEquals(32, select(jCas, POS.class).size());
        assertEquals(0, select(jCas, Chunk.class).size());
    }

    private static JCas readTestFile(String aFile)
        throws Exception
    {
        return readTestFile(aFile, true, true, true, true);
    }

    private static JCas readTestFile(String aFile, boolean readToken, boolean readPos,
            boolean readSent, boolean readChunk)
        throws Exception
    {
        CollectionReader reader = CollectionReaderFactory.createReader(
                PennTreebankChunkedReader.class, PennTreebankChunkedReader.PARAM_LANGUAGE, "en",
                PennTreebankChunkedReader.PARAM_SOURCE_LOCATION,
                "src/test/resources/pennTreebankChunkedReaderTestFiles/",
                PennTreebankChunkedReader.PARAM_POS_TAG_SET, "ptb",
                PennTreebankChunkedReader.PARAM_READ_TOKEN, readToken,
                PennTreebankChunkedReader.PARAM_READ_POS, readPos,
                PennTreebankChunkedReader.PARAM_READ_SENTENCE, readSent,
                PennTreebankChunkedReader.PARAM_READ_CHUNK, readChunk,
                PennTreebankChunkedReader.PARAM_PATTERNS, aFile);

        JCas jcas = JCasFactory.createJCas();
        reader.getNext(jcas.getCas());
        return jcas;
    }
}
