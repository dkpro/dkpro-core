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
package de.tudarmstadt.ukp.dkpro.core.flextag.features.character;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

public class TestIsAllCapitalized
{
    JCas jcas = null;
    TextClassificationUnit tcu_firstLetterCapital = null;
    TextClassificationUnit tcu_allCapital = null;
    TextClassificationUnit tcu_nonCapital = null;

    @Test
    public void testFirstLetterCapitalized()
        throws Exception
    {
        IsAllCapitalized featExtractor = new IsAllCapitalized();
        List<Feature> extract = featExtractor.extract(jcas, tcu_firstLetterCapital);

        assertEquals(1, extract.size());
        assertEquals(0, extract.get(0).getValue());
    }

    @Test
    public void testAllCapitalized()
        throws Exception
    {
        IsAllCapitalized featExtractor = new IsAllCapitalized();
        List<Feature> extract = featExtractor.extract(jcas, tcu_allCapital);

        assertEquals(1, extract.size());
        assertEquals(1, extract.get(0).getValue());
    }

    @Test
    public void testNonCapitalized()
        throws Exception
    {
        IsAllCapitalized featExtractor = new IsAllCapitalized();
        List<Feature> extract = featExtractor.extract(jcas, tcu_nonCapital);

        assertEquals(1, extract.size());
        assertEquals(0, extract.get(0).getValue());
    }

    @Before
    public void setup()
        throws UIMAException
    {
        jcas = JCasFactory.createJCas();
        jcas.setDocumentText("The FERRARIE burns.");

        tcu_firstLetterCapital = new TextClassificationUnit(jcas, 0, 4);
        tcu_firstLetterCapital.addToIndexes();
        tcu_allCapital = new TextClassificationUnit(jcas, 4, 12);
        tcu_allCapital.addToIndexes();
        tcu_nonCapital = new TextClassificationUnit(jcas, 13, 17);
        tcu_nonCapital.addToIndexes();

    }

}
