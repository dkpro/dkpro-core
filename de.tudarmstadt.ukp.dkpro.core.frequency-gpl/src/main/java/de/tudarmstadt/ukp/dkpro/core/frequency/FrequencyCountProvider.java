package de.tudarmstadt.ukp.dkpro.core.frequency;

public interface FrequencyCountProvider
{

    /**
     * Get frequency for a phrase
     *
     * @param phrase
     *            phrase to search
     *            
     * @return frequency of the phrase. Returns 0 if the phrase does not exist in the corpus.
     * @throws Exception 
     */
    public long getFrequency(String phrase) throws Exception;

    /**
     * Get the probability (=normalized frequency) for a phrase.<br>
     * The frequency count is normalized by the number of phrases in the corpus.
     * 
     *
     * @param phrase
     *            phrase to search
     *            
     * @return probability (=normalized frequency) of the phrase. Returns null if the phrase does not exist in the corpus.
     * @throws Exception 
     */
    public double getProbability(String phrase) throws Exception;

    /**
     * Get the log probability for a phrase
     *
     * @param phrase
     *            phrase to search
     *            
     * @return The log probability of the. Returns 0 if the phrase does not exist in the corpus.
     * @throws Exception 
     */
    public double getLogProbability(String phrase) throws Exception;
    
    /**
     * @return The number of tokens in the corpus.
     */
    public long getNrOfTokens() throws Exception;
    
    /**
     * Returns the number of ngrams of a given size in the corpus.
     */
    public long getNrOfNgrams(int n) throws Exception;
}