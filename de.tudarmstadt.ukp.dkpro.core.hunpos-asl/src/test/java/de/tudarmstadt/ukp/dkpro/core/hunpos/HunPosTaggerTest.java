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
package de.tudarmstadt.ukp.dkpro.core.hunpos;

import static org.apache.commons.lang.StringUtils.repeat;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.testing.util.HideOutput;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class HunPosTaggerTest
{
    @Test
    public void testCroatian()
        throws Exception
    {
        runTest("hr", null, "Ovo je test . \n",
                new String[] { "Pd-nsn--n-a", "Vcr3s", "N-msan", "Z" },
                new String[] { "POS",  "POS", "POS",  "POS" });
    }
    
    @Test
	public void testEnglish()
		throws Exception
	{
        runTest("en", null, "This is a test . \n",
				new String[] { "DT",   "VBZ", "DT",  "NN",   "." },
				new String[] { "ART",  "V",   "ART", "NN",   "PUNC" });

        runTest("en", null, "A neural net . \n",
        		new String[] { "DT",  "JJ",     "NN",  "." },
        		new String[] { "ART", "ADJ",    "NN",  "PUNC" });

        runTest("en", null, "John is purchasing oranges . \n",
        		new String[] { "NNP",  "VBZ", "VBG",      "NNS",    "." },
        		new String[] { "NP",   "V",   "V",        "NN",     "PUNC" });
    }

    @Test
    public void testFarsi()
        throws Exception
    {
        runTest("fa", null, "این یک تست است . \n",
                new String[] { "DET", "PRO", "N_SING", "V_COP", "DELM" },
                new String[] { "ART", "PR",  "N",      "V",     "PUNC" });
    }
	
    @Test
    public void testHungarian()
        throws Exception
    {
        runTest("hu", null, "Ez egy teszt . \n",
                new String[] { "NOUN", "ART", "NOUN", "PUNCT" },
                new String[] { "POS",  "POS", "POS",  "POS" });
    }

    @Test
    public void testPortuguese()
        throws Exception
    {
        runTest("pt", null, "Este é um teste . \n",
                new String[] { "PROSUB", "V",   "ART", "N",   "." },
                new String[] { "POS",    "POS", "POS", "POS", "POS" });
    }
    
    @Test
    public void testSwedish()
        throws Exception
    {
        runTest("sv", null, "Detta är ett test . \n",
                new String[] { "PN_NEU_SIN_DEF_SUB/OBJ", "VB_PRS_AKT", "DT_NEU_SIN_IND", "NN_NEU_SIN_IND_NOM", "DL_MAD"    },
                new String[] { "O", "O", "O", "O", "O" });
    }

    @Test
//  @Ignore("Platform specific")
    public void testOddCharacters()
        throws Exception
    {
        runTest("en", null, "² § ¶ § °",
                new String[] { "NNP", "NNP", "NNP", "NNP", "NNP" },
                new String[] { "NP", "NP", "NP", "NP", "NP"});
    }

    /**
     * Generate a very large document and test it.
     */
    @Test
    @Ignore("Takes too long")
    public void hugeDocumentTest()
        throws Exception
    {
        // Start Java with -Xmx512m
        boolean run = Runtime.getRuntime().maxMemory() > (500000000);
        if (!run) {
            System.out.println("Test requires more heap than available, skipping");
        }
        Assume.assumeTrue(run);

        String text = "This is a test .\n";
        int reps = 4000000 / text.length();
        String testString = repeat(text, " ", reps);

        AnalysisEngineDescription engine = createEngineDescription(HunPosTagger.class);
        JCas jcas = TestRunner.runTest(engine, "en", testString);
        List<POS> actualTags = new ArrayList<POS>(select(jcas, POS.class));
        assertEquals(reps * 5, actualTags.size());

        // test POS annotations
        String[] expectedTags = new String[] { "DT",   "VBZ", "DT",  "NN",   "." };
        String[] expectedTagClasses = new String[] { "ART",  "V",   "ART", "NN",   "PUNC" };

        for (int i = 0; i < actualTags.size(); i++) {
            POS posAnnotation = actualTags.get(i);
            assertEquals("In position "+i, expectedTagClasses[i%5], posAnnotation.getType().getShortName());
            assertEquals("In position "+i, expectedTags[i%5], posAnnotation.getPosValue());
        }

        System.out.println("Successfully tagged document with " + testString.length() +
                " characters");
    }

    /**
     * Test using the same AnalysisEngine multiple times.
     */
    @Test
    @Ignore("Takes too long")
    public void multiDocumentTest()
        throws Exception
    {
        String testDocument = "This is a test .";
        String[] tags       = new String[] { "DT",   "VBZ", "DT",  "NN",   "." };
        String[] tagClasses = new String[] { "ART",  "V",   "ART", "NN",   "PUNC" };

        AnalysisEngine engine = createEngine(HunPosTagger.class);

        HideOutput hideOut = new HideOutput();
        try {
            for (int n = 0; n < 100; n++) {
                JCas aJCas = TestRunner.runTest(engine, "en", testDocument);

                AssertAnnotations.assertPOS(tagClasses, tags, select(aJCas, POS.class));
            }
        }
        finally {
            engine.destroy();
            hideOut.restoreOutput();
        }
    }
	
	private void runTest(String language, String variant, String testDocument, String[] tags,
			String[] tagClasses)
		throws Exception
	{
		AnalysisEngine engine = createEngine(HunPosTagger.class,
		        HunPosTagger.PARAM_VARIANT, variant,
		        HunPosTagger.PARAM_PRINT_TAGSET, true);

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
