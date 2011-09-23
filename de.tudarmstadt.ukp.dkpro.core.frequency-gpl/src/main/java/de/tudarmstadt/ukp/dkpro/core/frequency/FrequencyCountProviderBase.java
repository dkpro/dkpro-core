package de.tudarmstadt.ukp.dkpro.core.frequency;

import de.tudarmstadt.ukp.dkpro.core.frequency.util.FrequencyUtils;

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
