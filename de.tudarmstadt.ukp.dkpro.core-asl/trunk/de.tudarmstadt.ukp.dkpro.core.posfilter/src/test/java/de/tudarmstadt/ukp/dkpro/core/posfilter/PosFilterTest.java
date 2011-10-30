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

import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.util.JCasUtil.*;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.core.treetagger.TreeTaggerPosLemmaTT4J;

public class PosFilterTest
{

	@Test
	public void FilterTest()
		throws Exception
	{
		checkModelsAndBinary("en");

		String testDocument = "This is a not so long test sentence. This is a longer second test " +
				"sentence. More sentences are necessary for the tests.";

		AnalysisEngineDescription aggregate = createAggregateDescription(
				createPrimitiveDescription(BreakIteratorSegmenter.class),
				createPrimitiveDescription(TreeTaggerPosLemmaTT4J.class),
				createPrimitiveDescription(PosFilter.class,
						PosFilter.PARAM__TYPE_TO_REMOVE, Token.class.getName(),
						PosFilter.PARAM_ADJ, true,
						PosFilter.PARAM_N, true));

		AnalysisEngine engine = AnalysisEngineFactory.createAggregate(aggregate);
		JCas aJCas = engine.newJCas();
		aJCas.setDocumentLanguage("en");
		aJCas.setDocumentText(testDocument);

		engine.process(aJCas);

		int i = 0;
		for (Token token : select(aJCas, Token.class)) {
			System.out.println(token.getCoveredText());
			i++;
		}
		assertEquals(10, i);
	}

	@Test
	public void FilterTest2()
		throws Exception
	{
		checkModelsAndBinary("en");

		String testDocument = "This is a not so long test sentence. This is a longer second test " +
				"sentence. More sentences are necessary for the tests.";

		AnalysisEngineDescription aggregate = createAggregateDescription(
				createPrimitiveDescription(BreakIteratorSegmenter.class),
				createPrimitiveDescription(TreeTaggerPosLemmaTT4J.class),
				createPrimitiveDescription(PosFilter.class,
						PosFilter.PARAM__TYPE_TO_REMOVE, Token.class.getName(),
						PosFilter.PARAM_ADJ, true,
						PosFilter.PARAM_N, true,
						PosFilter.PARAM_V, true));

		AnalysisEngine engine = AnalysisEngineFactory.createAggregate(aggregate);
		JCas aJCas = engine.newJCas();
		aJCas.setDocumentText(testDocument);
		aJCas.setDocumentLanguage("en");

		engine.process(aJCas);

		for (POS p : select(aJCas, POS.class)) {
			System.out.println(p);
		}

		int i = 0;
		for (Token token : select(aJCas, Token.class)) {
			System.out.println(token.getCoveredText());
			i++;
		}
		assertEquals(13, i);
	}

	private void checkModelsAndBinary(String lang)
	{
		Assume.assumeTrue(getClass().getResource(
				"/de/tudarmstadt/ukp/dkpro/core/treetagger/lib/" + lang
						+ "-tagger-little-endian.par") != null);

		Assume.assumeTrue(getClass().getResource(
				"/de/tudarmstadt/ukp/dkpro/core/treetagger/bin/LICENSE.txt") != null);
	}

}
