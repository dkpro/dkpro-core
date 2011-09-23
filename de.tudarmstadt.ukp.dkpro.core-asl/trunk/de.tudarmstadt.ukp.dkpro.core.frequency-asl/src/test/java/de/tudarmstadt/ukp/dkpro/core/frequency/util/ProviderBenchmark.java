package de.tudarmstadt.ukp.dkpro.core.frequency.util;

import org.apache.commons.lang.time.StopWatch;

import de.tudarmstadt.ukp.dkpro.core.frequency.Web1TFrequencyCountProvider;
import de.tudarmstadt.ukp.dkpro.teaching.core.Sentence;
import de.tudarmstadt.ukp.dkpro.teaching.core.Text;
import de.tudarmstadt.ukp.dkpro.teaching.corpus.BrownCorpus;

public class ProviderBenchmark
{
    public static void main(String[] args) throws Exception
    {
        Web1TFrequencyCountProvider web1t = new Web1TFrequencyCountProvider(args);
        BrownCorpus brown = new BrownCorpus();
        
        StopWatch watch = new StopWatch();
        watch.start();
        watch.suspend();

        for (Text text : brown.getTexts()) {
            for (Sentence s : text.getSentences()) {
                for (String t : s.getTokens()) {
                    watch.resume();
                    web1t.getFrequency(t);
                    watch.suspend();
                }
            }
        }
        
        double time = (double) watch.getTime() / 1000;
        System.out.println(time + "s");
    }
}
