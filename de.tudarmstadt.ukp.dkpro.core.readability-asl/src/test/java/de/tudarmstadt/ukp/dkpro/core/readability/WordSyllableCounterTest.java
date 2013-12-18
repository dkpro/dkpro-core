package de.tudarmstadt.ukp.dkpro.core.readability;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.readability.measure.WordSyllableCounter;

public class WordSyllableCounterTest
{
	@Test
	public void countWordSyllTest_vowelPairs() 
		throws Exception	
	{
		WordSyllableCounter  wsc = new WordSyllableCounter("en");
		
		 assertEquals(4, wsc.countSyllables("analysis"));
		 assertEquals(2, wsc.countSyllables("teacher"));
		 
		 //TODO 
		 /*
		  * According to Linux 'style' algorithm, the syllables number
		  * of "readability" is 4. But in fact it should be 5. This means
		  * Linux'Style' algorithm is not always precise. 
		  */
		 assertEquals(4, wsc.countSyllables("readability"));
	}
	
	@Test
	public void countWordSyllTest_case() 
		throws Exception	
	{
		WordSyllableCounter wsc = new WordSyllableCounter("en");
		
		 assertEquals(1, wsc.countSyllables("pEA"));
	}

}
