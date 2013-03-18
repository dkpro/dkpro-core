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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;

/**
 * This class creates paragraph annotations for the given input document. It searches for the
 * occurrence of two or more line-breaks (Unix and Windows) and regards this as the boundary between
 * paragraphs.
 *
 * @author Niklas Jakob
 */
public class ParagraphSplitter
	extends JCasAnnotator_ImplBase
{
	public static final String SINGLE_LINE_BREAKS_PATTERN = "((\n\r\n)+(\r\n)*)|((\n)+(\n)*)";
	public static final String DOUBLE_LINE_BREAKS_PATTERN = "((\r\n\r\n)+(\r\n)*)|((\n\n)+(\n)*)";

	/**
	 * A regular expression used to detect paragraph splits.
	 * 
	 * Default: {@link #DOUBLE_LINE_BREAKS_PATTERN} (split on two consecutive line breaks)
	 */
	public static final String PARAM_SPLIT_PATTERN = "splitPattern";
	@ConfigurationParameter(name = PARAM_SPLIT_PATTERN, defaultValue = DOUBLE_LINE_BREAKS_PATTERN)
	private Pattern splitPattern;

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		String input = aJCas.getDocumentText();

		if (input.length() < 1) {
			throw new AnalysisEngineProcessException(new Throwable("Document text is empty."));
		}

		Pattern ParagraphPattern = splitPattern;
		Matcher matcher = ParagraphPattern.matcher(input);
		int pos = 0;
		int nextBeginning = 0;
		while (matcher.find(pos)) {
			Paragraph paragraph = new Paragraph(aJCas, nextBeginning, matcher.start());
			paragraph.addToIndexes();
			nextBeginning = matcher.end();
			pos = matcher.end();
		}
		if (pos < input.length()) {
			Paragraph paragraph = new Paragraph(aJCas, nextBeginning, input.length());
			paragraph.addToIndexes();
		}
	}
}
