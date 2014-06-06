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

package de.tudarmstadt.ukp.dkpro.core.decompounding.splitter;

import org.junit.Assert;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.Fragment;

public class FragmentTest
{

	@Test
	public void testCreate()
	{
		Fragment e = Fragment.createFromString("aktion(s)");

		Assert.assertEquals("aktion", e.getWord());
		Assert.assertEquals("s", e.getMorpheme());

		e = Fragment.createFromString("plan");
		Assert.assertEquals("plan", e.getWord());
		Assert.assertEquals(null, e.getMorpheme());
	}

	@Test
	public void testToString()
	{
		Fragment e = new Fragment();
		e.setWord("aktion");
		e.setMorpheme("s");
		Assert.assertEquals("aktion(s)", e.toString());

		e.setMorpheme(null);
		Assert.assertEquals("aktion", e.toString());
	}

	@Test
	public void testEquals()
	{
		Fragment e1 = new Fragment();
		e1.setWord("aktion");
		e1.setMorpheme("s");

		Fragment e2 = new Fragment();
		e2.setWord("aktion");
		e2.setMorpheme("s");

		Assert.assertTrue(e1.equals(e1));

		e2.setMorpheme(null);
		Assert.assertFalse(e1.equals(e2));
	}

	   @Test
	    public void testCreateFromString()
	    {
	        Fragment fragm = Fragment.createFromString("(");
	        Assert.assertThat(fragm.getWord(),CoreMatchers.is("("));
	    }

}
