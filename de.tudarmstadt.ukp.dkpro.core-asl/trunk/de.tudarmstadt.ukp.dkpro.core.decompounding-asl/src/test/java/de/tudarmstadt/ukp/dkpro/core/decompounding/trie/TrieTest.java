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
 *******************************************************************************/

package de.tudarmstadt.ukp.dkpro.core.decompounding.trie;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.German98Dictionary;
import de.tudarmstadt.ukp.dkpro.core.decompounding.trie.TrieStructure;

public class TrieTest
{

	@Test
	public void testAddSorted()
	{
		TrieStructure t = new TrieStructure();

		t.addWord("abc");
		Assert.assertEquals(new Integer(1), t.findWord("a").getValue());
		Assert.assertEquals(new Integer(1), t.findWord("ab").getValue());
		Assert.assertEquals(new Integer(0), t.findWord("abc").getValue());

		t.addWord("abcde");
		Assert.assertEquals(new Integer(2), t.findWord("a").getValue());
		Assert.assertEquals(new Integer(2), t.findWord("ab").getValue());
		Assert.assertEquals(new Integer(1), t.findWord("abc").getValue());
		Assert.assertEquals(new Integer(1), t.findWord("abcd").getValue());
		Assert.assertEquals(new Integer(0), t.findWord("abcde").getValue());

		t.addWord("abde");
		Assert.assertEquals(new Integer(3), t.findWord("a").getValue());
		Assert.assertEquals(new Integer(3), t.findWord("ab").getValue());
		Assert.assertEquals(new Integer(1), t.findWord("abd").getValue());
		Assert.assertEquals(new Integer(0), t.findWord("abde").getValue());
	}

	@Test
	public void testAddUnsorted()
	{
		TrieStructure t = new TrieStructure();

		t.addWord("abde");
		Assert.assertEquals(new Integer(1), t.findWord("a").getValue());
		Assert.assertEquals(new Integer(1), t.findWord("ab").getValue());
		Assert.assertEquals(new Integer(1), t.findWord("abd").getValue());
		Assert.assertEquals(new Integer(0), t.findWord("abde").getValue());

		t.addWord("abc");
		Assert.assertEquals(new Integer(2), t.findWord("a").getValue());
		Assert.assertEquals(new Integer(2), t.findWord("ab").getValue());
		Assert.assertEquals(new Integer(0), t.findWord("abc").getValue());

		t.addWord("abcde");
		Assert.assertEquals(new Integer(3), t.findWord("a").getValue());
		Assert.assertEquals(new Integer(3), t.findWord("ab").getValue());
		Assert.assertEquals(new Integer(1), t.findWord("abc").getValue());
		Assert.assertEquals(new Integer(1), t.findWord("abcd").getValue());
		Assert.assertEquals(new Integer(0), t.findWord("abcde").getValue());
	}

	@Test
	public void testSimpleDict()
	{
		German98Dictionary dict = new German98Dictionary(new File(
				"src/test/resources/dic/igerman98.dic"), new File(
				"src/test/resources/dic/igerman98.aff"));
		TrieStructure t = TrieStructure.createForDict(dict);

		Assert.assertEquals(new Integer(1), t.findWord("h").getValue());
		Assert.assertEquals(new Integer(1), t.findWord("hel").getValue());
		Assert.assertEquals(new Integer(0), t.findWord("hello").getValue());

		Assert.assertEquals(new Integer(2), t.findWord("t").getValue());
		Assert.assertEquals(new Integer(2), t.findWord("tr").getValue());
		Assert.assertEquals(new Integer(0), t.findWord("try").getValue());
		Assert.assertEquals(new Integer(1), t.findWord("tri").getValue());
		Assert.assertEquals(new Integer(0), t.findWord("tried").getValue());

		Assert.assertEquals(new Integer(2), t.findWord("w").getValue());
		Assert.assertEquals(new Integer(2), t.findWord("wor").getValue());
		Assert.assertEquals(new Integer(1), t.findWord("work").getValue());
		Assert.assertEquals(new Integer(1), t.findWord("worke").getValue());
		Assert.assertEquals(new Integer(0), t.findWord("worked").getValue());
	}

	@Test
	public void testSimpleDictReverse()
	{
		German98Dictionary dict = new German98Dictionary(new File(
				"src/test/resources/dic/igerman98.dic"), new File(
				"src/test/resources/dic/igerman98.aff"));
		TrieStructure t = TrieStructure.createForDictReverse(dict);

		Assert.assertEquals(new Integer(2), t.findWord("d").getValue());
		Assert.assertEquals(new Integer(2), t.findWord("de").getValue());
		Assert.assertEquals(new Integer(1), t.findWord("dei").getValue());
		Assert.assertEquals(new Integer(0), t.findWord("deirt").getValue());

		Assert.assertEquals(new Integer(2), t.findWord("k").getValue());
		Assert.assertEquals(new Integer(1), t.findWord("o").getValue());
	}
}
