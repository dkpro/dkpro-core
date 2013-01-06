/*******************************************************************************
 * Copyright 2012
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

import static org.uimafit.util.JCasUtil.select;
import static org.uimafit.util.JCasUtil.selectCovered;
import static org.uimafit.util.JCasUtil.toText;

import java.io.IOException;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.TypeCapability;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Naive lexicon-based lemmatizer. The words are looked up using the wordform lexicons of
 * LanguageTool. Multiple readings are produced. The annotator simply takes the most frequent
 * lemma from those readings. If no readings could be found, the original text is assigned as
 * lemma.
 * 
 * @author Richard Eckart de Castilho
 */
@TypeCapability(
	    inputs = { 
	        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
	        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" },
	    outputs = {
		    "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma" })
public class LanguageToolLemmatizer
	extends JCasAnnotator_ImplBase
{
	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		try {
			Language lang = Language.getLanguageForShortName(aJCas.getDocumentLanguage());
			
			for (Sentence s : select(aJCas, Sentence.class)) {
				// Get the tokens from the sentence
				List<Token> tokens = selectCovered(Token.class, s);
				List<String> tokenText = toText(tokens);
				
				// Let LanguageTool analyze the tokens
				List<AnalyzedTokenReadings> rawTaggedTokens = lang.getTagger().tag(tokenText);
				AnalyzedSentence as = new AnalyzedSentence(
						rawTaggedTokens.toArray(new AnalyzedTokenReadings[rawTaggedTokens.size()]));
				as = lang.getDisambiguator().disambiguate(as);
				
				for (int i = 0; i < tokens.size(); i++) {
					Token token = tokens.get(i);
					
					// Get the most frequent lemma
					String best = getMostFrequentLemma(as.getTokens()[i]);

					// Create the annotation
					Lemma lemma = new Lemma(aJCas, token.getBegin(), token.getEnd());
					lemma.setValue((best != null) ? best : token.getCoveredText());
					lemma.addToIndexes();
					token.setLemma(lemma);
				}
			}
		}
		catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}
	
	private String getMostFrequentLemma(AnalyzedTokenReadings aReadings)
	{
		FrequencyDistribution<String> freq = new FrequencyDistribution<String>();
		for (AnalyzedToken t : aReadings.getReadings()) {
			if (t.getLemma() != null) {
				freq.inc(t.getLemma());
			}
		}
		
		String best = null;
		for (String l : freq.getKeys()) {
			if (best == null) {
				best = l;
			}
			else if (freq.getCount(best) < freq.getCount(l)) {
				best = l;
			}
		}
		
		return best;
	}
}
