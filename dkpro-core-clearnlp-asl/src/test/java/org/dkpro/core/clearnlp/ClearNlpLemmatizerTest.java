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
package org.dkpro.core.clearnlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.clearnlp.ClearNlpLemmatizer;
import org.dkpro.core.clearnlp.ClearNlpPosTagger;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.TestRunner;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;

public class ClearNlpLemmatizerTest
{
    @Test
    public void testEnglish()
        throws Exception
    {
        // Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1200000000l);

        JCas jcas = runTest("en", "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String[] lemmas = { "we", "need", "a", "very", "complicated", "example", "sentence", ",",
                "which", "contain", "as", "many", "constituent", "and", "dependency", "as",
                "possible", "." };

        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
    }

    @Test
    public void testUnderscore()
        throws Exception
    {
        JCas jcas = runTest("en", "foo _ bar");

        String[] lemmas = { "foo", "_", "bar" };
        
        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
    }

    private JCas runTest(String aLanguage, String aText)
        throws Exception
    {
        AnalysisEngineDescription tagger = createEngineDescription(ClearNlpPosTagger.class);
        AnalysisEngineDescription lemma = createEngineDescription(ClearNlpLemmatizer.class);

        JCas jcas = TestRunner.runTest(createEngineDescription(tagger, lemma), aLanguage, aText);

        return jcas;
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
