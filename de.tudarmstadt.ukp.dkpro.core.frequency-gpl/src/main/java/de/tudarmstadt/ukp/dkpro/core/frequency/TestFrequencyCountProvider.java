package de.tudarmstadt.ukp.dkpro.core.frequency;

import java.io.IOException;

/**
 *  Frequency provider to be used in unit tests.
 *  The returned frequency is based on the number of characters.
 */
public class TestFrequencyCountProvider
    extends FrequencyCountProviderBase
{

    
    @Override
    public long getFrequency(String phrase)
        throws IOException
    {
        return new Double(Math.floor(
                            Math.pow(
                                    10.0,
                                    new Integer(phrase.length()).doubleValue())
                            )
                         ).longValue();
    }

    @Override
    public long getNrOfNgrams(int n) {
        return 100 * n;
    }
    
    @Override
    public long getNrOfTokens() {
        return 100;
    }
}