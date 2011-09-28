package de.tudarmstadt.ukp.dkpro.core.frequency;

import java.util.Arrays;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.provider.FrequencyCountProvider;
import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.io.LmReaders;

/**
 * Wrapper for the Berkeley LM package.
 *
 * nGram index files are looked up in DKPRO_HOME directory.
 */
public class BerkeleyLMProvider
	implements FrequencyCountProvider
{

    private final NgramLanguageModel<String> lm;
    
	public BerkeleyLMProvider(String binaryFile)
	    throws Exception
	{
	    lm = LmReaders.readLmBinary(binaryFile);
	}

	// FIXME how to obtain phrase count from logProb
    @Override
    public long getFrequency(String phrase)
		throws Exception
	{
        throw new Exception("Not implemented yet.");
//        return getProbnew Float(Math.exp(logProb)).longValue();
	}

    @Override
    public double getProbability(String phrase)
        throws Exception
    {
        return Math.exp(getLogProbability(phrase));
    }

    @Override
    public double getLogProbability(String phrase)
        throws Exception
    {
        return lm.getLogProb(Arrays.asList(phrase.split(" ")));
    }

    @Override
    public long getNrOfTokens()
        throws Exception
    {
        return lm.getWordIndexer().numWords();
    }

    @Override
    public long getNrOfNgrams(int n)
        throws Exception
    {
        throw new Exception("Not implemented yet.");
    }

    @Override
    public long getNrOfDistinctNgrams(int n)
        throws Exception
    {
        throw new Exception("Not implemented yet.");
    }
}