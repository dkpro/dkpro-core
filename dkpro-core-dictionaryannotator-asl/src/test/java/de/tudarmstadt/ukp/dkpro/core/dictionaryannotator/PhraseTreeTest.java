/*******************************************************************************
 * Copyright 2010
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
package de.tudarmstadt.ukp.dkpro.core.dictionaryannotator;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class PhraseTreeTest
{
	private PhraseTree phrases;
	
	@Before
	public void setUp()
		throws Exception
	{
		phrases = new PhraseTree();
		
		phrases.addPhrase("the red dog".split(" "));
		phrases.addPhrase("the red".split(" "));
		phrases.addPhrase("the new kid".split(" "));
		phrases.addPhrase("a".split(" "));
	}
	
	@Test
	public void containsTest()
		throws Exception
	{
		assertFalse(phrases.contains("the".split(" ")));
		assertFalse(phrases.contains("the new".split(" ")));
		assertFalse(phrases.contains("the new BUNNY".split(" ")));
		assertFalse(phrases.contains("the red dog barks".split(" ")));
		assertTrue(phrases.contains("a".split(" ")));
		assertTrue(phrases.contains("the red dog".split(" ")));
		assertTrue(phrases.contains("the red".split(" ")));
		assertTrue(phrases.contains("the new kid".split(" ")));
	}
	
	@Test
	public void matchTest()
		throws Exception
	{
		String[] sentence = "the red dog whines".split(" ");
		String[] longestMatch = phrases.getLongestMatch(sentence);
		
		assertArrayEquals(longestMatch, "the red dog".split(" "));
		
		sentence = "the".split(" ");
		assertNull(phrases.getLongestMatch(sentence));
		
		sentence = "red dog".split(" ");
		assertNull(phrases.getLongestMatch(sentence));
		
		sentence = "the new".split(" ");
		assertNull(phrases.getLongestMatch(sentence));
	}
}
