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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.Dictionary;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.LinkingMorphemes;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.SimpleDictionary;

public class LeftToRightSplitAlgorithmTest
{

	@Test
	public void testSplit1()
	{
		Dictionary dict = new SimpleDictionary("Akt", "ion", "plan", "Aktion",
				"Aktionsplan");
		LinkingMorphemes morphemes = new LinkingMorphemes("s");
		LeftToRightSplitterAlgorithm algo = new LeftToRightSplitterAlgorithm(dict,
				morphemes);

		List<DecompoundedWord> result = algo.split("Aktionsplan").getAllSplits();
		Assert.assertEquals(6, result.size());
		Assert.assertEquals("aktionsplan", result.get(0).toString());
		Assert.assertEquals("akt+ionsplan", result.get(1).toString());
		Assert.assertEquals("akt+ion+splan", result.get(2).toString());
		Assert.assertEquals("akt+ion(s)+plan", result.get(3).toString());
		Assert.assertEquals("aktion+splan", result.get(4).toString());
		Assert.assertEquals("aktion(s)+plan", result.get(5).toString());
	}

	@Test
	public void testSplit2()
	{
		Dictionary dict = new SimpleDictionary("Donau", "dampf", "schiff",
				"fahrt", "dampfschiff", "schifffahrt");
		LinkingMorphemes morphemes = new LinkingMorphemes("s");
		LeftToRightSplitterAlgorithm algo = new LeftToRightSplitterAlgorithm(dict,
				morphemes);

		List<DecompoundedWord> result = algo.split("Donaudampfschifffahrt").getAllSplits();
		Assert.assertEquals(6, result.size());
	}

	@Test
	public void testSplit3()
	{
		Dictionary dict = new SimpleDictionary("Super", "mann", "anzug",
				"Supermann", "anzug");
		LinkingMorphemes morphemes = new LinkingMorphemes("s");
		LeftToRightSplitterAlgorithm algo = new LeftToRightSplitterAlgorithm(dict,
				morphemes);

		List<DecompoundedWord> result = algo.split("Supermannanzug").getAllSplits();
		// Super+mann+anzug, Supermann+anzug
		Assert.assertEquals(4, result.size());
	}

	@Test
	public void testMorphemes1()
	{
		Dictionary dict = new SimpleDictionary("alarm", "reaktion");
		LinkingMorphemes morphemes = new LinkingMorphemes("en");
		LeftToRightSplitterAlgorithm algo = new LeftToRightSplitterAlgorithm(dict,
				morphemes);

		List<DecompoundedWord> result = algo.split("alarmreaktionen").getAllSplits();
		// Super+mann+anzug, Supermann+anzug
		Assert.assertEquals(3, result.size());
		Assert.assertEquals("alarmreaktionen", result.get(0).toString());
		Assert.assertEquals("alarm+reaktionen", result.get(1).toString());
		Assert.assertEquals("alarm+reaktion(en)", result.get(2).toString());
	}

	@Test
	public void testSplit4() throws IOException{

        final File dictFile =  ResourceUtils.getUrlAsFile(getClass().getResource(
        		"/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/spelling/de/igerman98/de_DE_igerman98.dic"), false);
        final File morphemesFile =  ResourceUtils.getUrlAsFile(getClass().getResource(
        		"/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/spelling/de/igerman98/de_DE.linking"), false);

	    Dictionary dict = new SimpleDictionary(dictFile);
	    LinkingMorphemes morphemes = new LinkingMorphemes(morphemesFile);

	    LeftToRightSplitterAlgorithm splitter = new LeftToRightSplitterAlgorithm(dict,morphemes);

	    List<DecompoundedWord> result = splitter.split("geräteelektronik").getAllSplits();

	    assertThat(result.size(),is(1));

	}

}
