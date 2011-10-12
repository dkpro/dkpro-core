package de.tudarmstadt.ukp.dkpro.core.api.frequency.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.util.FrequencyDistribution;

public class FrequencyDistributionTest
{

    @Test
    public void cfdTest() {
        
        List<String> tokens = Arrays.asList("This is a first test that contains a first test example".split(" "));
        
        FrequencyDistribution<String> fd = new FrequencyDistribution<String>();
        fd.incAll(tokens);
        
        System.out.println(fd);
        
        assertEquals(11, fd.getN());
        assertEquals(8, fd.getB());
        
        assertEquals(0, fd.getCount("humpelgrumpf"));
        assertEquals(1, fd.getCount("This"));
        assertEquals(2, fd.getCount("test"));
    }
}