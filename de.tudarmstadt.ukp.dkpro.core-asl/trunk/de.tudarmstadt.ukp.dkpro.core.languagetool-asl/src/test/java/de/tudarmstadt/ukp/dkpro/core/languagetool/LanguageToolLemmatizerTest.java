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
package de.tudarmstadt.ukp.dkpro.core.languagetool;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitive;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class LanguageToolLemmatizerTest
{
	@Test
	public void testGerman()
		throws Exception
	{
        runTest("de", "Das ist ein Test .",
        		new String[] { "der",   "sein",  "ein", "Test", "."    });
	}

	@Test
	public void testEnglish()
		throws Exception
	{
        runTest("en", "This is a test .",
				new String[] { "this", "be",  "a",   "test", "."    });

        runTest("en", "A neural net .",
        		new String[] { "a",   "neural", "net", "."    });

        runTest("en", "John is purchasing oranges .",
        		new String[] { "John", "be",  "purchasing", "orange", "."    });
    }

	private void runTest(String language, String testDocument, String[] aLemma)
		throws Exception
	{
		AnalysisEngine engine = createPrimitive(LanguageToolLemmatizer.class);

		JCas jcas = TestRunner.runTest(engine, language, testDocument);

		AssertAnnotations.assertLemma(aLemma, select(jcas, Lemma.class));
	}

	@Rule
	public TestName name = new TestName();

	@Before
	public void printSeparator()
	{
		System.out.println("\n=== " + name.getMethodName() + " =====================");
	}
}
