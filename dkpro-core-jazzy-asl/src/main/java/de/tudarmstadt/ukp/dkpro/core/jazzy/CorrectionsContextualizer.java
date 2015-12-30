/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.core.jazzy;

import static de.tudarmstadt.ukp.dkpro.core.frequency.Web1TProviderBase.BOS;
import static de.tudarmstadt.ukp.dkpro.core.jazzy.util.ContextualizerUtils.getCandidatePosition;
import static de.tudarmstadt.ukp.dkpro.core.jazzy.util.ContextualizerUtils.getChangedWords;
import static de.tudarmstadt.ukp.dkpro.core.jazzy.util.ContextualizerUtils.getTrigram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SpellingAnomaly;
import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SuggestedAction;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.provider.FrequencyCountProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.ngrams.util.NGramStringIterable;

/**
 * This component assumes that some spell checker has already been applied upstream (e.g. Jazzy).
 * It then uses ngram frequencies from a frequency provider in order to rank the provided corrections. 
 * 
 */
public class CorrectionsContextualizer
    extends JCasAnnotator_ImplBase
{
    
    public final static String FREQUENCY_PROVIDER_RESOURCE = "FrequencyProvider";
    @ExternalResource(key = FREQUENCY_PROVIDER_RESOURCE)
    private FrequencyCountProvider provider;
    
    protected Map<String,Long> countCache;
    
    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        countCache = new HashMap<String,Long>();
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
            List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class, sentence);
            List<String> tokenStrings = JCasUtil.toText(tokens);
            for (SpellingAnomaly anomaly : JCasUtil.selectCovered(jcas, SpellingAnomaly.class, sentence)) {                
                
                FSArray suggestedActions = anomaly.getSuggestions();
                int n = suggestedActions.size();
                FSArray newActions = new FSArray(jcas, n + 1);
                for (int i=0; i<n; i++) {
                    SuggestedAction action = (SuggestedAction) suggestedActions.get(i);

                    List<String> changedWords = getChangedWords(action.getReplacement(), tokenStrings, getCandidatePosition(anomaly, tokens));
                    
                    double probability = getSentenceProbability(changedWords);
                    
                    action.setCertainty((float) probability);
                    newActions.set(i, action);
                    
                }
                
                // add the original word as a possibility
                // might turn out that it fits in well according to ngram model
                SuggestedAction newAction = new SuggestedAction(jcas);
                newAction.setReplacement(anomaly.getCoveredText());
                newAction.setCertainty((float) getSentenceProbability(tokenStrings));
                newActions.set(n, newAction);
                
                anomaly.setSuggestions(newActions);
            }        
        }
    }
    
    protected double getSentenceProbability(List<String> words) throws AnalysisEngineProcessException  {
        double sentenceProbability = 0.0;
        
        if (words.size() < 1) {
            return 0.0;
        }
        
        long nrOfUnigrams;
        try {
            nrOfUnigrams = provider.getNrOfTokens();
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
        
        List<String> trigrams = new ArrayList<String>();

        // in the google n-grams this is not represented (only single BOS markers)
        // but I leave it in place in case we add another n-gram provider
        trigrams.add(getTrigram(BOS, BOS, words.get(0)));
        
        if (words.size() > 1) {
            trigrams.add(getTrigram(BOS, words.get(0), words.get(1)));
        }
        
        for (String trigram : new NGramStringIterable(words, 3, 3)) {
            trigrams.add(trigram);
        }
        
        // FIXME - implement backoff or linear interpolation

        for (String trigram : trigrams) {
            long trigramFreq = getNGramCount(trigram);

            String[] parts = StringUtils.split(trigram, " ");
            
            String bigram = StringUtils.join(Arrays.copyOfRange(parts, 0, 2), " ");
            long bigramFreq = getNGramCount(bigram);
            
            String unigram = StringUtils.join(Arrays.copyOfRange(parts, 0, 1), " ");
            long unigramFreq = getNGramCount(unigram);

            if (trigramFreq < 1) {
                trigramFreq = 1;
            }
            if (bigramFreq < 1) {
                bigramFreq = 1;
            }
            if (unigramFreq < 1) {
                unigramFreq = 1;
            }
            
            double trigramProb = Math.log( (double) trigramFreq / bigramFreq);
            double bigramProb  = Math.log( (double) bigramFreq  / unigramFreq);
            double unigramProb = Math.log( (double) unigramFreq / nrOfUnigrams);

            double interpolated = (trigramProb + bigramProb + unigramProb) / 3.0;
            
            sentenceProbability += interpolated;
        }
        
        return Math.exp(sentenceProbability);
    }
    
    protected long getNGramCount(String ngram) throws AnalysisEngineProcessException {
        if (!countCache.containsKey(ngram)) {
            try {
                countCache.put(ngram, provider.getFrequency(ngram));
            }
            catch (Exception e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
        
        return countCache.get(ngram);
    }
}