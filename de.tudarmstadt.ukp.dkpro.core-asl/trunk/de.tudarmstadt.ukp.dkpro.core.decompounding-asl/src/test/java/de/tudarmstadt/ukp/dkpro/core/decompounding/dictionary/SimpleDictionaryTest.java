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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;

public class SimpleDictionaryTest
{

	private SimpleDictionary dict;
	
	@Before
	public void setUp() throws IOException{

        final File dictFile =  ResourceUtils.getUrlAsFile(getClass().getResource(
        		"/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/de_DE.dic"), false);
	    dict = new SimpleDictionary(dictFile);
	}
	
	@Test
	public void testContains()
	{
		Assert.assertEquals(71030, dict.getAll().size());

		Assert.assertTrue(dict.contains("worauf"));
		Assert.assertTrue(dict.contains("woraufhin"));
		Assert.assertTrue(dict.contains("woraus"));
	}

	@Test
	public void testDictionary(){
		
	    assertThat(dict.getAll().size(), not(0));
	    assertThat(dict.contains("zu"),is(true));
	}

}
