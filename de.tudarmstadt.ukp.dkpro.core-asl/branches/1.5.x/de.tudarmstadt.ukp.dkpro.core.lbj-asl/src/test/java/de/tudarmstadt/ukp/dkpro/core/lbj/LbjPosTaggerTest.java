/*******************************************************************************
 * Copyright 2010
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
package de.tudarmstadt.ukp.dkpro.core.lbj;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class LbjPosTaggerTest
{
    @Test
    public void testEnglish()
        throws Exception
    {
        runTest("en", null, "This is a test . \n",
                new String[] { "DT",   "VBZ", "DT",  "NN",   "." },
                new String[] { "ART",  "V",   "ART", "NN",   "PUNC" });

        runTest("en", null, "A neural net . \n",
                new String[] { "DT",  "NN",     "NN",  "." },
                new String[] { "ART", "NN",    "NN",  "PUNC" });

        runTest("en", null, "John is purchasing oranges . \n",
                new String[] { "NNP",  "VBZ", "VBG",      "NNS",    "." },
                new String[] { "NP",   "V",   "V",        "NN",     "PUNC" });
    }

    private void runTest(String language, String variant, String testDocument, String[] tags,
            String[] tagClasses)
        throws Exception
    {
        AnalysisEngine engine = createEngine(LbjPosTagger.class);

        JCas jcas = TestRunner.runTest(engine, language, testDocument);

        AssertAnnotations.assertPOS(tagClasses, tags, select(jcas, POS.class));
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}
