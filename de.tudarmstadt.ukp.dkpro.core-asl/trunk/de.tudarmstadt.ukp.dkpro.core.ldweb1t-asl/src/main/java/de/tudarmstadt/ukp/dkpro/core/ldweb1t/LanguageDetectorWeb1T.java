/*******************************************************************************
 * Copyright 2013
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
package de.tudarmstadt.ukp.dkpro.core.ldweb1t;

import static de.tudarmstadt.ukp.dkpro.core.frequency.Web1TProviderBase.BOS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.provider.FrequencyCountProvider;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.ngrams.util.NGramStringIterable;

/**
 * Language detector based on n-gram frequency counts, e.g. as provided by Web1T
 * 
 * @author zesch
 *
 */
public class LanguageDetectorWeb1T
    extends JCasAnnotator_ImplBase
{

    /**
     * An array of external resources of frequency providers (one for each language that should be detected). 
     */
    public static final String PARAM_FREQUENCY_PROVIDER_RESOURCES = "frequencyProviders";
    @ExternalResource(key = PARAM_FREQUENCY_PROVIDER_RESOURCES, mandatory = true)
    private FrequencyCountProvider[] frequencyProviders;

    /**
     * The minimum n-gram size that should be considered. Default is 1. 
     */
    public static final String PARAM_MIN_NGRAM_SIZE = "minNGramSize";
    @ConfigurationParameter(name = PARAM_MIN_NGRAM_SIZE, mandatory = true, defaultValue = "1")
    private int minNGramSize;
    
    /**
     * The maximum n-gram size that should be considered. Default is 3. 
     */
    public static final String PARAM_MAX_NGRAM_SIZE = "maxNGramSize";
    @ConfigurationParameter(name = PARAM_MAX_NGRAM_SIZE, mandatory = true, defaultValue = "3")
    private int maxNGramSize;

    private Map<String,FrequencyCountProvider> providerMap;
    
    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        
        providerMap = new HashMap<String,FrequencyCountProvider>();
        
        for (FrequencyCountProvider provider : frequencyProviders) {
            try {
                providerMap.put(provider.getLanguage(), provider);
            }
            catch (Exception e) {
                throw new ResourceInitializationException(e);
            }
        }
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
      
        List<String> words = JCasUtil.toText(JCasUtil.select(jcas, Token.class));
        
        if (words.size() < 1) {
            return;
        }
        
        List<String> ngrams = new ArrayList<String>();
        if (words.size() > 1) {
            ngrams.add(getNgram(BOS, words.get(0), words.get(1)));
        }
        
        for (String ngram : new NGramStringIterable(words, 1, 3)) {
            ngrams.add(ngram);
        }
                
        try {
            Map<String,Double> langProbs = getLanguageProbabilities(ngrams);
            
            String maxLanguage = "x-unspecified";
            double maxLogProb = Double.NEGATIVE_INFINITY;
            for (String lang : langProbs.keySet()) {
                double prob = langProbs.get(lang);
                if (prob > maxLogProb) {
                    maxLogProb = prob;
                    maxLanguage = lang;
                }
                System.out.println(lang + " - " + prob);
            }
            jcas.setDocumentLanguage(maxLanguage);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
    
    private Map<String,Double> getLanguageProbabilities(List<String> ngrams)
            throws Exception
    {
        Map<String,Double> langProbs = new HashMap<String,Double>();
       
        for (String lang : providerMap.keySet()) {
                                    
            FrequencyCountProvider provider = providerMap.get(lang);
            
            long nrOfUnigrams = provider.getNrOfNgrams(1);
            long nrOfBigrams  = provider.getNrOfNgrams(2);
            long nrOfTrigrams = provider.getNrOfNgrams(3);
            
            double textLogProbability = 0.0;
            
            for (String ngram : ngrams) {
                
                long frequency = provider.getFrequency(ngram);

                int ngramSize = FrequencyUtils.getPhraseLength(ngram);
                                
                long normalization = 1;
                int weighting = 1;
                if (ngramSize == 1) {
                    normalization = nrOfUnigrams;
                }
                else if (ngramSize == 2) {
                    weighting = 2;
                    normalization = nrOfBigrams;
                }
                else if (ngramSize == 3) {
                    weighting = 4;
                    normalization = nrOfTrigrams;
                }
    
                if (frequency > 0) {
                    double logProb = Math.log( weighting * ((double) frequency) / normalization );
                    
                    textLogProbability += logProb;
                }
                else {
                    textLogProbability += Math.log( 1.0 / normalization);
                }
            }
            
            langProbs.put(lang, textLogProbability);
        }
        
        return langProbs;
    }
    
    private String getNgram(String ...strings) {
        return StringUtils.join(strings, " ");
    }
}