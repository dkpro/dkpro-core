package de.tudarmstadt.ukp.dkpro.core.arktools;


/**
 * Copyright 2007-2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.arktools.ArktweetTokenizer;

public class ArktweetTokenizationTest {

	@Test
	public void testDummySentenceBoundary()
			throws AnalysisEngineProcessException,
			ResourceInitializationException {
		String text = " Content.&quot;made a pac lets see how long it last&quot;";
		JCas tokenize = tokenize(text);
		assertEquals(1, JCasUtil.select(tokenize, Sentence.class).size());
	}

	@Test
	public void testTokenization1() throws ResourceInitializationException,
			AnalysisEngineProcessException {
		String text = " Content.&quot;made a pac lets see how long it last&quot;";
		List<Token> tokens = getTokens(text);

		assertNumberOfTokens(13, tokens.size());
		assertTokenizationBoundaries(new String[] { "Content", ".", "&quot;",
				"made", "a", "pac", "lets", "see", "how", "long", "it", "last",
				"&quot;" }, tokens);
	}

	private void assertTokenizationBoundaries(String[] expected,
			List<Token> tokens) {
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], tokens.get(i).getCoveredText());
		}
	}

	private void assertNumberOfTokens(int expected, int numberOfTokens) {
		assertEquals(expected, numberOfTokens);
	}

	@Test
	public void testTokenization2() throws ResourceInitializationException,
			AnalysisEngineProcessException {
		String text = "   Tiger Woods is up by 2at 18 via http://nascar.com/racebuddy";

		List<Token> tokens = getTokens(text);

		assertNumberOfTokens(9, tokens.size());
		assertTokenizationBoundaries(
				new String[] { "Tiger", "Woods", "is", "up", "by", "2at", "18",
						"via", "http://nascar.com/racebuddy" }, tokens);
	}

	@Test
	public void testTokenization3() throws ResourceInitializationException,
			AnalysisEngineProcessException {
		String text = "    My cell phone screen is dead.  Sooooooooooo, no texts and I don't know who's calling.  Fuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuck";

		List<Token> tokens = getTokens(text);

		assertNumberOfTokens(19, tokens.size());
		assertTokenizationBoundaries(
				new String[] { "My", "cell", "phone", "screen", "is", "dead",
						".", "Sooooooooooo", ",", "no", "texts", "and", "I",
						"don't", "know", "who's", "calling", "." }, tokens);
	}

	@Test
	public void testTokenization4() throws ResourceInitializationException,
			AnalysisEngineProcessException {
		String text = " &quot;Im in love and I don't care who knows it !&quot; -elf";

		List<Token> tokens = getTokens(text);

		assertNumberOfTokens(14, tokens.size());
		assertTokenizationBoundaries(new String[] { "&quot;", "Im", "in",
				"love", "and", "I", "don't", "care", "who", "knows", "it", "!",
				"&quot;", "-elf" }, tokens);
	}

	@Test
	public void testTokenization5() throws ResourceInitializationException,
			AnalysisEngineProcessException {
		String text = " I love him, and now, we're not even friends&lt;\\3";

		List<Token> tokens = getTokens(text);

		assertNumberOfTokens(13, tokens.size());
		assertTokenizationBoundaries(new String[] { "I", "love", "him", ",",
				"and", "now", ",", "we're", "not", "even", "friends", "&lt;",
				"\\3" }, tokens);
	}

	@Test
	public void testTokenization6() throws ResourceInitializationException,
			AnalysisEngineProcessException {
		String text = "@TextTonic &quot;control&quot; or &quot;abuse&quot;? I see them as Very different. Whilst we are into self promoting here goes   http://tinyurl.com/cru3hu";
		List<Token> tokens = getTokens(text);

		assertNumberOfTokens(25, tokens.size());
		assertTokenizationBoundaries(new String[] { "@TextTonic", "&quot;",
				"control", "&quot;", "or", "&quot;", "abuse", "&quot;", "?", "I", "see",
				"them", "as", "Very", "different", ".", "Whilst", "we", "are",
				"into", "self", "promoting", "here", "goes",
				"http://tinyurl.com/cru3hu" }, tokens);
	}
	
	private List<Token> getTokens(String text)
			throws AnalysisEngineProcessException,
			ResourceInitializationException {
		JCas jcas = tokenize(text);
		List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class, 0, jcas
				.getDocumentText().length());
		return tokens;
	}

	private JCas tokenize(String text) throws ResourceInitializationException,
			AnalysisEngineProcessException {
		AnalysisEngineDescription segmenter = createEngineDescription(ArktweetTokenizer.class);
		AnalysisEngine segEngine = UIMAFramework
				.produceAnalysisEngine(segmenter);

		JCas testCas = segEngine.newJCas();
		testCas.setDocumentLanguage("en");
		testCas.setDocumentText(text);
		segEngine.process(testCas);
		return testCas;
	}
}
