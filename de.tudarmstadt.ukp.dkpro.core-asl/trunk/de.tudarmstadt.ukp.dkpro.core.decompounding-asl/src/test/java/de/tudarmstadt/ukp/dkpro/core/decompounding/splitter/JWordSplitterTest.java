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
import java.net.URL;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.Dictionary;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.SimpleDictionary;

public class JWordSplitterTest
{

    @Test
    public void testSplitter()
        throws IOException
    {
        JWordSplitterAlgorithm splitter = new JWordSplitterAlgorithm();
        List<DecompoundedWord> result = splitter.split("Aktionsplan").getAllSplits();
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("Aktionsplan", result.get(0).toString());
        Assert.assertEquals("Aktion(s)+plan", result.get(1).toString());
    }

    @Test
	public void testSplliter2() throws IOException {
	    JWordSplitterAlgorithm splitter = new JWordSplitterAlgorithm();
        final File dictFile =  ResourceUtils.getUrlAsFile(getClass().getResource(
        		"/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/de_DE.dic"), false);;

	    Dictionary dict = new SimpleDictionary(dictFile);
	    splitter.setDictionary(dict);
	    List<DecompoundedWord> result = splitter.split("geräteelektronik").getAllSplits();
	    assertThat(result.size(),is(1));
	}
}
