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

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.languagetool.Language;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterBase;

/**
 * Segmenter using LanguageTool to do the heavy lifting. LanguageTool internally uses different
 * strategies for tokenization.
 * 
 * @author Richard Eckart de Castilho
 */
public class LanguageToolSegmenter extends SegmenterBase
{
	@Override
	protected void process(JCas aJCas, String aText, int aZoneBegin)
		throws AnalysisEngineProcessException
	{
		Language lang = Language.getLanguageForShortName(aJCas.getDocumentLanguage());
		
		List<String> sentences = lang.getSentenceTokenizer().tokenize(aText);
		
		int lastSStart = 0;
		for (String s : sentences) {
			int sStart = aText.indexOf(s, lastSStart);
			int sEnd = sStart + s.length();
			lastSStart = sEnd;
			
			sStart += aZoneBegin;
			sEnd += aZoneBegin;
			
			createSentence(aJCas, sStart, sEnd);
			
			List<String> tokens = lang.getWordTokenizer().tokenize(s);
			int lastTStart = 0;
			for (String t : tokens) {
				int tStart = s.indexOf(t, lastTStart);
				
				if (tStart == -1) {
					// The chinese tokenizer adds some /xxx suffixes, try to remove that
					int suffix = t.indexOf('/');
					if (suffix != -1) {
						t = t.substring(0,  suffix);
					}
					tStart = s.indexOf(t, lastTStart);
				}
				
				if (tStart == -1) {
					throw new IllegalStateException("Token [" + t + "] not found in sentence [" + s
							+ "]");
				}
				
				int tEnd = tStart + t.length();
				lastTStart = tEnd;
			
				tStart += sStart;
				tEnd += sStart;
				
				createToken(aJCas, tStart, tEnd);
			}
		}
	}
}
