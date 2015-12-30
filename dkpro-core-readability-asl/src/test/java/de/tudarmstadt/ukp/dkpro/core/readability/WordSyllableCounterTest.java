/*******************************************************************************
 * Copyright 2013
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
