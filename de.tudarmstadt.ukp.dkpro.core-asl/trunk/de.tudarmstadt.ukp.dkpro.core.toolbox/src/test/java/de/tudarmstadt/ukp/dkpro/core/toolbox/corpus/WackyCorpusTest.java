package de.tudarmstadt.ukp.dkpro.core.toolbox.corpus;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.toolbox.corpus.WackyCorpus.WackyLanguageEdition;

public class WackyCorpusTest
{
    @Test
    public void wackyCorpusTest() throws Exception {
        
        WackyCorpus corpus = new WackyCorpus(
                "src/test/resources/test_corpora/wacky/",
                WackyLanguageEdition.DEWAC
        );

        assertEquals(WackyLanguageEdition.DEWAC.name(), corpus.getName());
        assertEquals("de", corpus.getLanguage());
        
        int i=0;
        while (corpus.hasNextText()) {
            corpus.getNextText();
            i++;
        }
        assertEquals(4, i);
    }
}
