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

import static org.junit.Assert.*;

import java.util.List;

import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

public class TestContainsDot
{
    JCas jcas = null;
    TextClassificationUnit tcu_dot = null;
    TextClassificationUnit tcu_nonDot = null;

    @Test
    public void testPureNumber()
        throws Exception
    {
        ContainsDot featExtractor = new ContainsDot();
        List<Feature> extract = featExtractor.extract(jcas, tcu_dot);
        assertEquals(1, extract.size());
        assertEquals(1, extract.get(0).getValue());
    }

    @Test
    public void testMixedNumber()
        throws Exception
    {
        ContainsDot featExtractor = new ContainsDot();
        List<Feature> extract = featExtractor.extract(jcas, tcu_nonDot);
        assertEquals(1, extract.size());
        assertEquals(0, extract.get(0).getValue());
    }

    @Before
    public void setup()
        throws Exception
    {
        jcas = JCasFactory.createJCas();
        jcas.setDocumentText("From 1 to 3.");

        Sentence s = new Sentence(jcas, 0, 12);
        s.addToIndexes();

        tcu_dot = new TextClassificationUnit(jcas, 11, 12);
        tcu_dot.addToIndexes();
        tcu_nonDot = new TextClassificationUnit(jcas, 0, 4);
        tcu_nonDot.addToIndexes();
    }

}
