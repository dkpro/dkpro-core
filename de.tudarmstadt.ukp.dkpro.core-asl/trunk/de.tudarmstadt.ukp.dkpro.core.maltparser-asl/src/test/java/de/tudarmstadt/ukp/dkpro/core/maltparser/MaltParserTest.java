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
package de.tudarmstadt.ukp.dkpro.core.maltparser;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitiveDescription;
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

/**
 * @author Oliver Ferschke
 * @author Richard Eckart de Castilho
 */
public class MaltParserTest
{
	static final String documentEnglish = "We need a very complicated example sentence , which " +
			"contains as many constituents and dependencies as possible .";

	@Test
	public void testEnglishDependenciesDefault()
		throws Exception
	{
		JCas jcas = runTest("en", null, documentEnglish);

		String[] dependencies = new String[] { "ADVMOD 15,26,10,14", "AMOD 35,43,15,26",
				"AMOD 69,81,64,68", "CC 69,81,82,85", "CONJ 69,81,86,98", "DET 35,43,8,9",
				"DOBJ 3,7,35,43", "NN 35,43,27,34", "NSUBJ 3,7,0,2", "NSUBJ 52,60,46,51",
				"POBJ 61,63,69,81", "POBJ 99,101,102,110", "PREP 52,60,61,63", "PREP 69,81,99,101",
				"PUNCT 3,7,111,112", "PUNCT 35,43,44,45", "RCMOD 35,43,52,60" };

		AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
	}

	@Test
	public void testEnglishDependenciesLinear()
		throws Exception
	{
		JCas jcas = runTest("en", "linear", documentEnglish);

		String[] dependencies = new String[] { "ADVMOD 15,26,10,14", "AMOD 35,43,15,26",
				"AMOD 69,81,64,68", "CC 69,81,82,85", "CONJ 69,81,86,98", "DET 35,43,8,9",
				"DOBJ 3,7,35,43", "NN 35,43,27,34", "NSUBJ 3,7,0,2", "NSUBJ 52,60,46,51",
				"POBJ 61,63,69,81", "POBJ 99,101,102,110", "PREP 52,60,61,63", "PREP 69,81,99,101",
				"PUNCT 3,7,111,112", "PUNCT 35,43,44,45", "RCMOD 35,43,52,60" };

		AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
	}

	@Test
	public void testEnglishDependenciesPoly()
		throws Exception
	{
		JCas jcas = runTest("en", "poly", documentEnglish);

		String[] dependencies = new String[] { "ADVMOD 15,26,10,14", "AMOD 35,43,15,26",
				"AMOD 69,81,64,68", "CC 69,81,82,85", "CONJ 69,81,86,98", "DET 35,43,8,9",
				"DOBJ 3,7,35,43", "NN 35,43,27,34", "NSUBJ 3,7,0,2", "NSUBJ 52,60,46,51",
				"POBJ 61,63,69,81", "POBJ 99,101,102,110", "PREP 52,60,61,63", "PREP 69,81,99,101",
				"PUNCT 3,7,111,112", "PUNCT 35,43,44,45", "RCMOD 35,43,52,60" };

		AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
	}

