/*******************************************************************************
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.core.flextag.features.token;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;
import java.util.Random;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

public class TestNextToken
{
    JCas jcas = null;
    TextClassificationUnit tcu = null;
    TextClassificationUnit tcu_noNextTokenExists = null;

    @Test
    public void testNextToken()
        throws Exception
    {
        NextToken featExtractor = new NextToken();
        List<Feature> extract = featExtractor.extract(jcas, tcu);

        assertFalse(extract.isEmpty());
        assertEquals(1, extract.size());
        assertEquals(NextToken.FEATURE_NAME, extract.get(0).getName());
        assertEquals("great", extract.get(0).getValue());
    }

    @Test
    public void testEndOfSequence()
        throws Exception
    {
        NextToken next = new NextToken();
        List<Feature> extract = next.extract(jcas, tcu_noNextTokenExists);

        assertFalse(extract.isEmpty());
        assertEquals(1, extract.size());
        assertEquals(NextToken.END_OF_SEQUENCE, extract.get(0).getValue());
    }

    @Before
    public void setUp()
        throws UIMAException
    {
        jcas = JCasFactory.createJCas();
        jcas.setDocumentText("The sun shines. This is great.");
        DocumentMetaData dmd = new DocumentMetaData(jcas);
        dmd.setDocumentId(""+new Random().nextInt());
        dmd.addToIndexes();

        Token t = null;
        t = new Token(jcas, 0, 3); // The
        t.addToIndexes();
        t = new Token(jcas, 4, 7); // sun
        t.addToIndexes();
        t = new Token(jcas, 8, 14); // shines
        t.addToIndexes();
        t = new Token(jcas, 14, 15); // .
        t.addToIndexes();
        Sentence s1 = new Sentence(jcas, 0, 15);
        s1.addToIndexes();

        t = new Token(jcas, 16, 20); // This
        t.addToIndexes();
        t = new Token(jcas, 21, 23); // is
        t.addToIndexes();
        t = new Token(jcas, 24, 29); // great
        t.addToIndexes();
        t = new Token(jcas, 29, 30); // .
        t.addToIndexes();
        Sentence s2 = new Sentence(jcas, 16, 30);
        s2.addToIndexes();

        // target unit which is the current unit
        tcu = new TextClassificationUnit(jcas, 21, 23);
        tcu.addToIndexes();

        TextClassificationUnit otherUnits = null;
        otherUnits = new TextClassificationUnit(jcas, 0, 3);
        otherUnits.addToIndexes();
        otherUnits = new TextClassificationUnit(jcas, 4, 7);
        otherUnits.addToIndexes();
        otherUnits = new TextClassificationUnit(jcas, 8, 14);
        otherUnits.addToIndexes();
        otherUnits = new TextClassificationUnit(jcas, 14, 15);
        otherUnits.addToIndexes();
        otherUnits = new TextClassificationUnit(jcas, 16, 20);
        otherUnits.addToIndexes();
        otherUnits = new TextClassificationUnit(jcas, 21, 23);
        otherUnits.addToIndexes();
        otherUnits = new TextClassificationUnit(jcas, 24, 29);
        otherUnits.addToIndexes();
        tcu_noNextTokenExists = new TextClassificationUnit(jcas, 14, 15);
        tcu_noNextTokenExists.addToIndexes();
    }

}
