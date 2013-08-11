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

		String[] dependencies = new String[] { 
		        "Dependency(advmod)[10,14] D(very)[10,14] G(complicated)[15,26]",
		        "Dependency(amod)[15,26] D(complicated)[15,26] G(sentence)[35,43]",
		        "Dependency(amod)[64,68] D(many)[64,68] G(constituents)[69,81]",
		        "Dependency(cc)[82,85] D(and)[82,85] G(constituents)[69,81]",
		        "Dependency(conj)[86,98] D(dependencies)[86,98] G(constituents)[69,81]",
		        "Dependency(det)[8,9] D(a)[8,9] G(sentence)[35,43]",
		        "Dependency(dobj)[35,43] D(sentence)[35,43] G(need)[3,7]",
		        "Dependency(nn)[27,34] D(example)[27,34] G(sentence)[35,43]",
		        "Dependency(nsubj)[0,2] D(We)[0,2] G(need)[3,7]",
		        "Dependency(nsubj)[46,51] D(which)[46,51] G(contains)[52,60]",
		        "Dependency(pobj)[102,110] D(possible)[102,110] G(as)[99,101]",
		        "Dependency(pobj)[69,81] D(constituents)[69,81] G(as)[61,63]",
		        "Dependency(prep)[61,63] D(as)[61,63] G(contains)[52,60]",
		        "Dependency(prep)[99,101] D(as)[99,101] G(constituents)[69,81]",
		        "Dependency(punct)[111,112] D(.)[111,112] G(need)[3,7]",
		        "Dependency(punct)[44,45] D(,)[44,45] G(sentence)[35,43]",
		        "Dependency(rcmod)[52,60] D(contains)[52,60] G(sentence)[35,43]" };

		AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
	}

	@Test
	public void testEnglishDependenciesLinear()
		throws Exception
	{
		JCas jcas = runTest("en", "linear", documentEnglish);

		String[] dependencies = new String[] { 
		        "Dependency(advmod)[10,14] D(very)[10,14] G(complicated)[15,26]",
		        "Dependency(amod)[15,26] D(complicated)[15,26] G(sentence)[35,43]",
		        "Dependency(amod)[64,68] D(many)[64,68] G(constituents)[69,81]",
		        "Dependency(cc)[82,85] D(and)[82,85] G(constituents)[69,81]",
		        "Dependency(conj)[86,98] D(dependencies)[86,98] G(constituents)[69,81]",
		        "Dependency(det)[8,9] D(a)[8,9] G(sentence)[35,43]",
		        "Dependency(dobj)[35,43] D(sentence)[35,43] G(need)[3,7]",
		        "Dependency(nn)[27,34] D(example)[27,34] G(sentence)[35,43]",
		        "Dependency(nsubj)[0,2] D(We)[0,2] G(need)[3,7]",
		        "Dependency(nsubj)[46,51] D(which)[46,51] G(contains)[52,60]",
		        "Dependency(pobj)[102,110] D(possible)[102,110] G(as)[99,101]",
		        "Dependency(pobj)[69,81] D(constituents)[69,81] G(as)[61,63]",
		        "Dependency(prep)[61,63] D(as)[61,63] G(contains)[52,60]",
		        "Dependency(prep)[99,101] D(as)[99,101] G(constituents)[69,81]",
		        "Dependency(punct)[111,112] D(.)[111,112] G(need)[3,7]",
		        "Dependency(punct)[44,45] D(,)[44,45] G(sentence)[35,43]",
		        "Dependency(rcmod)[52,60] D(contains)[52,60] G(sentence)[35,43]" };

		AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
	}

	@Test
	public void testEnglishDependenciesPoly()
		throws Exception
	{
		JCas jcas = runTest("en", "poly", documentEnglish);

		String[] dependencies = new String[] { 
		        "Dependency(advmod)[10,14] D(very)[10,14] G(complicated)[15,26]",
		        "Dependency(amod)[15,26] D(complicated)[15,26] G(sentence)[35,43]",
		        "Dependency(amod)[64,68] D(many)[64,68] G(constituents)[69,81]",
		        "Dependency(cc)[82,85] D(and)[82,85] G(constituents)[69,81]",
		        "Dependency(conj)[86,98] D(dependencies)[86,98] G(constituents)[69,81]",
		        "Dependency(det)[8,9] D(a)[8,9] G(sentence)[35,43]",
		        "Dependency(dobj)[35,43] D(sentence)[35,43] G(need)[3,7]",
		        "Dependency(nn)[27,34] D(example)[27,34] G(sentence)[35,43]",
		        "Dependency(nsubj)[0,2] D(We)[0,2] G(need)[3,7]",
		        "Dependency(nsubj)[46,51] D(which)[46,51] G(contains)[52,60]",
		        "Dependency(pobj)[102,110] D(possible)[102,110] G(as)[99,101]",
		        "Dependency(pobj)[69,81] D(constituents)[69,81] G(as)[61,63]",
		        "Dependency(prep)[61,63] D(as)[61,63] G(contains)[52,60]",
		        "Dependency(prep)[99,101] D(as)[99,101] G(constituents)[69,81]",
		        "Dependency(punct)[111,112] D(.)[111,112] G(need)[3,7]",
		        "Dependency(punct)[44,45] D(,)[44,45] G(sentence)[35,43]",
		        "Dependency(rcmod)[52,60] D(contains)[52,60] G(sentence)[35,43]" };

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

		String[] dependencies = new String[] { 
		        "Dependency(-PUNCT-)[112,113] D(.)[112,113] G(Dependenzen)[89,100]",
		        "Dependency(DET)[13,16] D(ein)[13,16] G(brauchen)[4,12]",
		        "Dependency(DET)[17,21] D(sehr)[17,21] G(ein)[13,16]",
		        "Dependency(DET)[22,35] D(kompliziertes)[22,35] G(Beispiel)[36,44]",
		        "Dependency(DET)[47,54] D(welches)[47,54] G(,)[45,46]",
		        "Dependency(DET)[55,64] D(möglichst)[55,64] G(welches)[47,54]",
		        "Dependency(DET)[65,70] D(viele)[65,70] G(möglichst)[55,64]",
		        "Dependency(DET)[71,84] D(Konstituenten)[71,84] G(viele)[65,70]",
		        "Dependency(DET)[85,88] D(und)[85,88] G(Konstituenten)[71,84]",
		        "Dependency(DET)[89,100] D(Dependenzen)[89,100] G(und)[85,88]",
		        "Dependency(GMOD)[36,44] D(Beispiel)[36,44] G(sehr)[17,21]",
		        "Dependency(KON)[45,46] D(,)[45,46] G(brauchen)[4,12]",
		        "Dependency(PN)[101,111] D(beinhaltet)[101,111] G(.)[112,113]",
		        "Dependency(ROOT)[4,12] D(brauchen)[4,12] G(Wir)[0,3]" };

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
		AnalysisEngineDescription engine = createEngineDescription(
				createEngineDescription(OpenNlpPosTagger.class),
				createEngineDescription(MaltParser.class,
						MaltParser.PARAM_VARIANT, aVariant,
						MaltParser.PARAM_PRINT_TAGSET, true),
				createEngineDescription(DependencyDumper.class));

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
