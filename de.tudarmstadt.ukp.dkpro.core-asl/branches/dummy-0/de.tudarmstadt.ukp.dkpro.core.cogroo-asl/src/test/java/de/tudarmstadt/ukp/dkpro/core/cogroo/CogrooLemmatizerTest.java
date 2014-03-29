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
package de.tudarmstadt.ukp.dkpro.core.cogroo;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class CogrooLemmatizerTest
{
	@Test
	public void testPortuguese()
		throws Exception
	{
        runTest("pt", "Este é um teste . ",
                 new String[] { "este", "ser", "um", "teste", "." });

        runTest("pt", "Uma rede neural .",
        		new String[] { "um", "rede", "neural", "." });

        runTest("pt", "John está comprando laranjas .",
        		new String[] { "John", "estar", "comprar", "laranja", "." });
    }

	private void runTest(String language, String testDocument, String[] aLemma)
		throws Exception
	{
		AnalysisEngineDescription engine = createEngineDescription(
		        createEngineDescription(CogrooPosTagger.class),
		        createEngineDescription(CogrooLemmatizer.class));

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
