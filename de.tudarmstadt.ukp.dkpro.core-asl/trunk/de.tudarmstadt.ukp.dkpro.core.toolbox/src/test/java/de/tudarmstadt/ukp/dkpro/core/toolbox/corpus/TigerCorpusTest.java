package de.tudarmstadt.ukp.dkpro.core.toolbox.corpus;

import org.junit.Ignore;
import org.junit.Test;

public class TigerCorpusTest
{

    @Test
    public void tigerTest() throws Exception {
        TigerCorpus corpus = new TigerCorpus("src/test/resources/test_corpora/tiger/tiger.txt");
        while (corpus.hasNextText()) {
            String text = corpus.getNextText();
            System.out.println(text);
        }
    }

    @Ignore
    @Test
    public void tigerTest_DKPRO_HOME() throws Exception {
        TigerCorpus corpus = new TigerCorpus();
        int i=0;
        while (i < 10 && corpus.hasNextText()) {
            String text = corpus.getNextText();
            System.out.println(text);
            i++;
        }
    }
}
