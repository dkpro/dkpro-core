package de.tudarmstadt.ukp.dkpro.core.frequency;

import java.io.IOException;

import com.googlecode.jweb1t.Searcher;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.provider.FrequencyCountProviderBase;

public abstract class Web1TProviderBase
    extends FrequencyCountProviderBase
{

    protected Searcher searcher;

    @Override
    public long getNrOfNgrams(int n)
    {
        return searcher.getNrOfNgrams(n);
    }

    public long getNrOfTokens()
    {
        return searcher.getNrOfNgrams(1);
    }

    @Override
    public long getNrOfDistinctNgrams(int n)
        throws Exception
    {
        return searcher.getNrOfDistinctNgrams(n);
    }
    
    @Override
    protected long getFrequencyFromProvider(String phrase)
        throws IOException
    {
        return searcher.getFrequency(phrase);
    }
}