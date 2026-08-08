/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dkpro.core.decompounding.dictionary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.dkpro.core.api.resources.ResourceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SimpleDictionaryTest
{
    private SimpleDictionary dict;

    @BeforeEach
    public void setUp() throws IOException
    {
        final File dictFile = ResourceUtils.getUrlAsFile(getClass().getResource(
                "/de/tudarmstadt/ukp/dkpro/core/decompounding/lib/spelling-de-igerman98.dic"),
                false);
        dict = new SimpleDictionary(dictFile, "UTF-8");
    }

    @Test
    public void testContains()
    {
        assertEquals(72508, dict.getAll().size());

        assertTrue(dict.contains("worauf"));
        assertTrue(dict.contains("woraufhin"));
        assertTrue(dict.contains("woraus"));
    }

    @Test
    public void testDictionary()
    {
        assertThat(dict.getAll()).isNotEmpty();
        assertThat(dict.contains("zu")).isTrue();
    }
}
