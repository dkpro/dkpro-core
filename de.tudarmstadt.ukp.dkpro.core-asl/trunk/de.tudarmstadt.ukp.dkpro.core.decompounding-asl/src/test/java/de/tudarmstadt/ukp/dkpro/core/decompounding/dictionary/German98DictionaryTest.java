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

import junit.framework.Assert;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.German98Dictionary;

public class German98DictionaryTest
{

	@Test
	public void testContains()
	{
		German98Dictionary dict = new German98Dictionary(new File(
				"src/test/resources/dic/igerman98.dic"), new File(
				"src/test/resources/dic/igerman98.aff"));
		Assert.assertEquals(6, dict.getAll().size());

		Assert.assertTrue(dict.contains("hello"));
		Assert.assertTrue(dict.contains("try"));
		Assert.assertTrue(dict.contains("tried"));
		Assert.assertTrue(dict.contains("work"));
		Assert.assertTrue(dict.contains("worked"));
		Assert.assertTrue(dict.contains("rework"));
	}
}
