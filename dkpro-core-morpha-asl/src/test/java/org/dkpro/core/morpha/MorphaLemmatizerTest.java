/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.morpha;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.morpha.MorphaLemmatizer;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class MorphaLemmatizerTest
{
    @Test
    public void testEnglishNoPos()
        throws Exception
    {
        JCas jcas = runTest("en", false, "We need a very complicated example sentence , which " +
            "contains as many constituents and dependencies as possible .");

        String[] lemmas = { "We", "need", "a", "very", "complicate", "example", "sentence", ",",
                "which", "contain", "as", "many", "constituent", "and", "dependency", "as",
                "possible", "." };

        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
    }
    
    @Test
    public void testEnglishWithPos()
        throws Exception
    {
        JCas jcas = runTest("en", true, "We need a very complicated example sentence , which " +
            "contains as many constituents and dependencies as possible .");

        String[] lemmas = { "We", "need", "a", "very", "complicated", "example", "sentence", ",",
                "which", "contain", "as", "many", "constituent", "and", "dependency", "as",
                "possible", "." };

        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
    }
    
    private JCas runTest(String aLanguage, boolean aUsePosTags, String aText)
        throws Exception
    {
        AnalysisEngineDescription engine;
        
        if (aUsePosTags) {
            engine = createEngineDescription(
                    createEngineDescription(OpenNlpPosTagger.class),
                    createEngineDescription(MorphaLemmatizer.class,
                            MorphaLemmatizer.PARAM_READ_POS, true));
        }
        else {
            engine = createEngineDescription(
                    createEngineDescription(MorphaLemmatizer.class));
        }

        return TestRunner.runTest(engine, aLanguage, aText);
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
