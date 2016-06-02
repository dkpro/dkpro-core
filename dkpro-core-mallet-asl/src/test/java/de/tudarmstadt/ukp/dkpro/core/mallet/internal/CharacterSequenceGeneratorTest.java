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
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import java.util.List;

import static de.tudarmstadt.ukp.dkpro.core.mallet.internal.AnnotationSequenceGeneratorTest.jCasWithTokens;
import static de.tudarmstadt.ukp.dkpro.core.mallet.internal.AnnotationSequenceGeneratorTest.jcasWithSentence;
import static org.junit.Assert.assertEquals;

public class CharacterSequenceGeneratorTest
{
    @Test
    public void testCharacterSequence()
            throws UIMAException, FeaturePathException
    {
        JCas jcas = jCasWithTokens();
        int expectedSize = 13;
        String expectedFirst = "T";
        String expectedLast = "2";

        CharacterSequenceGenerator tsg = new CharacterSequenceGenerator();

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
        CharacterSequenceGenerator tsg = new CharacterSequenceGenerator();
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

        TokenSequenceGenerator tsg = new CharacterSequenceGenerator();
        tsg.setCoveringTypeName(covering);

        List<TokenSequence> tokenSequences = tsg.tokenSequences(jCas);
        assertEquals(expectedSequences, tokenSequences.size());
        TokenSequence ts = tokenSequences.get(0);
        assertEquals(expectedSize, ts.size());
        assertEquals(expectedFirst, ts.get(0).getText());
        assertEquals(expectedLast, ts.get(expectedSize - 1).getText());
    }
}
