package de.tudarmstadt.ukp.dkpro.core.toolbox.tutorial;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;
import de.tudarmstadt.ukp.dkpro.core.ngrams.util.NGramStringIterable;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Sentence;
import de.tudarmstadt.ukp.dkpro.core.toolbox.corpus.BrownCorpus;

public class BrownNgrams {

	public static void main(String[] args) throws Exception
    {
        BrownCorpus brownCorpus = new BrownCorpus();
        
        // disregarding sentence boundaries
        FrequencyDistribution<String> ngramFreqDist = new FrequencyDistribution<String>(
        		new NGramStringIterable(brownCorpus.getTokens(), 1, 2)
        );
        
        // regarding sentence boundaries
        FrequencyDistribution<String> ngramFreqDist2 = new FrequencyDistribution<String>();
        for (Sentence s : brownCorpus.getSentences()) {
        	ngramFreqDist2.incAll(
        		new NGramStringIterable(s.getTokens(), 1, 5)
        	);
        }
        
        System.out.println(ngramFreqDist.getCount("the"));
        System.out.println(ngramFreqDist.getCount("the old man"));
        System.out.println();
        System.out.println(ngramFreqDist2.getCount("the"));
        System.out.println(ngramFreqDist2.getCount("the old man"));
        
    }
}
