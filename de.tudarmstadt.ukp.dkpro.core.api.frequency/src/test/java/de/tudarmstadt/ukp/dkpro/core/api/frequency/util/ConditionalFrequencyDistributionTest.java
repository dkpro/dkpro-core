package de.tudarmstadt.ukp.dkpro.core.api.frequency.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.ConditionalFrequencyDistribution;

public class ConditionalFrequencyDistributionTest
{

    @Test
    public void cfdTest() {
        
        String condition1 = "text1";
        String condition2 = "text2";
        
        List<String> tokens1 = Arrays.asList("This is a first test that contains a first test example".split(" "));
        List<String> tokens2 = Arrays.asList("This second example contains other example tokens".split(" "));
        
        ConditionalFrequencyDistribution<String, String> cfd = new ConditionalFrequencyDistribution<String, String>();
        cfd.addSamples(condition1, tokens1);
        cfd.addSamples(condition2, tokens2);
        
        System.out.println(cfd);
        
        assertEquals(2, cfd.getConditions().size());
        for (String condition : cfd.getConditions()) {
            assertTrue(condition.equals(condition1) || condition.equals(condition2));
        }
        
        assertEquals(18, cfd.getN());
        
        assertEquals(0, cfd.getCount(condition1, "humpelgrumpf"));
        assertEquals(1, cfd.getCount(condition1, "This"));
        assertEquals(1, cfd.getCount(condition2, "This"));
        assertEquals(2, cfd.getCount(condition1, "test"));
        assertEquals(2, cfd.getCount(condition2, "example"));
    }
}
