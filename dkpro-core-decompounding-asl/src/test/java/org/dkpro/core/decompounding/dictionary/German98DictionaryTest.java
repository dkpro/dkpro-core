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

package org.dkpro.core.decompounding.dictionary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.dkpro.core.api.resources.ResourceUtils;
import org.junit.jupiter.api.Test;

public class German98DictionaryTest
{
    @Test
    public void testContains() throws IOException
    {
        final File affixFile = ResourceUtils.getUrlAsFile(
                getClass().getResource(
                        "/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/spelling-de-affix.aff"),
                false);
        final File dictFile = ResourceUtils.getUrlAsFile(getClass().getResource(
                "/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/spelling-de-igerman98.dic"),
                false);
        final German98Dictionary dict = new German98Dictionary(dictFile, affixFile, "UTF-8");
        assertEquals(298506, dict.getAll().size());

        assertTrue(dict.contains("hallo"));
        assertTrue(dict.contains("versuchen"));
        assertTrue(dict.contains("arbeiten"));
        assertTrue(dict.contains("arbeit"));
    }
}
