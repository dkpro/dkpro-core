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
package de.tudarmstadt.ukp.dkpro.core.api.frequency.provider;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyUtils;

public abstract class FrequencyCountProviderBase
    implements FrequencyCountProvider
{

    @Override
    public double getProbability(String phrase)
        throws Exception
    {
        long n = getNrOfNgrams(FrequencyUtils.getPhraseLength(phrase));
        
        if (n == 0) {
            return 0;
        }
        else {
            return (double) getFrequency(phrase) / n;
        }
    }
    
    @Override
    public double getLogProbability(String phrase)
        throws Exception
    {
        return Math.log(getProbability(phrase));
    }
    
    public double getLogLikelihood(int termFrequency, int sizeOfCorpus, String term) throws Exception {
        return FrequencyUtils.loglikelihood(
                termFrequency,
                sizeOfCorpus,
                getFrequency(term),
                getNrOfTokens()
        );
    }
}