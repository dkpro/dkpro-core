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
package de.tudarmstadt.ukp.dkpro.core.api.lexmorph.pos;

import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS_NOUN;

public class POSUtilsTest
{
    
    @Test
    public void testAssignCoarseValue() throws Exception {
        JCas jcas = JCasFactory.createJCas();
        POS posPos = new POS(jcas);
        POS posNoun = new POS_NOUN(jcas);
        
        POSUtils.assignCoarseValue(null);
        POSUtils.assignCoarseValue(posPos);
        POSUtils.assignCoarseValue(posNoun);
        
        Assert.assertNull(posPos.getCoarseValue());
        Assert.assertEquals("NOUN", posNoun.getCoarseValue());
        
    }
}
