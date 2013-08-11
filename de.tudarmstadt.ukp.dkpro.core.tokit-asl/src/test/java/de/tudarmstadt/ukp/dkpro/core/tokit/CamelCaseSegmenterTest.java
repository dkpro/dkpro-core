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
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.toText;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class CamelCaseSegmenterTest
{
	@Test
	public void testProcess() throws Exception
	{
		AnalysisEngine seg = createEngine(CamelCaseTokenSegmenter.class);

		//                0123456789012345678901
		String content = "ThisIsACamel CaseText";
		JCas cas = seg.newJCas();
		cas.setDocumentText(content);
		new Token(cas, 0, 12).addToIndexes();
		new Token(cas, 13, 21).addToIndexes();

		seg.process(cas);

		List<String> ref = asList("This", "Is", "A", "Camel", "Case", "Text");
		List<String> tokens = toText(select(cas, Token.class));
		System.out.println(tokens);
		assertEquals(ref, tokens);
	}

	@Test
	public void testProcess2() throws Exception
	{
		AnalysisEngine seg = createEngine(CamelCaseTokenSegmenter.class);

		//                01234567890123456789012
		String content = "GetFileUploadURLRequest";
		JCas cas = seg.newJCas();
		cas.setDocumentText(content);
		new Token(cas, 0, 23).addToIndexes();

		seg.process(cas);

		List<String> ref = asList("Get", "File", "Upload", "URL", "Request");
		List<String> tokens = toText(select(cas, Token.class));
		System.out.println(tokens);
		assertEquals(ref, tokens);
	}

	@Test
	public void testProcess3() throws Exception
	{
		AnalysisEngine seg = createEngine(CamelCaseTokenSegmenter.class);

		//                01234567890123
		String content = "_ORGANIZATION";
		JCas cas = seg.newJCas();
		cas.setDocumentText(content);
		new Token(cas, 0, 1).addToIndexes();
		new Token(cas, 1, 13).addToIndexes();

		seg.process(cas);

		List<String> ref = asList("_", "ORGANIZATION");
		List<String> tokens = toText(select(cas, Token.class));
		System.out.println(tokens);
		assertEquals(ref, tokens);
	}
}
