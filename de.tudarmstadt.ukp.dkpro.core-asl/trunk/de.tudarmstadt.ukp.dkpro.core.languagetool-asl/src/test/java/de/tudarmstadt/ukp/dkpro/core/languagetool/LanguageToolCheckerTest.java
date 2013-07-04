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
package de.tudarmstadt.ukp.dkpro.core.languagetool;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitive;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertEquals;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.testing.factory.TokenBuilder;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.GrammarAnomaly;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class LanguageToolCheckerTest
{
	@Test
	public void grammarCheckerTest()
		throws Exception
	{
		String testDocument = "A sentence with a error in the Hitchhiker's Guide tot he Galaxy .";

		AnalysisEngine engine = createPrimitive(LanguageToolChecker.class,
				LanguageToolChecker.PARAM_LANGUAGE, "en");
		JCas aJCas = engine.newJCas();

		TokenBuilder<Token, Sentence> tb = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class);
		tb.buildTokens(aJCas, testDocument);

		engine.process(aJCas);

		// copy input match type annotations to an array
		int count = 0;
		for (GrammarAnomaly ga : select(aJCas, GrammarAnomaly.class)) {
			System.out.println("Error " + (count + 1) + " (" + ga.getBegin() + ", " + ga.getEnd()
					+ "):" + ga.getDescription());
			count++;
		}
		assertEquals(count, 3);
	}
}
