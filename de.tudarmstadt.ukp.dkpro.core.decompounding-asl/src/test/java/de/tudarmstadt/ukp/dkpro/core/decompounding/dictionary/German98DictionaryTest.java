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
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;

public class German98DictionaryTest
{

	@Test
	public void testContains() throws IOException
	{
		
        final File affixFile = ResourceUtils.getUrlAsFile(getClass().getResource(
        		"/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/de_DE.aff"), false);
        final File dictFile =  ResourceUtils.getUrlAsFile(getClass().getResource(
        		"/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/de_DE.dic"), false);
		final German98Dictionary dict = new German98Dictionary(dictFile, affixFile);
		Assert.assertEquals(286322, dict.getAll().size());

		Assert.assertTrue(dict.contains("hallo"));
		Assert.assertTrue(dict.contains("versuchen"));
		Assert.assertTrue(dict.contains("arbeiten"));
		Assert.assertTrue(dict.contains("arbeit"));
	}
}
