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

import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.util.JCasUtil.select;
import static org.uimafit.util.JCasUtil.toText;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Test;
import org.uimafit.testing.factory.TokenBuilder;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TokenFilterTest
{

    @Test
	public void testProcess()
		throws Exception
	{
		AnalysisEngine filter = createPrimitive(
		        TokenFilter.class,
				TokenFilter.PARAM_MAX_TOKEN_LENGTH, 5);

		String content  = "1 22 333 4444 55555 666666 7777777 88888888 999999999";
		JCas jcas = filter.newJCas();
		
        TokenBuilder<Token, Annotation> tb = new TokenBuilder<Token, Annotation>(Token.class, Annotation.class);
        tb.buildTokens(jcas, content);
		filter.process(jcas);

		assertEquals("1 22 333 4444 55555", StringUtils.join(toText(select(jcas, Token.class)), " "));
	}
}
