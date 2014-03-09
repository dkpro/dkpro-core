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
        runTest("en", null, "This is a test .",
				new String[] { "DT",   "VBZ", "DT",  "NN",   "." },
				new String[] { "ART",  "V",   "ART", "NN",   "PUNC" });

        runTest("en", null, "A neural net .",
        		new String[] { "DT",  "JJ",     "NN",  "." },
        		new String[] { "ART", "ADJ",    "NN",  "PUNC" });

        runTest("en", null, "John is purchasing oranges .",
        		new String[] { "NNP",  "VBZ", "VBG",      "NNS",    "." },
        		new String[] { "NP",   "V",   "V",        "NN",     "PUNC" });
        
        // This is WRONG tagging. "jumps" is tagged as "NNS"
        runTest("en", "maxent", "The quick brown fox jumps over the lazy dog . \n",
                new String[] { "DT", "JJ", "JJ", "NN", "NNS", "IN", "DT", "JJ", "NN", "." },                
                new String[] { "ART", "ADJ", "ADJ", "NN", "NN", "PP", "ART", "ADJ", "NN", "PUNC" });
        
        runTest("en", "perceptron", "The quick brown fox jumps over the lazy dog . \n",
                new String[] { "DT", "JJ", "JJ", "NN", "NNS", "IN", "DT", "JJ", "NN", "." },                
                new String[] { "ART", "ADJ", "ADJ", "NN", "NN", "PP", "ART", "ADJ", "NN", "PUNC" });
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
    public void testPortuguese()
        throws Exception
    {
        JCas jcas = runTest("pt", null, "Este é um teste .",
                new String[] { "pron-det", "v-fin", "art", "n",   "punc" },
                new String[] { "PR", "V", "ART", "NN", "PUNC" });
        
        runTest("pt", "maxent", "Este é um teste .",
                new String[] { "pron-det", "v-fin", "art", "n",   "punc" },
                new String[] { "PR", "V", "ART", "NN", "PUNC" });

        runTest("pt", "perceptron", "Este é um teste .",
                new String[] { "pron-det", "v-fin", "art", "n",   "punc" },
                new String[] { "PR", "V", "ART", "NN", "PUNC" });

        runTest("pt", "mm-maxent", "Este é um teste .",
                new String[] { "PROSUB", "V",   "ART", "N",   "." },
                new String[] { "POS",    "POS", "POS", "POS", "POS" });

        runTest("pt", "mm-perceptron", "Este é um teste .",
                new String[] { "PROSUB", "V",   "ART", "N",   "." },
                new String[] { "POS",    "POS", "POS", "POS", "POS" });
        
        String[] posTags = new String[] { "?", "adj", "adv", "art", "conj-c", "conj-s", "ec", "in",
                "n", "num", "pp", "pron-det", "pron-indp", "pron-pers", "prop", "prp", "punc",
                "v-fin", "v-ger", "v-inf", "v-pcp", "vp" };

        AssertAnnotations.assertTagset(POS.class, "bosque", posTags, jcas);
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

	private JCas runTest(String language, String variant, String testDocument, String[] tags,
			String[] tagClasses)
		throws Exception
	{
		AnalysisEngine engine = createEngine(OpenNlpPosTagger.class,
				OpenNlpPosTagger.PARAM_VARIANT, variant,
				OpenNlpPosTagger.PARAM_PRINT_TAGSET, true);

		JCas jcas = TestRunner.runTest(engine, language, testDocument);

		AssertAnnotations.assertPOS(tagClasses, tags, select(jcas, POS.class));
		
		return jcas;
	}

	@Rule
	public TestName name = new TestName();

	@Before
	public void printSeparator()
	{
		System.out.println("\n=== " + name.getMethodName() + " =====================");
	}
}
