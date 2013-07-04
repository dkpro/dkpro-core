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
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitive;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.toText;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class PatternBasedTokenSegmenterTest
{
	private static final String[] PATTERNS = new String[] {
			PatternBasedTokenSegmenter.INCLUDE_PREFIX + "[0-9]+",
			PatternBasedTokenSegmenter.EXCLUDE_PREFIX + "[\\/?!&%\"'#<>_=.:;]+" };

	@Test
	public void testProcess()
		throws Exception
	{
		AnalysisEngine seg = createPrimitive(PatternBasedTokenSegmenter.class,
				PatternBasedTokenSegmenter.PARAM_PATTERNS, PATTERNS);

		// 01234567890123456789012345
		String content = "This_Is_A_Camel Case_Text";
		JCas cas = seg.newJCas();
		cas.setDocumentText(content);
		new Token(cas, 0, 15).addToIndexes();
		new Token(cas, 16, 25).addToIndexes();

		seg.process(cas);

		List<String> ref = asList("This", "Is", "A", "Camel", "Case", "Text");
		List<String> tokens = toText(select(cas, Token.class));
		System.out.println(tokens);
		assertEquals(ref, tokens);
	}

	@Test
	public void testProcess2()
		throws Exception
	{
		AnalysisEngine seg = createPrimitive(PatternBasedTokenSegmenter.class,
				PatternBasedTokenSegmenter.PARAM_PATTERNS, PATTERNS);

		// 0123456789012345678901234567
		String content = "This_Is.A_Camel_ _Case_Text";
		JCas cas = seg.newJCas();
		cas.setDocumentText(content);
		new Token(cas, 0, 16).addToIndexes();
		new Token(cas, 17, 27).addToIndexes();

		seg.process(cas);

		List<String> ref = asList("This", "Is", "A", "Camel", "Case", "Text");
		List<String> tokens = toText(select(cas, Token.class));
		System.out.println(tokens);
		assertEquals(ref, tokens);
	}

	@Test
	public void testProcess3()
		throws Exception
	{
		AnalysisEngine seg = createPrimitive(PatternBasedTokenSegmenter.class,
				PatternBasedTokenSegmenter.PARAM_PATTERNS, PATTERNS);

		// 012345
		String content = "This_";
		JCas cas = seg.newJCas();
		cas.setDocumentText(content);
		new Token(cas, 0, 5).addToIndexes();

		seg.process(cas);

		List<String> ref = asList("This");
		List<String> tokens = toText(select(cas, Token.class));
		System.out.println(tokens);
		assertEquals(ref, tokens);
	}

	@Test
	public void testProcess4()
		throws Exception
	{
		AnalysisEngine seg = createPrimitive(PatternBasedTokenSegmenter.class,
				PatternBasedTokenSegmenter.PARAM_PATTERNS, PATTERNS);

		// 0123456789012345
		String content = "rent25to29point9";
		JCas cas = seg.newJCas();
		cas.setDocumentText(content);
		new Token(cas, 0, 16).addToIndexes();

		seg.process(cas);

		List<String> ref = asList("rent", "25", "to", "29", "point", "9");
		List<String> tokens = toText(select(cas, Token.class));
		System.out.println(tokens);
		assertEquals(ref, tokens);
	}

	@Test
	public void testProcess5()
		throws Exception
	{
		AnalysisEngine seg = createPrimitive(PatternBasedTokenSegmenter.class,
				PatternBasedTokenSegmenter.PARAM_PATTERNS, PATTERNS);

		// 012345
		String content = "_This";
		JCas cas = seg.newJCas();
		cas.setDocumentText(content);
		new Token(cas, 0, 5).addToIndexes();

		seg.process(cas);

		List<String> ref = asList("This");
		List<String> tokens = toText(select(cas, Token.class));
		System.out.println(tokens);
		assertEquals(ref, tokens);
	}

	@Test
	public void testProcess6()
		throws Exception
	{
		AnalysisEngine seg = createPrimitive(PatternBasedTokenSegmenter.class,
				PatternBasedTokenSegmenter.PARAM_PATTERNS, PATTERNS);

		// 012345
		String content = "_This";
		JCas cas = seg.newJCas();
		cas.setDocumentText(content);
		new Token(cas, 0, 1).addToIndexes();
		new Token(cas, 1, 5).addToIndexes();

		seg.process(cas);

		List<String> ref = asList("This");
		List<String> tokens = toText(select(cas, Token.class));
		System.out.println(tokens);
		assertEquals(ref, tokens);
	}
}
