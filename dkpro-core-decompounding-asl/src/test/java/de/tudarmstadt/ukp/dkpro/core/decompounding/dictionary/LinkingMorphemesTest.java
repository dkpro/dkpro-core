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

package de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.LinkingMorphemes;

public class LinkingMorphemesTest
{

	@Test
	public void testStringConstructor()
	{
		LinkingMorphemes l = new LinkingMorphemes("s", "ens");
		Assert.assertEquals(2, l.getAll().size());
		Assert.assertEquals("s", l.getAll().get(0));
	}

	@Test
	public void testListConstructor()
	{
		List<String> list = new ArrayList<String>();
		list.add("s");
		list.add("ens");

		LinkingMorphemes l = new LinkingMorphemes(list);
		Assert.assertEquals(2, l.getAll().size());
		Assert.assertEquals("s", l.getAll().get(0));
	}

	@Test
	public void testFileConstructor() throws Exception
	{
		LinkingMorphemes l = new LinkingMorphemes(new File(
				"src/test/resources/dic/morphemes.txt"));
		Assert.assertEquals(2, l.getAll().size());
		Assert.assertEquals("s", l.getAll().get(0));
	}
}