	@Test
	public void testGermanDependencies()
		throws Exception
	{
		checkModel("de", "linear");

		String documentGerman = "Wir brauchen ein sehr kompliziertes Beispiel , welches möglichst "
				+ "viele Konstituenten und Dependenzen beinhaltet .";

		JCas jcas = runTest("de", "linear", documentGerman);

		String[] dependencies = new String[] { "-PUNCT- 89,100,112,113", "DET 13,16,17,21",
				"DET 36,44,22,35", "DET 4,12,13,16", "DET 45,46,47,54", "DET 47,54,55,64",
				"DET 55,64,65,70", "DET 65,70,71,84", "DET 71,84,85,88", "DET 85,88,89,100",
				"GMOD 17,21,36,44", "KON 4,12,45,46", "PN 112,113,101,111", "ROOT 0,3,4,12" };

		AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
	}

//	@Ignore("Unfortunately we have no part-of-speech tagger for Swedish")
//	@Test
//	public void testSwedishDependencies()
//		throws Exception
//	{
//		String document = "Vi behöver en mycket komplicerad exempel meningen som innehåller lika " +
//				"många beståndsdelar och beroenden som möjligt.";
//		String model = "sv-linear";
//
//		AnalysisEngineDescription pipeline = getPipeline(model);
//
//		JCas jcas = JCasFactory.createJCas();
//		jcas.setDocumentText(document);
//		jcas.setDocumentLanguage("sv");
//
//		runPipeline(jcas, pipeline);
//
//		Set<String> expected = new HashSet<String>();
//		expected.add("SS 0,2,3,10");
//		expected.add("SS 3,10,21,32");
//		expected.add("TA 21,32,41,49");
//		expected.add("ET 50,53,54,64");
//		expected.add("DT 76,89,70,75");
//		expected.add("ET 76,89,90,93");
//		expected.add("DT 108,115,104,107");
//		expected.add("ET 108,115,115,116");
//		expected.add("PA 94,103,108,115");
//		expected.add("AA 90,93,94,103");
//		expected.add("PA 54,64,76,89");
//		expected.add("DT 70,75,65,69");
//		expected.add("SP 41,49,50,53");
//		expected.add("HD 21,32,33,40");
//		expected.add("DT 21,32,14,20");
//
//		assertDependencies(jcas, expected);
//	}
//
//	@Ignore("The tags produced by our French TreeTagger model are different form the ones that " +
//			"the pre-trained MaltParser model expects. Also the input format in our MaltParser " +
//			"class is currently hardcoded to the format used by the English pre-trained model. " +
//			"For the French model the 5th column of the input format should contain fine-grained " +
//			"tags. See http://www.maltparser.org/mco/french_parser/fremalt.html")
//	@Test
//	public void testFrenchDependencies()
//		throws Exception
//	{
//		String document = "Nous avons besoin d'une phrase par exemple très compliqué, qui " +
//				"contient des constituants que de nombreuses dépendances et que possible.";
//		String model = "fr-linear";
//
//		AnalysisEngineDescription pipeline = getPipeline(model);
//
//		JCas jcas = JCasFactory.createJCas();
//		jcas.setDocumentText(document);
//		jcas.setDocumentLanguage("fr");
//
//		runPipeline(jcas, pipeline);
//
//		Set<String> expected = new HashSet<String>();
//		expected.add("DEP 24,30,31,34");
//		expected.add("DEP 43,47,57,58");
//		expected.add("MOD 59,62,72,75");
//		expected.add("MOD 107,118,119,121");
//		expected.add("MOD 107,118,126,134");
//		expected.add("MOD 107,118,134,135");
//		expected.add("MOD 107,118,122,125");
//		expected.add("MOD 76,88,89,92");
//		expected.add("MOD 59,62,63,71");
//		expected.add("DEP 43,47,48,57");
//
//		assertDependencies(jcas, expected);
//	}
//

	private JCas runTest(String aLanguage, String aVariant, String aText)
		throws Exception
	{
		AnalysisEngineDescription engine = createAggregateDescription(
				createPrimitiveDescription(OpenNlpPosTagger.class),
				createPrimitiveDescription(MaltParser.class,
						MaltParser.PARAM_VARIANT, aVariant,
						MaltParser.PARAM_PRINT_TAGSET, true),
				createPrimitiveDescription(DependencyDumper.class));

		return TestRunner.runTest(engine, aLanguage, aText);
	}

	private void checkModel(String aLanguage, String aVariant)
	{
		Assume.assumeTrue(getClass().getResource(
				"/de/tudarmstadt/ukp/dkpro/core/maltparser/lib/parser-" + aLanguage
						+ "-"+aVariant+".mco") != null);
	}

	@Rule public TestName testName = new TestName();
	@Before
	public void printSeparator()
	{
		System.out.println("\n=== "+testName.getMethodName()+" =====================");
	}
}
