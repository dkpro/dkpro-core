/*******************************************************************************
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.cogroo;

import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertNamedEntity;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class CogrooNameFinderTest
{
    /**
     * The CogRoo name finder is a bit strange because it appears to find only multi-word named
     * entities. It also doesn't classify them.
     */
    @Test
    public void testPortuguese()
        throws Exception
    {
        JCas jcas = runTest("pt-BR", "Maria Gomez está viva. Fernando Pessoa morreu .");

        String[] namedEntities = new String[] { 
                "[  0, 11]NamedEntity(P) (Maria Gomez)", 
                "[ 23, 38]NamedEntity(P) (Fernando Pessoa)" };
        
        assertNamedEntity(namedEntities, select(jcas, NamedEntity.class));
    }
    
    private JCas runTest(String aLanguage, String aDocument)
        throws Exception
    {
        AnalysisEngineDescription desc = createEngineDescription(
                CogrooNameFinder.class
                //CogrooNameFinder.PARAM_PRINT_TAGSET, true
                );

        return TestRunner.runTest(desc, aLanguage, aDocument);
    }
}
