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

public class TestContainsComma
{
    JCas jcas = null;
    TextClassificationUnit tcu_comma = null;
    TextClassificationUnit tcu_noComma = null;

    @Test
    public void testComma()
        throws Exception
    {
        ContainsComma featExtractor = new ContainsComma();
        List<Feature> extract = featExtractor.extract(jcas, tcu_comma);
        assertEquals(1, extract.size());
        assertEquals(1, extract.get(0).getValue());
    }
    
    @Test
    public void testNoComma()
        throws Exception
    {
        ContainsComma featExtractor = new ContainsComma();
        List<Feature> extract = featExtractor.extract(jcas, tcu_noComma);
        assertEquals(1, extract.size());
        assertEquals(0, extract.get(0).getValue());
    }

    @Before
    public void setup()
        throws Exception
    {
        jcas = JCasFactory.createJCas();
        jcas.setDocumentText("First, one should");

        Sentence s = new Sentence(jcas, 0, 6);
        s.addToIndexes();

        tcu_comma = new TextClassificationUnit(jcas, 0,6);
        tcu_comma.addToIndexes();
        
        tcu_noComma = new TextClassificationUnit(jcas, 7, 10);
        tcu_noComma.addToIndexes();
    }

}
