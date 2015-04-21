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

public class TestContainsNumber
{
    JCas jcas = null;
    TextClassificationUnit tcu_pureNum = null;
    TextClassificationUnit tcu_mixedNum = null;
    TextClassificationUnit tcu_noNum=null;

    @Test
    public void testNonNumber()
        throws Exception
    {
        ContainsNumber featExtractor = new ContainsNumber();
        List<Feature> extract = featExtractor.extract(jcas, tcu_noNum);
        assertEquals(1, extract.size());
        assertEquals(0, extract.get(0).getValue());
    }
    
    @Test
    public void testPureNumber()
        throws Exception
    {
        ContainsNumber isNum = new ContainsNumber();
        List<Feature> extract = isNum.extract(jcas, tcu_pureNum);
        assertEquals(1, extract.size());
        assertEquals(1, extract.get(0).getValue());
    }

    
    @Test
    public void testMixedNumber()
        throws Exception
    {
        ContainsNumber isNum = new ContainsNumber();
        List<Feature> extract = isNum.extract(jcas, tcu_mixedNum);
        assertEquals(1, extract.size());
        assertEquals(1, extract.get(0).getValue());
    }

    @Before
    public void setup()
        throws Exception
    {
        jcas = JCasFactory.createJCas();
        jcas.setDocumentText("From 1 to 3. On 24th of May.");

        Sentence s = new Sentence(jcas, 0, 12);
        s.addToIndexes();
        s = new Sentence(jcas, 13, 28);
        s.addToIndexes();

        tcu_noNum = new TextClassificationUnit(jcas, 0, 4);
        tcu_noNum.addToIndexes();
        tcu_pureNum = new TextClassificationUnit(jcas, 5, 6);
        tcu_pureNum.addToIndexes();
        tcu_mixedNum = new TextClassificationUnit(jcas, 16, 20);
        tcu_mixedNum.addToIndexes();
    }

}
