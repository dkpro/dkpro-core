package de.tudarmstadt.ukp.dkpro.core.toolbox.core;
import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;

import org.junit.Test;

public class TagTest
{

    @Test
    public void test() throws MalformedURLException {
        
        Tag tag = new Tag("NNS", "en");
        assertEquals("NN", tag.getSimplifiedTag());
    }
}
