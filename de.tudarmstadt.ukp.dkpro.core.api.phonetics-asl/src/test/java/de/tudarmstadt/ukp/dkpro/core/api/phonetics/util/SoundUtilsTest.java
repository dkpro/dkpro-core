package de.tudarmstadt.ukp.dkpro.core.api.phonetics.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class SoundUtilsTest
{
    @Test
    public void arpabetToIPATest() throws Exception
    {
        assertEquals("ˈɛndˌɪnɚkwˈoʊt", SoundUtils.arpabetToIPA("   EH1 N D  IH2 N ER0 K W OW1 T "));
    }
}
