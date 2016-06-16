/*
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
 */
package de.tudarmstadt.ukp.dkpro.core.api.io.sequencegenerator;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static de.tudarmstadt.ukp.dkpro.core.api.io.sequencegenerator.AnnotationStringSequenceGeneratorTest.jCasWithTokens;
import static de.tudarmstadt.ukp.dkpro.core.api.io.sequencegenerator.AnnotationStringSequenceGeneratorTest.jcasWithSentence;
import static org.junit.Assert.assertEquals;

public class CharacterStringSequenceGeneratorTest
{
    @Test
    public void testCharacterSequence()
            throws UIMAException, FeaturePathException
    {
        JCas jcas = jCasWithTokens();
        int expectedSize = 13;
        String expectedFirst = "T";
        String expectedLast = "2";

        StringSequenceGenerator sequenceGenerator = new CharacterStringSequenceGenerator.Builder()
                .build();

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
        StringSequenceGenerator sequenceGenerator = new CharacterStringSequenceGenerator.Builder()
                .lowercase(true)
                .build();

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
        JCas jCas = jcasWithSentence();
        int expectedSequences = 1;
        int expectedSize = 13;
        String expectedFirst = "T";
        String expectedLast = "2";

        StringSequenceGenerator sequenceGenerator = new CharacterStringSequenceGenerator.Builder()
                .coveringType(covering)
                .build();

        List<String[]> sequences = sequenceGenerator.tokenSequences(jCas);
        assertEquals(expectedSequences, sequences.size());
        String[] sequence = sequences.get(0);
        assertEquals(expectedSize, sequence.length);
        assertEquals(expectedFirst, sequence[0]);
        assertEquals(expectedLast, sequence[expectedSize - 1]);
    }
}
