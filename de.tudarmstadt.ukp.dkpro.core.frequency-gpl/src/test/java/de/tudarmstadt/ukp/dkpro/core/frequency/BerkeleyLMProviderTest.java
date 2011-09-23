package de.tudarmstadt.ukp.dkpro.core.frequency;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

public class BerkeleyLMProviderTest
{

    @Ignore
    @Test
    public void berkeleyLMTest() throws Exception {
        BerkeleyLMProvider lm  = new BerkeleyLMProvider(
                "src/test/resources/test.ser"
        );

        assertEquals(50, lm.getFrequency("of the ("));
        assertEquals(0.011155508, lm.getProbability("is"),     0.0000001);
        assertEquals(-4.49582195, lm.getLogProbability("is"),  0.0000001);
    }
}