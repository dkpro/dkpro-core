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

import java.text.BreakIterator;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterBase;

/**
 * @author Richard Eckart de Castilho
 */
public
class BreakIteratorSegmenter
extends SegmenterBase
{
	public static final String PARAM_SPLIT_AT_APOSTROPHE = "SplitAtApostrophe";
	@ConfigurationParameter(name=PARAM_SPLIT_AT_APOSTROPHE, mandatory=true, defaultValue="false")
	private boolean splitAtApostrophe;

	@Override
	protected
	void process(
			JCas aJCas,
			String text,
			int zoneBegin)
	throws AnalysisEngineProcessException
	{
		BreakIterator bi = BreakIterator.getSentenceInstance(getLocale(aJCas));
		bi.setText(text);
		int last = bi.first() + zoneBegin;
		int cur = bi.next();
		while (cur != BreakIterator.DONE) {
			cur += zoneBegin;
			if (isCreateSentences()) {
				Annotation segment = createSentence(aJCas, last, cur);
				if (segment != null) {
					processSentence(aJCas, segment.getCoveredText(), segment.getBegin());
				}
			}
			else {
				int[] span = new int[] { last, cur };
				trim(aJCas.getDocumentText(), span);
				processSentence(aJCas, aJCas.getDocumentText().substring(span[0], span[1]), span[0]);
			}
			last = cur;
			cur = bi.next();
		}
	}

	/**
	 * Process the sentence to create tokens.
	 *
	 * @param aJCas
	 * @param text
	 * @param zoneBegin
	 */
	private
	void processSentence(
			JCas aJCas,
			String text,
			int zoneBegin)
	{
		BreakIterator bi = BreakIterator.getWordInstance(getLocale(aJCas));
		bi.setText(text);
		int last = bi.first() + zoneBegin;
		int cur = bi.next();
		while (cur != BreakIterator.DONE) {
			cur += zoneBegin;
			Annotation token = createToken(aJCas, last, cur);
			if (token != null) {
				if (splitAtApostrophe) {
					int i = token.getCoveredText().indexOf("'");
					if (i > 0) {
						i += token.getBegin();
						createToken(aJCas, i, token.getEnd());
						token.setEnd(i);
					}
				}
			}

			last = cur;
			cur = bi.next();
		}
	}
}
