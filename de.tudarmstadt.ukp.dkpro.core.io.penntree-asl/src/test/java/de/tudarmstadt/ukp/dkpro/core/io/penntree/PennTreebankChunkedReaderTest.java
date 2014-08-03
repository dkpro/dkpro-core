/*******************************************************************************
 * Copyright 2014
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.penntree;

import static org.junit.Assert.assertEquals;
import static org.apache.uima.fit.util.JCasUtil.*;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;

public class PennTreebankChunkedReaderTest
{
    @Test
    public void testCountsOfSentenceTokenTagAnnotation()
        throws Exception
    {
        JCas jCas = readTestFile("generalTest.pos");
        
        assertEquals(1, select(jCas, Sentence.class).size());
        assertEquals(32, select(jCas, Token.class).size());
        assertEquals(32, select(jCas, POS.class).size());
    }

    @Test
    public void testPartOfSpeechTagAssignment()
        throws Exception
    {
        JCas jCas = readTestFile("generalTest.pos");
        
        String[] posOriginal = new String[] { "DT", "NN", "IN", "JJ", "NNS", "VBG", "IN", "NNP",
                "NNP", "NNP", "VBD", "PRP", "VBZ", "VBN", "DT", "$", "CD", "CD", "NN", "NN", "IN",
                "JJS", "IN", "NNP", "NNP", "NNP", "POS", "NN", "CC", "NN", "NNS", "." };

        String[] posMapped = new String[] { "ART", "NN", "PP", "ADJ", "NN", "V", "PP", "NP", "NP",
                "NP", "V", "PR", "V", "V", "ART", "O", "CARD", "CARD", "NN", "NN", "PP", "ADJ",
                "PP", "NP", "NP", "NP", "O", "NN", "CONJ", "NN", "NN", "PUNC" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jCas, POS.class));
    }

    @Test
    public void testTokenBoundaries()
        throws Exception
    {
        JCas jCas = readTestFile("generalTest.pos");
        
        String[] tokens = new String[] { "A", "consortium", "of", "private", "investors", "operating", "as",
                "LJH", "Funding", "Co.", "said", "it", "has", "made", "a", "$", "409",
                "million", "cash", "bid", "for", "most", "of", "L.J.", "Hooker", "Corp.",
                "'s", "real-estate", "and", "shopping-center", "holdings", "." };
        
        AssertAnnotations.assertToken(tokens, select(jCas, Token.class));
    }

    @Test
    public void testErroneouslyJoinedTokensWithCorrectedTag()
        throws Exception
    {
        JCas jcas = readTestFile("erroneouslyJoinedTokensAndTheirTags.pos");
        
        String[] posOriginal = new String[] { "DT", "NNS", "NNS" };

        String[] posMapped = new String[] { "ART", "NN", "NN" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
    }

    @Test
    public void testDashedWordsTokenization()
        throws Exception
    {
        JCas jcas = readTestFile("generalTest.pos");

        String[] chunks = new String[] { 
                "[  0, 12]Chunk(null) (A consortium)",
                "[ 16, 33]Chunk(null) (private investors)",
                "[ 47, 62]Chunk(null) (LJH Funding Co.)", 
                "[ 68, 70]Chunk(null) (it)",
                "[ 80,104]Chunk(null) (a $ 409 million cash bid)", 
                "[109,113]Chunk(null) (most)",
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

        String[] posOriginal = new String[] { "VBG", "NN" };

        String[] posMapped = new String[] { "V", "NN" };
        
        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
    }

    private static JCas readTestFile(String aFile)
        throws Exception
    {
        CollectionReader reader = CollectionReaderFactory.createReader(
                PennTreebankChunkedReader.class, 
                PennTreebankChunkedReader.PARAM_LANGUAGE, "en",
                PennTreebankChunkedReader.PARAM_SOURCE_LOCATION,
                "src/test/resources/pennTreebankChunkedReaderTestFiles/",
                PennTreebankChunkedReader.PARAM_POS_TAGSET, "ptb",
                PennTreebankChunkedReader.PARAM_PATTERNS, aFile );

        JCas jcas = JCasFactory.createJCas();
        reader.getNext(jcas.getCas());
        return jcas;
    }
}
