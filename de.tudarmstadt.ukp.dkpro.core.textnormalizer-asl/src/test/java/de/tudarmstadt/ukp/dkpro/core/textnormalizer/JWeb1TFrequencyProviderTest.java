package de.tudarmstadt.ukp.dkpro.core.textnormalizer;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.provider.FrequencyCountProvider;
import de.tudarmstadt.ukp.dkpro.core.frequency.Web1TFileAccessProvider;

public class JWeb1TFrequencyProviderTest
{

    @Test
    public void testFrequencyProvider() throws IOException
    {
        FrequencyCountProvider provider = new Web1TFileAccessProvider(
                new File("src/test/resources/jweb1t"),1,1);
        
        assertEquals(1, provider.getFrequency("süß"));
        assertEquals(1, provider.getFrequency("Kresse"));
    }

}
