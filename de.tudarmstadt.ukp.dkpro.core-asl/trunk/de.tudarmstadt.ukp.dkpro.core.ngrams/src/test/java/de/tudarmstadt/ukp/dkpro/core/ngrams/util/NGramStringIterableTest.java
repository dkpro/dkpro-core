package de.tudarmstadt.ukp.dkpro.core.ngrams.util;


import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class NGramStringIterableTest
{
    @Test
    public void ngramTest() {
        
        String[] tokens = "This is a simple example sentence .".split(" ");
        
        int i=0;
        for (String ngram : new NGramStringIterable(tokens, 2, 2)) {
            if (i==0) {
                assertEquals("This is", ngram);
            }
            
            System.out.println(ngram);
            i++;
        }
        assertEquals(6, i);
    }
}
