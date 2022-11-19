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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.TestRunner;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;

public class ClearNlpPosTaggerTest
{
    @Test
    public void testEnglish()
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1200000000l);

        runTest("en", null, "This is a test . \n",
                new String[] { "DT",  "VBZ",  "DT",  "NN",   "." },
                new String[] { "POS_DET", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" });

        runTest("en", null, "A neural net . \n",
                new String[] { "DT",  "JJ",  "NN",   "." },
                new String[] { "POS_DET", "POS_ADJ", "POS_NOUN", "POS_PUNCT" });

        runTest("en", null, "John is purchasing oranges . \n",
                new String[] { "NNP",  "VBZ",  "VBG",  "NNS",  "." },
                new String[] { "POS_PROPN", "POS_VERB", "POS_VERB", "POS_NOUN", "POS_PUNCT" });
    }

    @Test
    public void testEnglishMayo()
        throws Exception
    {
        runTest("en", "mayo", "This is a test . \n",
                new String[] { "DT",  "VBZ",  "DT",  "NN",   "." },
                new String[] { "POS_DET", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" });

        runTest("en", "mayo", "A neural net . \n",
                new String[] { "DT",  "JJ",  "NN",   "." },
                new String[] { "POS_DET", "POS_ADJ", "POS_NOUN", "POS_PUNCT" });

        runTest("en", "mayo", "John is purchasing oranges . \n",
                new String[] { "NNP",  "VBZ",  "VBG",  "NNS",  "." },
                new String[] { "POS_PROPN", "POS_VERB", "POS_VERB", "POS_NOUN", "POS_PUNCT" });
    }

    private void runTest(String language, String variant, String testDocument, String[] tags,
            String[] tagClasses)
        throws Exception
    {
        AnalysisEngine engine = createEngine(ClearNlpPosTagger.class,
                ClearNlpPosTagger.PARAM_VARIANT, variant,
                ClearNlpPosTagger.PARAM_PRINT_TAGSET, true);

        JCas jcas = TestRunner.runTest(engine, language, testDocument);

        AssertAnnotations.assertPOS(tagClasses, tags, select(jcas, POS.class));
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();

    @Before
    public void clearMemory()
    {
        Runtime.getRuntime().gc();
        Runtime.getRuntime().gc();
        Runtime.getRuntime().gc();
    }
}
