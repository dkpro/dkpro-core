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

    /**
     * Setting this to something higher than 1, will linearly scale down the returned frequency counts.
     * E.g. setting it to 10, will return 1/10 of the actual frequency counts.
     * This way, smaller n-gram models can be estimated.
     * 
     * The same functionality could be implemented in the components using the provider,
     *   but they all needed to be aware of that down-scaling then which is undesirable.
     * However, it might be much faster, as the actual frequency count only needs to be retrieved once,
     *   and different down-scaling factors can be tried instantly.
     * So in some situations, it might be more efficient to return the unscaled score and do it yourself.
     * 
     * Keep in mind that the resulting n-gram model is only a rough estimate of a really down-scaled model.
     * Especially normalizing with the number of n-grams will not give exact results,
     *   as some of them now have zero counts and should not be counted.
     * As long as one stays within the same n-gram model, the effects should be relative and can be ignored.
     * However, comparing the relative frequencies from a down-scaled model with another model is invalid. 
     */
    private int scaleDownFactor = 1;
    
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

    @Override
    public long getFrequency(String phrase)
        throws Exception
    {
        long frequency = getFrequencyFromProvider(phrase);
        return frequency / getScaleDownFactor();
    }

    protected abstract long getFrequencyFromProvider(String phrase)
        throws Exception;
    
    public double getLogLikelihood(int termFrequency, int sizeOfCorpus, String term) throws Exception {
        return FrequencyUtils.loglikelihood(
                termFrequency,
                sizeOfCorpus,
                getFrequency(term),
                getNrOfTokens()
        );
    }

    public int getScaleDownFactor()
    {
        return scaleDownFactor;
    }

    public void setScaleDownFactor(int scaleDownFactor)
    {
        if (scaleDownFactor > 0) {
            this.scaleDownFactor = scaleDownFactor;
        }
        else {
            System.err.println("Invalid scale down factor. It needs to be larger than 0.");
        }
    }
}