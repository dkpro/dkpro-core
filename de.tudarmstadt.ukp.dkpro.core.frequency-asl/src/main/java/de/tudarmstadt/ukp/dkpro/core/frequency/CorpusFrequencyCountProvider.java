/*******************************************************************************
 * Copyright 2011
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
package de.tudarmstadt.ukp.dkpro.core.frequency;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.provider.FrequencyCountProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyUtils;
import de.tudarmstadt.ukp.dkpro.teaching.core.ConditionalFrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.teaching.core.Sentence;
import de.tudarmstadt.ukp.dkpro.teaching.corpus.Corpus;
import de.tudarmstadt.ukp.dkpro.teaching.ngram.NGramIterable;

public class CorpusFrequencyCountProvider
    extends FrequencyCountProviderBase
{

    private final ConditionalFrequencyDistribution<Integer,String> cfd;
    
    public CorpusFrequencyCountProvider(Corpus corpus, int minN, int maxN) throws Exception {
        cfd = new ConditionalFrequencyDistribution<Integer,String>();
        
        if (minN > maxN) {
            throw new IllegalArgumentException("minN > maxN");
        }
        
        for (int i=minN; i<=maxN; i++) {
            for (Sentence s : corpus.getSentences()) {
                cfd.addSamples(
                        i,
                        new NGramIterable(s.getTokens(), i, i)
                );
            }
        }
    }
    
    @Override
    public long getFrequency(String phrase) throws Exception {
        int phraseLength = FrequencyUtils.getPhraseLength(phrase);
        
        if (cfd.hasCondition(phraseLength)) {
            return cfd.getCount(phraseLength, phrase);
        }
        else {
            return 0;
        }
    }

    @Override
    public double getProbability(String phrase)
        throws Exception
    {
        long count = getFrequency(phrase);

        long N = cfd.getN();
        
        if (N == 0) {
            return 0;
        }
        else {
            return (double) count / N;
        }
    }

    @Override
    public double getLogProbability(String phrase)
        throws Exception
    {
        return Math.log(getProbability(phrase));
    }

    @Override
    public long getNrOfTokens()
        throws Exception
    {
        return cfd.getFrequencyDistribution(1).getN();
    }

    @Override
    public long getNrOfNgrams(int n)
        throws Exception
    {
        // FIXME implement this 
        throw new Exception("Not implemented yet.");
    }
}