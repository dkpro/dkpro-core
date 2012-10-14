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
package de.tudarmstadt.ukp.dkpro.core.posfilter;

import static java.util.Arrays.asList;
import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.util.JCasUtil.select;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class PosFilterTest
{
	@Test
	public void testEnglish1()
		throws Exception
	{
		String testDocument = "This is a not so long test sentence . This is a longer second " +
				"test sentence . More sentences are necessary for the tests .";

		String[] tokens = new String[] { "long", "test", "sentence", "second", "test", "sentence",
				"More", "sentences", "necessary", "tests" };

		runTest("en", testDocument, tokens, PosFilter.PARAM_ADJ, true, PosFilter.PARAM_N, true);
	}

	@Test
	public void testEnglish2()
		throws Exception
	{
		String testDocument = "This is a not so long test sentence . This is a longer second " +
				"test sentence . More sentences are necessary for the tests .";

		String[] tokens = new String[] { "is", "long", "test", "sentence", "is", "second", "test",
				"sentence", "More", "sentences", "are", "necessary", "tests" };

		runTest("en", testDocument, tokens, PosFilter.PARAM_ADJ, true, PosFilter.PARAM_N, true,
				PosFilter.PARAM_V, true);
	}
	
	private void runTest(String language, String testDocument,
			String[] aTokens, Object... aExtraParams)
		throws Exception
	{
		List<Object> posFilterParams = new ArrayList<Object>();
		posFilterParams.addAll(asList(PosFilter.PARAM__TYPE_TO_REMOVE, Token.class.getName()));
		posFilterParams.addAll(asList(aExtraParams));
		
		AnalysisEngineDescription aggregate = createAggregateDescription(
				createPrimitiveDescription(OpenNlpPosTagger.class),
				createPrimitiveDescription(PosFilter.class,
						posFilterParams.toArray(new Object[posFilterParams.size()])));

		JCas jcas = TestRunner.runTest(aggregate, language, testDocument);
		
		AssertAnnotations.assertToken(aTokens, select(jcas, Token.class));
	}

	@Rule
	public TestName name = new TestName();

	@Before
	public void printSeparator()
	{
		System.out.println("\n=== " + name.getMethodName() + " =====================");
	}

}
