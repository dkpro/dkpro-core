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
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.German98Dictionary;

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
	public void testSimpleDict() throws IOException
	{
        final File affixFile = ResourceUtils.getUrlAsFile(getClass().getResource(
        		"/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/spelling-de-affix.aff"), false);
        final File dictFile =  ResourceUtils.getUrlAsFile(getClass().getResource(
        		"/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/spelling-de-igerman98.dic"), false);
		final German98Dictionary dict = new German98Dictionary(dictFile, affixFile);
		TrieStructure t = TrieStructure.createForDict(dict);

		Assert.assertEquals(new Integer(14963), t.findWord("h").getValue());
		Assert.assertEquals(new Integer(257), t.findWord("hel").getValue());
		Assert.assertEquals(new Integer(0), t.findWord("hallo").getValue());

		Assert.assertEquals(new Integer(8033), t.findWord("t").getValue());
		Assert.assertEquals(new Integer(2714), t.findWord("tr").getValue());
		Assert.assertEquals(new Integer(996), t.findWord("tra").getValue());
		Assert.assertEquals(new Integer(38), t.findWord("tram").getValue());

		Assert.assertEquals(new Integer(11138), t.findWord("w").getValue());
		Assert.assertEquals(new Integer(178), t.findWord("wor").getValue());
		Assert.assertEquals(new Integer(160), t.findWord("wort").getValue());

	}

	@Test
	public void testSimpleDictReverse() throws IOException
	{
        final File affixFile = ResourceUtils.getUrlAsFile(getClass().getResource(
        		"/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/spelling-de-affix.aff"), false);
        final File dictFile =  ResourceUtils.getUrlAsFile(getClass().getResource(
        		"/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/spelling-de-igerman98.dic"), false);
		final German98Dictionary dict = new German98Dictionary(dictFile, affixFile);
		TrieStructure t = TrieStructure.createForDict(dict);
		Assert.assertEquals(new Integer(11121), t.findWord("d").getValue());
		Assert.assertEquals(new Integer(2494), t.findWord("de").getValue());
		Assert.assertEquals(new Integer(69), t.findWord("dei").getValue());

		Assert.assertEquals(new Integer(13809), t.findWord("k").getValue());
		Assert.assertEquals(new Integer(2101), t.findWord("o").getValue());
	}
}
