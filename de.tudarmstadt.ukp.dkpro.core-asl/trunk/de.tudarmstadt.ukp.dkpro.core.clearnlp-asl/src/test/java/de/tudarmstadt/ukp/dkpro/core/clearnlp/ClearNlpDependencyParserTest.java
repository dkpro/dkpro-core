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
package de.tudarmstadt.ukp.dkpro.core.clearnlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;
import de.tudarmstadt.ukp.dkpro.core.testing.dumper.DependencyDumper;

public class ClearNlpDependencyParserTest
{
	static final String documentEnglish = "We need a very complicated example sentence , which " +
			"contains as many constituents and dependencies as possible .";

	@Test
	public void testEnglishDependencies()
		throws Exception
	{
		Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 3000000000l);

		JCas jcas = runTest("en", null, documentEnglish);

		String[] dependencies = new String[] { "ADVMOD 15,26,10,14", "AMOD 35,43,15,26",
				"AMOD 69,81,64,68", "AMOD 99,101,102,110", "CC 69,81,82,85", "CONJ 69,81,86,98",
				"DET 35,43,8,9", "DOBJ 3,7,35,43", "NN 35,43,27,34", "NSUBJ 3,7,0,2",
				"NSUBJ 52,60,46,51", "POBJ 61,63,69,81", "PREP 52,60,61,63", "PREP 69,81,99,101",
				"PUNCT 3,7,111,112", "PUNCT 35,43,44,45", "RCMOD 35,43,52,60" };

		AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
	}

	@Test
	public void testEnglishMayo()
		throws Exception
	{
//		Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1200000000l);

		JCas jcas = runTest("en", "mayo", documentEnglish);

		String[] dependencies = new String[] { "ADVCL 52,60,102,110", "ADVMOD 15,26,10,14",
				"AMOD 35,43,15,26", "AMOD 69,81,64,68", "CC 69,81,82,85", "CONJ 69,81,86,98",
				"DET 35,43,8,9", "DOBJ 3,7,35,43", "MARK 102,110,99,101", "NN 35,43,27,34",
				"NSUBJ 3,7,0,2", "NSUBJ 52,60,46,51", "POBJ 61,63,69,81", "PREP 52,60,61,63",
				"PUNCT 3,7,111,112", "PUNCT 35,43,44,45", "RCMOD 35,43,52,60" };

		AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
	}

	private JCas runTest(String aLanguage, String aVariant, String aText)
			throws Exception
	{
		AnalysisEngineDescription engine = createEngineDescription(
				createEngineDescription(OpenNlpPosTagger.class),
				createEngineDescription(ClearNlpLemmatizer.class),
				createEngineDescription(ClearNlpDependencyParser.class,
						ClearNlpDependencyParser.PARAM_VARIANT, aVariant,
						ClearNlpDependencyParser.PARAM_PRINT_TAGSET, true),
				createEngineDescription(DependencyDumper.class));

		return TestRunner.runTest(engine, aLanguage, aText);
	}

	@Rule public TestName testName = new TestName();
	@Before
	public void printSeparator()
	{
		Runtime.getRuntime().gc();
		Runtime.getRuntime().gc();
		Runtime.getRuntime().gc();

		System.out.println("\n=== "+testName.getMethodName()+" =====================");
	}
}
