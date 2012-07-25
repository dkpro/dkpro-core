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

import static org.uimafit.util.JCasUtil.select;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Split up existing tokens again at particular split-chars.
 * The prefix states whether the split chars should be added as separate {@link Token Tokens}.
 * If the {@link INCLUDE_PREFIX} precedes the split pattern, the pattern is included.
 * Consequently, patterns following the {@link EXCLUDE_PREFIX}, will not be added as a Token.
 *
 * @author Richard Eckart de Castilho
 */
public class PatternBasedTokenSegmenter
	extends JCasAnnotator_ImplBase
{
	public static final String INCLUDE_PREFIX = "+|";
	public static final String EXCLUDE_PREFIX = "-|";

	public static final String PARAM_DELETE_COVER = "DeleteCover";
	@ConfigurationParameter(name=PARAM_DELETE_COVER, defaultValue="true", mandatory=true)
	private boolean deleteCover;

	public static final String PARAM_PATTERNS = "Patterns";
	@ConfigurationParameter(name=PARAM_PATTERNS, mandatory=true)
	private String[] rawPatterns;

	private StringBuilder buf;

	private SplitPattern[] patterns;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		patterns = new SplitPattern[rawPatterns.length];
		for (int i = 0; i < rawPatterns.length; i++) {
			if (rawPatterns[i].startsWith(INCLUDE_PREFIX)) {
				patterns[i] = new SplitPattern(rawPatterns[i].substring(INCLUDE_PREFIX.length()), true);
			}
			else if (rawPatterns[i].startsWith(EXCLUDE_PREFIX)) {
				patterns[i] = new SplitPattern(rawPatterns[i].substring(EXCLUDE_PREFIX.length()), false);
			}
			else {
				patterns[i] = new SplitPattern(rawPatterns[i], false);
			}
		}
	}

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		buf = new StringBuilder();
		List<Token> toAdd = new ArrayList<Token>();
		List<Token> toRemove = new ArrayList<Token>();

		for (Token t : select(aJCas, Token.class)) {
			String text = t.getCoveredText();
			int offset = t.getBegin();
			int start = 0;
			SplitPattern lastPattern = getPattern(text.charAt(0), null);
			Token firstToken = null;
			for (int i = 1; i < text.length(); i++) {
				SplitPattern pattern = getPattern(text.charAt(i), lastPattern);
				if (pattern != lastPattern) {
					if (lastPattern == null || lastPattern.includeInOutput) {
						Token nt = addToken(aJCas, offset, text, start, i, toAdd);
						firstToken = (firstToken == null) ? nt : firstToken;
					}
					start = i;
				}
				lastPattern = pattern;
			}

			// If we would just create the same token again, better do nothing
			if (start == 0) {
				// That is - if the whole token matches something to exclude, we remove it
				if (lastPattern != null && !lastPattern.includeInOutput) {
					toRemove.add(t);
				}
				continue;
			}

			if (deleteCover) {
				toRemove.add(t);
			}

			// The rest goes into the final token
			if (lastPattern == null || lastPattern.includeInOutput) {
				addToken(aJCas, offset, text, start, text.length(), toAdd);
			}
		}

		for (Token t : toAdd) {
			t.addToIndexes();
		}

		for (Token t : toRemove) {
			t.removeFromIndexes();
		}
	}

	private Token addToken(JCas aJCas, int offset, String text, int start, int end, List<Token> toAdd)
	{
		// No adding empty tokens
		if (end == start) {
			return null;
		}

		Token t = new Token(aJCas, offset+start, offset+end);
		toAdd.add(t);
		return t;
	}

	SplitPattern getPattern(char ch, SplitPattern aLastPattern)
	{
		buf.append(ch);
		for (SplitPattern p : patterns) {
			p.matchter.reset(buf);
			if (p.matchter.matches()) {
				if (p != aLastPattern) {
					buf.setLength(0);
				}
				return p;
			}
		}
		buf.setLength(0);
		return null;
	}

	private static class SplitPattern
	{
		final boolean includeInOutput;
		final Matcher matchter;

		public SplitPattern(String aPattern, boolean aInclude)
		{
			includeInOutput = aInclude;
			matchter = Pattern.compile(aPattern).matcher("");
		}
	}
}
