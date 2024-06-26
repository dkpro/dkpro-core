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
 */
package org.dkpro.core.api.frequency.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FrequencyDistributionTest
{
    @Test
    public void fdTest()
    {
        List<String> tokens = Arrays
                .asList("This is a first test that contains a first test example".split(" "));

        FrequencyDistribution<String> fd = new FrequencyDistribution<String>();
        fd.incAll(tokens);

        System.out.println(fd);

        assertEquals(11, fd.getN());
        assertEquals(8, fd.getB());

        assertEquals(0, fd.getCount("humpelgrumpf"));
        assertEquals(1, fd.getCount("This"));
        assertEquals(2, fd.getCount("test"));

        assertEquals(2, fd.getMaxFreq());
        assertEquals("a", fd.getSampleWithMaxFreq());

        List<String> top3 = fd.getMostFrequentSamples(3);
        assertEquals(3, top3.size());
        assertTrue(top3.contains("first"));
        assertTrue(top3.contains("a"));
        assertTrue(top3.contains("test"));
    }

    /**
     * Bug in PCJ: see http://sourceforge.net/p/pcj/bugs/15/
     */
    @Test
    public void testMaxIntHash()
    {
        String badKey = "'s_'s_not_noticed";

        assertEquals(Integer.MIN_VALUE, badKey.hashCode());
        FrequencyDistribution<String> fd = new FrequencyDistribution<String>();
        fd.inc(badKey);
    }
    
    @Test
    public void clearTest() {
        List<String> tokens = Arrays
                .asList("This is a first test that contains a first test example".split(" "));

        FrequencyDistribution<String> fd = new FrequencyDistribution<String>();
        fd.incAll(tokens);
        fd.clear();
        assertEquals(0L,  fd.getMaxFreq());
        assertNull(fd.getSampleWithMaxFreq());
    }

    @Test
    public void saveAndLoadFdTest(@TempDir File tempDir)
        throws Exception
    {
        List<String> tokens = Arrays
                .asList("This is a first test that contains a first test example".split(" "));

        FrequencyDistribution<String> fd = new FrequencyDistribution<String>();
        fd.incAll(tokens);
        
        File outputFile = new File(tempDir, "test");

        fd.save(outputFile);

        FrequencyDistribution<String> loadedFd = new FrequencyDistribution<String>();
        loadedFd.load(outputFile);
        
        assertEquals(11, loadedFd.getN());
        assertEquals(8, loadedFd.getB());

        assertEquals(0, loadedFd.getCount("humpelgrumpf"));
        assertEquals(1, loadedFd.getCount("This"));
        assertEquals(2, loadedFd.getCount("test"));
        
        assertEquals("a", loadedFd.getSampleWithMaxFreq());
    }

    @Test
    public void fdTest_specialToken()
    {
        FrequencyDistribution<String> fd = new FrequencyDistribution<String>();
        fd.inc(", ");
        fd.inc(". ");
        fd.inc(".");
        fd.inc(",");
        fd.inc("\t");
        fd.inc(",\t");

        System.out.println(fd);

        assertEquals(6, fd.getN());
        assertEquals(6, fd.getB());

        assertEquals(0, fd.getCount("humpelgrumpf"));
        assertEquals(1, fd.getCount(", "));
        assertEquals(1, fd.getCount(","));
        assertEquals(1, fd.getCount(". "));
        assertEquals(1, fd.getCount("."));
        assertEquals(1, fd.getCount("\t"));
        assertEquals(1, fd.getCount(",\t"));
    }
}
