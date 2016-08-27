/*
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
 */
package de.tudarmstadt.ukp.dkpro.core.io.penntree;

import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertConstituents;
import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertPOS;
import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertSentence;
import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertToken;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.junit.Assert.assertEquals;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;

public class PennTreebankCombinedReaderTest
{
    @Test
    public void test()
        throws Exception
    {
        CollectionReader reader = createReader(PennTreebankCombinedReader.class, 
                PennTreebankCombinedReader.PARAM_SOURCE_LOCATION, 
                "src/test/resources/stanfordPennTrees/stanford-english-trees-first2.mrg");
        
        JCas jcas = JCasFactory.createJCas();
        reader.getNext(jcas.getCas());

        String text = "Al Qaida Endorses George W. Bush for President\n" + 
                "Al-Qaeda tries to incite more violence in Iraq\n";
        
        String[] sentences = { "Al Qaida Endorses George W. Bush for President",
                "Al-Qaeda tries to incite more violence in Iraq" };
        
        String[] tokens1 = { "Al", "Qaida", "Endorses", "George", "W.", "Bush", "for", "President" };

        String[] constituentMapped1 = { "Constituent 0,46", "Constituent 0,8", "Constituent 18,32",
                "Constituent 33,46", "Constituent 37,46", "Constituent 9,46", "ROOT 0,46" };

        String[] constituentOriginal1 = { "NP 0,8", "NP 18,32", "NP 37,46", "PP 33,46",
                "ROOT 0,46", "S 0,46", "VP 9,46" };
        
        String[] tokens2 = { "Al-Qaeda", "tries", "to", "incite", "more", "violence", "in", "Iraq" };
        
        String[] constituentMapped2 = { "Constituent 47,55", "Constituent 47,93",
                "Constituent 56,93", "Constituent 62,93", "Constituent 65,93", "Constituent 72,85",
                "Constituent 86,93", "Constituent 89,93", "ROOT 47,93" };

        String[] constituentOriginal2 = { "NP 47,55", "NP 72,85", "NP 89,93", "PP 86,93",
                "ROOT 47,93", "S 47,93", "S 62,93", "VP 56,93", "VP 62,93", "VP 65,93" };

        assertEquals(text, jcas.getDocumentText());
        assertSentence(sentences, select(jcas, Sentence.class));
        
        Sentence[] actualSentences = select(jcas, Sentence.class).toArray(new Sentence[0]);
        
        assertToken(tokens1, selectCovered(Token.class, actualSentences[0]));
        assertConstituents(constituentMapped1, constituentOriginal1,
                selectCovered(Constituent.class, actualSentences[0]));

        assertToken(tokens2, selectCovered(Token.class, actualSentences[1]));
        assertConstituents(constituentMapped2, constituentOriginal2,
                selectCovered(Constituent.class, actualSentences[1]));
    }
    
    @Test
    public void testWithDirectSpeech()
        throws Exception
    {
        CollectionReader reader = createReader(PennTreebankCombinedReader.class, 
                PennTreebankCombinedReader.PARAM_LANGUAGE, "en",
                PennTreebankCombinedReader.PARAM_SOURCE_LOCATION, 
                "src/test/resources/stanfordPennTrees/tree_with_direct_speech.mrg");
        
        JCas jcas = JCasFactory.createJCas();
        reader.getNext(jcas.getCas());

        String[] sentences = { "`` And what do you know ? ''" };
        
        String[] tokens = { "``", "And", "what", "do", "you", "know", "?", "''" };

        String[] posMapped = { "PUNCT", "CONJ", "PRON", "VERB", "PRON", "VERB", "PUNCT", "PUNCT" };

        String[] posOriginal = { "``", "CC", "WP", "VBP", "PRP", "VB", ".", "''" };

        String[] constituentMapped = { "NP 15,18", "ROOT 0,28", "SBARQ 0,28", "SQ 12,23",
                "VP 19,23", "WHNP 7,11" };

        String[] constituentOriginal = { "NP 15,18", "ROOT 0,28", "SBARQ 0,28", "SQ 12,23",
                "VP 19,23", "WHNP 7,11" };

        assertSentence(sentences, select(jcas, Sentence.class));
        assertToken(tokens, select(jcas, Token.class));
        assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
    }

    @Test
    public void testWithParentheses()
        throws Exception
    {
        CollectionReader reader = createReader(PennTreebankCombinedReader.class, 
                PennTreebankCombinedReader.PARAM_LANGUAGE, "en",
                PennTreebankCombinedReader.PARAM_SOURCE_LOCATION, 
                "src/test/resources/stanfordPennTrees/tree_with_parentheses.mrg");
        
        JCas jcas = JCasFactory.createJCas();
        reader.getNext(jcas.getCas());

        String[] sentences = { "( CNN ) ." };
        
        String[] tokens = { "(", "CNN", ")", "." };

        String[] posMapped = { "PUNCT", "PROPN", "PUNCT", "PUNCT" };

        String[] posOriginal = { "-LRB-", "NNP", "-RRB-", "." };
        
        String[] constituentMapped = { "FRAG 0,9", "NP 2,5", "ROOT 0,9" };

        String[] constituentOriginal = { "FRAG 0,9", "NP 2,5", "ROOT 0,9" };

        assertSentence(sentences, select(jcas, Sentence.class));
        assertToken(tokens, select(jcas, Token.class));
        assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        assertConstituents(constituentMapped, constituentOriginal, select(jcas, Constituent.class));
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
    
    @Before
    public void setupLogging()
    {
        System.setProperty("org.apache.uima.logger.class", "org.apache.uima.util.impl.Log4jLogger_impl");
    }
}
