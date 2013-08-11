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

		String[] dependencies = new String[] { 
		        "Dependency(advmod)[10,14] D(very)[10,14] G(complicated)[15,26]",
		        "Dependency(amod)[102,110] D(possible)[102,110] G(as)[99,101]",
		        "Dependency(amod)[15,26] D(complicated)[15,26] G(sentence)[35,43]",
		        "Dependency(amod)[64,68] D(many)[64,68] G(constituents)[69,81]",
		        "Dependency(cc)[82,85] D(and)[82,85] G(constituents)[69,81]",
		        "Dependency(conj)[86,98] D(dependencies)[86,98] G(constituents)[69,81]",
		        "Dependency(det)[8,9] D(a)[8,9] G(sentence)[35,43]",
		        "Dependency(dobj)[35,43] D(sentence)[35,43] G(need)[3,7]",
		        "Dependency(nn)[27,34] D(example)[27,34] G(sentence)[35,43]",
		        "Dependency(nsubj)[0,2] D(We)[0,2] G(need)[3,7]",
		        "Dependency(nsubj)[46,51] D(which)[46,51] G(contains)[52,60]",
		        "Dependency(pobj)[69,81] D(constituents)[69,81] G(as)[61,63]",
		        "Dependency(prep)[61,63] D(as)[61,63] G(contains)[52,60]",
		        "Dependency(prep)[99,101] D(as)[99,101] G(constituents)[69,81]",
		        "Dependency(punct)[111,112] D(.)[111,112] G(need)[3,7]",
		        "Dependency(punct)[44,45] D(,)[44,45] G(sentence)[35,43]",
		        "Dependency(rcmod)[52,60] D(contains)[52,60] G(sentence)[35,43]" };

		AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
	}

	@Test
	public void testEnglishMayo()
		throws Exception
	{
//		Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1200000000l);

		JCas jcas = runTest("en", "mayo", documentEnglish);

		String[] dependencies = new String[] { 
		        "Dependency(advcl)[102,110] D(possible)[102,110] G(contains)[52,60]",
		        "Dependency(advmod)[10,14] D(very)[10,14] G(complicated)[15,26]",
		        "Dependency(amod)[15,26] D(complicated)[15,26] G(sentence)[35,43]",
		        "Dependency(amod)[64,68] D(many)[64,68] G(constituents)[69,81]",
		        "Dependency(cc)[82,85] D(and)[82,85] G(constituents)[69,81]",
		        "Dependency(conj)[86,98] D(dependencies)[86,98] G(constituents)[69,81]",
		        "Dependency(det)[8,9] D(a)[8,9] G(sentence)[35,43]",
		        "Dependency(dobj)[35,43] D(sentence)[35,43] G(need)[3,7]",
		        "Dependency(mark)[99,101] D(as)[99,101] G(possible)[102,110]",
		        "Dependency(nn)[27,34] D(example)[27,34] G(sentence)[35,43]",
		        "Dependency(nsubj)[0,2] D(We)[0,2] G(need)[3,7]",
		        "Dependency(nsubj)[46,51] D(which)[46,51] G(contains)[52,60]",
		        "Dependency(pobj)[69,81] D(constituents)[69,81] G(as)[61,63]",
		        "Dependency(prep)[61,63] D(as)[61,63] G(contains)[52,60]",
		        "Dependency(punct)[111,112] D(.)[111,112] G(need)[3,7]",
		        "Dependency(punct)[44,45] D(,)[44,45] G(sentence)[35,43]",
		        "Dependency(rcmod)[52,60] D(contains)[52,60] G(sentence)[35,43]" };

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
