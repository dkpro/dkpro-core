package de.tudarmstadt.ukp.dkpro.core.frequency.util;

public class FrequencyUtils
{
    /**
     * Return the length of the phrase in tokens, i.e. which n-th gram this is.
     * @param phrase The phrase to test.
     * @return The length of the phrase in tokens
     */
    public static int getPhraseLength(String phrase) {
        return phrase.split(" ").length;
    }

    public static double loglikelihood(
            long tfCorpus1,
            long sizeCorpus1,
            long tfCorpus2,
            long sizeCorpus2)
    {
        double E1 = (double) sizeCorpus1 * (tfCorpus1 + tfCorpus2) / (sizeCorpus1 + sizeCorpus2);
        double E2 = (double) sizeCorpus2 * (tfCorpus1 + tfCorpus2) / (sizeCorpus1 + sizeCorpus2);

        double G2 = 2 * ((tfCorpus1 * Math.log(tfCorpus1 / E1)) + (tfCorpus2 * Math.log(tfCorpus2 / E2)));
        return G2;
    }
}