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
		        "[  0,  2]Dependency(nsubj) D[0,2](We) G[3,7](need)",
		        "[  8,  9]Dependency(det) D[8,9](a) G[35,43](sentence)",
		        "[ 10, 14]Dependency(advmod) D[10,14](very) G[15,26](complicated)",
		        "[ 15, 26]Dependency(amod) D[15,26](complicated) G[35,43](sentence)",
		        "[ 27, 34]Dependency(nn) D[27,34](example) G[35,43](sentence)",
		        "[ 35, 43]Dependency(dobj) D[35,43](sentence) G[3,7](need)",
		        "[ 44, 45]Dependency(punct) D[44,45](,) G[35,43](sentence)",
		        "[ 46, 51]Dependency(nsubj) D[46,51](which) G[52,60](contains)",
		        "[ 52, 60]Dependency(rcmod) D[52,60](contains) G[35,43](sentence)",
		        "[ 61, 63]Dependency(prep) D[61,63](as) G[52,60](contains)",
		        "[ 64, 68]Dependency(amod) D[64,68](many) G[69,81](constituents)",
		        "[ 69, 81]Dependency(pobj) D[69,81](constituents) G[61,63](as)",
		        "[ 82, 85]Dependency(cc) D[82,85](and) G[69,81](constituents)",
		        "[ 86, 98]Dependency(conj) D[86,98](dependencies) G[69,81](constituents)",
		        "[ 99,101]Dependency(prep) D[99,101](as) G[86,98](dependencies)",
		        "[102,110]Dependency(amod) D[102,110](possible) G[99,101](as)",
		        "[111,112]Dependency(punct) D[111,112](.) G[3,7](need)" };

		AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
	}

	@Test
	public void testEnglishMayo()
		throws Exception
	{
		Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1200000000l);

		JCas jcas = runTest("en", "mayo", documentEnglish);

		String[] dependencies = new String[] { 
		        "[  0,  2]Dependency(nsubj) D[0,2](We) G[3,7](need)",
		        "[  8,  9]Dependency(det) D[8,9](a) G[35,43](sentence)",
		        "[ 10, 14]Dependency(advmod) D[10,14](very) G[15,26](complicated)",
		        "[ 15, 26]Dependency(amod) D[15,26](complicated) G[35,43](sentence)",
		        "[ 27, 34]Dependency(nn) D[27,34](example) G[35,43](sentence)",
		        "[ 35, 43]Dependency(dobj) D[35,43](sentence) G[3,7](need)",
		        "[ 44, 45]Dependency(punct) D[44,45](,) G[35,43](sentence)",
		        "[ 46, 51]Dependency(nsubj) D[46,51](which) G[52,60](contains)",
		        "[ 52, 60]Dependency(rcmod) D[52,60](contains) G[35,43](sentence)",
		        "[ 61, 63]Dependency(prep) D[61,63](as) G[52,60](contains)",
		        "[ 64, 68]Dependency(amod) D[64,68](many) G[69,81](constituents)",
		        "[ 69, 81]Dependency(pobj) D[69,81](constituents) G[61,63](as)",
		        "[ 82, 85]Dependency(cc) D[82,85](and) G[69,81](constituents)",
		        "[ 86, 98]Dependency(conj) D[86,98](dependencies) G[69,81](constituents)",
		        "[ 99,101]Dependency(mark) D[99,101](as) G[102,110](possible)",
		        "[102,110]Dependency(advcl) D[102,110](possible) G[52,60](contains)",
		        "[111,112]Dependency(punct) D[111,112](.) G[3,7](need)" };

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
