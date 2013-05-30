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

import org.junit.Ignore;
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

	@Ignore
	@Test
	public void testSimpleDict() throws IOException
	{
        final File affixFile = ResourceUtils.getUrlAsFile(getClass().getResource(
        		"/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/de_DE.aff"), false);
        final File dictFile =  ResourceUtils.getUrlAsFile(getClass().getResource(
        		"/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/de_DE.dic"), false);
		final German98Dictionary dict = new German98Dictionary(dictFile, affixFile);
		TrieStructure t = TrieStructure.createForDict(dict);

		Assert.assertEquals(new Integer(14308), t.findWord("h").getValue());
		Assert.assertEquals(new Integer(251), t.findWord("hel").getValue());
		Assert.assertEquals(new Integer(0), t.findWord("hallo").getValue());

		Assert.assertEquals(new Integer(7655), t.findWord("t").getValue());
		Assert.assertEquals(new Integer(2596), t.findWord("tr").getValue());
		Assert.assertEquals(new Integer(927), t.findWord("tra").getValue());
		Assert.assertEquals(new Integer(23), t.findWord("tram").getValue());

		Assert.assertEquals(new Integer(10844), t.findWord("w").getValue());
		Assert.assertEquals(new Integer(181), t.findWord("wor").getValue());
		Assert.assertEquals(new Integer(163), t.findWord("wort").getValue());

	}

	@Ignore
	@Test
	public void testSimpleDictReverse() throws IOException
	{
        final File affixFile = ResourceUtils.getUrlAsFile(getClass().getResource(
        		"/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/de_DE.aff"), false);
        final File dictFile =  ResourceUtils.getUrlAsFile(getClass().getResource(
        		"/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/de_DE.dic"), false);
		final German98Dictionary dict = new German98Dictionary(dictFile, affixFile);
		TrieStructure t = TrieStructure.createForDict(dict);
		Assert.assertEquals(new Integer(10490), t.findWord("d").getValue());
		Assert.assertEquals(new Integer(2351), t.findWord("de").getValue());
		Assert.assertEquals(new Integer(68), t.findWord("dei").getValue());

		Assert.assertEquals(new Integer(13124), t.findWord("k").getValue());
		Assert.assertEquals(new Integer(1944), t.findWord("o").getValue());
	}
}
