package de.tudarmstadt.ukp.dkpro.core.frequency;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestFrequencyProviderTest
{

    @Test
    public void web1tTest_indexFiles() throws Exception
    {
        FrequencyCountProvider provider = new TestFrequencyCountProvider();
        
        assertEquals(10,   provider.getFrequency("1"));
        assertEquals(100,  provider.getFrequency("22"));
        assertEquals(1000, provider.getFrequency("333"));
    }
}
