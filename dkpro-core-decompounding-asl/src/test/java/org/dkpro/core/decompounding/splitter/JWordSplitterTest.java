/*
 * Copyright 2017
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
 **/
package org.dkpro.core.decompounding.splitter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.dkpro.core.api.resources.ResourceUtils;
import org.dkpro.core.decompounding.dictionary.Dictionary;
import org.dkpro.core.decompounding.dictionary.SimpleDictionary;
import org.junit.jupiter.api.Test;

public class JWordSplitterTest
{
    @Test
    public void testSplitter()
        throws IOException
    {
        JWordSplitterAlgorithm splitter = new JWordSplitterAlgorithm();
        List<DecompoundedWord> result = splitter.split("Aktionsplan").getAllSplits();
        
        assertEquals(2, result.size());
        assertEquals("Aktionsplan", result.get(0).toString());
        assertEquals("Aktion(s)+plan", result.get(1).toString());
    }

    @Test
    public void testSplliter2() throws IOException {
        JWordSplitterAlgorithm splitter = new JWordSplitterAlgorithm();
        final File dictFile = ResourceUtils.getUrlAsFile(getClass().getResource(
                "/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/spelling-de-igerman98.dic"),
                false);

        Dictionary dict = new SimpleDictionary(dictFile, "UTF-8");
        splitter.setDictionary(dict);
        List<DecompoundedWord> result = splitter.split("geräteelektronik").getAllSplits();
        
        assertThat(result).hasSize(1);
    }

    @Test
    public void testSplliter3() throws IOException {
        JWordSplitterAlgorithm splitter = new JWordSplitterAlgorithm();
        final File dictFile = ResourceUtils.getUrlAsFile(getClass().getResource(
                "/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/spelling-de-igerman98.dic"),
                false);

        Dictionary dict = new SimpleDictionary(dictFile, "UTF-8");
        splitter.setDictionary(dict);
        List<DecompoundedWord> result = splitter.split("Schwerwiegend").getAllSplits();
        
        assertThat(result).hasSize(1);
    }
}
