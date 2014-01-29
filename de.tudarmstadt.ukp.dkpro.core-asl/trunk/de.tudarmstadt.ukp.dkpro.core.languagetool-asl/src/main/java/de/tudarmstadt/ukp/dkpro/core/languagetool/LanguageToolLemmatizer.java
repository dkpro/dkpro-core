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

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;
import static org.apache.uima.fit.util.JCasUtil.toText;

import java.io.IOException;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
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
    private MappingProvider mappingProvider;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        mappingProvider = new MappingProvider();
        mappingProvider.setDefault(MappingProvider.VARIANT, "default");
        mappingProvider.setDefaultVariantsLocation(
                "de/tudarmstadt/ukp/dkpro/core/languagetool/lib/language-tagset.map");
        mappingProvider.setDefault(MappingProvider.LOCATION, 
                "classpath:/de/tudarmstadt/ukp/dkpro/core/api/lexmorph/tagset/${language}-${variant}.map");
    }
    
	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
	    mappingProvider.configure(aJCas.getCas());
	    
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

					String l = null;
					
					// Try using the POS to disambiguate the lemma
					if (token.getPos() != null) {
					    l = getByPos(token.getPos(), as.getTokens()[i]);
					}
					
					// Get the most frequent lemma
					if (l == null) {
					    l = getMostFrequentLemma(as.getTokens()[i]);
					}
					
					if (l == null) {
					    l = token.getCoveredText();
					}

					// Create the annotation
					Lemma lemma = new Lemma(aJCas, token.getBegin(), token.getEnd());
					lemma.setValue(l);
					lemma.addToIndexes();
					token.setLemma(lemma);
				}
			}
		}
		catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

    private String getByPos(POS aPos, AnalyzedTokenReadings aReadings)
    {
        String tag = aPos.getPosValue();
        //System.out.printf("%s %n", tag);
        for (AnalyzedToken t : aReadings.getReadings()) {
            //System.out.printf("-- %s %s ", t.getPOSTag(), t.getLemma());
            
            // Lets see if we have mapped tagsets
            try {
                String typeName = mappingProvider.getTagType(t.getPOSTag()).getName();
                if (aPos.getClass().getName().equals(typeName)) {
                    //System.out.printf("- mapped match%n");
                    return t.getLemma();
                }
            }
            catch (IllegalStateException e) {
                // Type could not be looked up. Go on with other types of matching
            }
            
            // Full match... feeling lucky ;) This is quite unlikely to happen because the tagset
            // used by LanguageTool is most certainly different from tagset used by POS tagger.
            if (tag.equals(t.getPOSTag())) {
                //System.out.printf("- full match%n");
                return t.getLemma();
            }
            
            // Some tagsets used by LanguageTool use ':' as separator. If we are lucky, the string
            // before the first ':' matches our POS tag.
            if (tag.equals(t.getPOSTag().split(":")[0])) {
                //System.out.printf("- first element match%n");
                return t.getLemma();
            }
            
            //System.out.printf("- no match%n");
        }
        
        //System.out.printf("- no reading matches%n");
        return null;
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
