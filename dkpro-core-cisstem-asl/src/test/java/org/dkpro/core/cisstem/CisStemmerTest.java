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
package org.dkpro.core.cisstem;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.jcas.JCas;
import org.dkpro.core.cisstem.CisStemmer;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class CisStemmerTest {

    @Test
    public void testGerman()
        throws Exception
    {
        runTest("de", "Automobile Fenster", 
                new String[] {"Automobil", "Fenst"} );
    }

    @Test
    public void testCaseInsensitive()
        throws Exception
    {
        runTest("de", "Automobile Fenster", 
                new String[] {"automobil", "fen"},
                CisStemmer.PARAM_LOWER_CASE, true);
    }

  
    private JCas runTest(String aLanguage, String aText, String[] aStems, Object... aParams)
        throws Exception
    {
        JCas result = TestRunner.runTest(createEngineDescription(CisStemmer.class, aParams),
                aLanguage, aText);

        AssertAnnotations.assertStem(aStems, select(result, Stem.class));
        
        return result;
    }
}
