/*
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
 */
package de.tudarmstadt.ukp.dkpro.core.cogroo;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class CogrooFeaturizerTest
{
	@Test
	public void testPortuguese()
		throws Exception
	{
        runTest("pt", "Este é um teste . ", new String[] {
                "[  0,  4]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - Este (M=S)",
                "[  5,  6]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - é (PR=3S=IND)",
                "[  7,  9]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - um (M=S)",
                "[ 10, 15]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - teste (M=S)",
                "[ 16, 17]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - . (-)" });

        runTest("pt", "Uma rede neural .", new String[] { 
                "[  0,  3]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - Uma (F=S)",
                "[  4,  8]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - rede (F=S)",
                "[  9, 15]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - neural (F=S)",
                "[ 16, 17]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - . (-)" });

        runTest("pt", "John está comprando laranjas .", new String[] { 
                "[  0,  4]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - John (M=S)",
                "[  5,  9]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - está (PR=3S=IND)",
                "[ 10, 19]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - comprando (-)",
                "[ 20, 28]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - laranjas (F=P)",
                "[ 29, 30]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - . (-)" });
    }

	private void runTest(String language, String testDocument, String[] aFeatures)
		throws Exception
	{
		AnalysisEngineDescription engine = createEngineDescription(
		        createEngineDescription(CogrooPosTagger.class),
		        createEngineDescription(CogrooFeaturizer.class));

		JCas jcas = TestRunner.runTest(engine, language, testDocument);

		AssertAnnotations.assertMorph(aFeatures, select(jcas, MorphologicalFeatures.class));
	}

	@Rule
	public TestName name = new TestName();

	@Before
	public void printSeparator()
	{
		System.out.println("\n=== " + name.getMethodName() + " =====================");
	}
}
