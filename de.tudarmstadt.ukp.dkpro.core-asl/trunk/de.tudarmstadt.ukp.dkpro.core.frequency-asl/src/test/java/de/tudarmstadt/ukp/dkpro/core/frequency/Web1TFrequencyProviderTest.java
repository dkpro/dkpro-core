package de.tudarmstadt.ukp.dkpro.core.frequency;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class Web1TFrequencyProviderTest
{

    @Test
    public void web1tTest_indexFiles() throws IOException
    {
        Web1TFrequencyCountProvider web1t = new Web1TFrequencyCountProvider(
                "src/test/resources/web1t/index-1gms",
                "src/test/resources/web1t/index-2gms"
        );
        
        assertEquals(2147436244l, web1t.getFrequency("!"));
        assertEquals(528,         web1t.getFrequency("Nilmeier"));
        assertEquals(106,         web1t.getFrequency("influx takes"));
        assertEquals(69,          web1t.getFrequency("frist will"));
    }

    @Test
    public void web1tTest_path() throws IOException
    {
        Web1TFrequencyCountProvider web1t = new Web1TFrequencyCountProvider(
                new File("src/test/resources/web1t/"),
                1,
                2
        );
        
        assertEquals(2147436244l, web1t.getFrequency("!"));
        assertEquals(528,         web1t.getFrequency("Nilmeier"));
        assertEquals(106,         web1t.getFrequency("influx takes"));
        assertEquals(69,          web1t.getFrequency("frist will"));
    }

}
