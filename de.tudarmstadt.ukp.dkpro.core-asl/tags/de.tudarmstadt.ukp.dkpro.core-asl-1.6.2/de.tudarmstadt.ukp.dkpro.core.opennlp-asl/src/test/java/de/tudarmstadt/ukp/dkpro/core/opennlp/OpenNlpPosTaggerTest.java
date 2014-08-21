/*******************************************************************************
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.opennlp;

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

public class OpenNlpPosTaggerTest
{
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
	public void testGerman()
		throws Exception
    {
        runTest("de", null, "Das ist ein Test .",
        		new String[] { "PDS", "VAFIN", "ART", "NN",   "$."    },
        		new String[] { "PR",  "V",     "ART", "NN",   "PUNC" });

        runTest("de", "maxent", "Das ist ein Test .",
        		new String[] { "PDS", "VAFIN", "ART", "NN",   "$."    },
        		new String[] { "PR",  "V",     "ART", "NN",   "PUNC" });

        runTest("de", "perceptron", "Das ist ein Test .",
        		new String[] { "PDS", "VAFIN", "ART", "NN",   "$."    },
        		new String[] { "PR",  "V",     "ART", "NN",   "PUNC" });
    }

    @Test
    public void testItalian()
        throws Exception
    {
        runTest("it", null, "Questo è un test .",
                new String[] { "PD", "Vip3", "RI",  "Sn", "FS"    },
                new String[] { "PR", "V",    "ART", "NN", "PUNC" });
        
        runTest("it", "perceptron", "Questo è un test .",
                new String[] { "PD", "Vip3", "RI",  "Sn", "FS"    },
                new String[] { "PR", "V",    "ART", "NN", "PUNC" });
    }
		
	@Test
	public void testSpanish()
		throws Exception
    {
        runTest("es", "maxent", "Esta es una prueba .",
        		new String[] { "PD", "VSI", "DI", "NC", "Fp"    },
        		new String[] { "POS", "POS", "POS", "POS", "POS" });
    }

   @Test
    public void testSwedish()
        throws Exception
    {
        runTest("sv", "maxent", "Detta är ett test .",
                new String[] { "PO",  "AV",  "EN",  "NN",  "IP"    },
                new String[] { "POS", "POS", "POS", "POS", "POS" });
    }

	private void runTest(String language, String variant, String testDocument, String[] tags,
			String[] tagClasses)
		throws Exception
	{
		AnalysisEngine engine = createEngine(OpenNlpPosTagger.class,
				OpenNlpPosTagger.PARAM_VARIANT, variant,
				OpenNlpPosTagger.PARAM_PRINT_TAGSET, true);

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
