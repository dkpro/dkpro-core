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


import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.CasAnnotator_ImplBase;

import cmu.arktweetnlp.Twokenize;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class ArktweetTokenizer extends CasAnnotator_ImplBase {

	private Type tokenType;
	private Type sentenceType;

	@Override
	public void typeSystemInit(TypeSystem aTypeSystem)
			throws AnalysisEngineProcessException {
		super.typeSystemInit(aTypeSystem);

		tokenType = aTypeSystem.getType(Token.class.getName());
		sentenceType = aTypeSystem.getType(Sentence.class.getName());
	}

	@Override
	public void process(CAS cas) throws AnalysisEngineProcessException {
		String text = cas.getDocumentText();
		String normalizedText = normalizeText(text);

		// FIXME: Implement proper sentence boundary detection
		AnnotationFS sentenceAnno = cas.createAnnotation(sentenceType, 0,
				normalizedText.length());
		cas.addFsToIndexes(sentenceAnno);

		List<String> tokens = Twokenize.tokenizeRawTweetText(normalizedText);

		int start = 0;
		int end = 0;
		int searchOffset = 0;
		int normalizedTextOffsetAdjustment = 0;
		for (String token : tokens) {

			int tokenOffset = text.indexOf(token, searchOffset);
			int normalizedOffset = normalizedText.indexOf(token, searchOffset
					- normalizedTextOffsetAdjustment);

			if (tokenOffset == -1) {
				int ampersandOffset = text.indexOf("&", searchOffset);
				int semicolonOffset = text.indexOf(";", searchOffset);

				start = normalizedOffset + normalizedTextOffsetAdjustment;
				end = normalizedOffset + token.length()
						+ (semicolonOffset - ampersandOffset)
						+ normalizedTextOffsetAdjustment;

				// if a substitution occurred, the normalized text will be
				// shorter than the raw text. Adjust position counter by length
				// difference
				normalizedTextOffsetAdjustment += (end - start) - 1;
			} else {
				start = tokenOffset;
				end = tokenOffset + token.length();
			}
			createTokenAnnotation(cas, start, end);

			searchOffset = end;
		}

	}

	private void createTokenAnnotation(CAS cas, int start, int end) {
		AnnotationFS tokenAnno = cas.createAnnotation(tokenType, start, end);
		cas.addFsToIndexes(tokenAnno);

	}

	/**
	 * Twitter text comes HTML-escaped, so unescape it. We also first unescape
	 * &amp;'s, in case the text has been buggily double-escaped.
	 */
	public static String normalizeText(String text) {
		text = text.replaceAll("&amp;", "&");
		text = StringEscapeUtils.unescapeHtml(text);
		return text;
	}

}
