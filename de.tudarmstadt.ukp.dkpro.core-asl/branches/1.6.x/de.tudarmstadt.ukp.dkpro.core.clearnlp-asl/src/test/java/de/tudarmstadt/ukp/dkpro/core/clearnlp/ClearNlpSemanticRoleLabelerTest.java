/*******************************************************************************
 * Copyright 2013
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

import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticPredicate;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class ClearNlpSemanticRoleLabelerTest
{
	static final String documentEnglish = "We need a very complicated example sentence , which "
			+ "contains as many constituents and dependencies as possible .";

	@Test
	public void testEnglish()
		throws Exception
	{
		Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 3000000000l);

		JCas jcas = runTest("en", null, documentEnglish);

		String[] predicates = new String[] {
				"contains (contain.01): [(A0:sentence)(R-A0:which)(A1:as)]",
				"need (need.01): [(A0:We)(A1:sentence)]" };

		AssertAnnotations.assertSemanticPredicates(predicates,
				select(jcas, SemanticPredicate.class));
	}

	@Test
	public void testEnglishMayo()
		throws Exception
	{
		Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 3000000000l);

		JCas jcas = runTest("en", "mayo", documentEnglish);

		String[] predicates = new String[] {
				"contains (contain.01): [(A0:sentence)(R-A0:which)(A1:as)]",
				"need (need.01): [(A0:We)(A1:sentence)]" };

		AssertAnnotations.assertSemanticPredicates(predicates,
				select(jcas, SemanticPredicate.class));
	}

	private JCas runTest(String aLanguage, String aVariant, String aText)
		throws Exception
	{
		AnalysisEngineDescription engine = createEngineDescription(
				createEngineDescription(OpenNlpPosTagger.class),
				createEngineDescription(ClearNlpLemmatizer.class),
				createEngineDescription(ClearNlpDependencyParser.class),
				createEngineDescription(ClearNlpSemanticRoleLabeler.class,
						ClearNlpDependencyParser.PARAM_VARIANT, aVariant,
						ClearNlpDependencyParser.PARAM_PRINT_TAGSET, true));

		return TestRunner.runTest(engine, aLanguage, aText);
	}

	@Rule
	public TestName testName = new TestName();

	@Before
	public void printSeparator()
	{
		Runtime.getRuntime().gc();
		Runtime.getRuntime().gc();
		Runtime.getRuntime().gc();

		System.out.println("\n=== " + testName.getMethodName() + " =====================");
	}
}
