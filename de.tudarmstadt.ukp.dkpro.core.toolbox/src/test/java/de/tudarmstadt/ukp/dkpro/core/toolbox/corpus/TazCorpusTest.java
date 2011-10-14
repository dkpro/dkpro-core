package de.tudarmstadt.ukp.dkpro.core.toolbox.corpus;

import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Sentence;

public class TazCorpusTest
{

    @Ignore
    @Test
    public void tazTest() throws Exception {
        TazCorpus corpus = new TazCorpus();
        for (Sentence s : corpus.getSentences()) {
            for (int i=0; i<s.getTokens().size(); i++) {
                System.out.print(s.getTokens().get(i));
            }
        }
    }
    
}
