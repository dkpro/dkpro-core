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
import org.dkpro.core.decompounding.dictionary.LinkingMorphemes;
import org.dkpro.core.decompounding.dictionary.SimpleDictionary;
import org.junit.jupiter.api.Test;

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
        assertEquals(6, result.size());
        assertEquals("aktionsplan", result.get(0).toString());
        assertEquals("akt+ionsplan", result.get(1).toString());
        assertEquals("akt+ion+splan", result.get(2).toString());
        assertEquals("akt+ion(s)+plan", result.get(3).toString());
        assertEquals("aktion+splan", result.get(4).toString());
        assertEquals("aktion(s)+plan", result.get(5).toString());
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
        assertEquals(6, result.size());
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
        assertEquals(4, result.size());
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
        assertEquals(3, result.size());
        assertEquals("alarmreaktionen", result.get(0).toString());
        assertEquals("alarm+reaktionen", result.get(1).toString());
        assertEquals("alarm+reaktion(en)", result.get(2).toString());
    }

    @Test
    public void testSplit4() throws IOException
    {
        final File dictFile = ResourceUtils.getUrlAsFile(getClass().getResource(
                "/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/spelling-de-igerman98.dic"),
                false);
        final File morphemesFile = ResourceUtils.getUrlAsFile(getClass().getResource(
                "/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/spelling-de-linking.linking"),
                false);

        Dictionary dict = new SimpleDictionary(dictFile, "UTF-8");
        LinkingMorphemes morphemes = new LinkingMorphemes(morphemesFile);

        LeftToRightSplitterAlgorithm splitter = new LeftToRightSplitterAlgorithm(dict,morphemes);

        List<DecompoundedWord> result = splitter.split("geräteelektronik").getAllSplits();

        assertThat(result).hasSize(1);
    }
}
