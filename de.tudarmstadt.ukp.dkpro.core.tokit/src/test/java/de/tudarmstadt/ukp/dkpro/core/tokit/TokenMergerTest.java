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
package de.tudarmstadt.ukp.dkpro.core.tokit;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.util.JCasUtil.select;
import static org.uimafit.util.JCasUtil.toText;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Test;
import org.uimafit.testing.factory.TokenBuilder;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TokenMergerTest
{
	@Test
	public void testSimpleMerge()
		throws Exception
	{
		AnalysisEngine filter = createPrimitive(TokenMerger.class,
				TokenMerger.PARAM_ANNOTATION_TYPE, Sentence.class);

		String content = "I love New York .";
		JCas jcas = filter.newJCas();

		TokenBuilder<Token, Annotation> tb = new TokenBuilder<Token, Annotation>(Token.class,
				Annotation.class);
		tb.buildTokens(jcas, content);

		new Sentence(jcas, 7, 16).addToIndexes();

		filter.process(jcas);

		assertEquals(asList("I", "love", "New York", "."), toText(select(jcas, Token.class)));
	}
}
