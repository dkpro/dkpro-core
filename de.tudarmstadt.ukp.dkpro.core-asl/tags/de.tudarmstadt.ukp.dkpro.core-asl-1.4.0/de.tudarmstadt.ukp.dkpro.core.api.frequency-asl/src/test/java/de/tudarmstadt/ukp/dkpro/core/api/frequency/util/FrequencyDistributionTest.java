/*******************************************************************************
 * Copyright 2011
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.api.frequency.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FrequencyDistributionTest
{

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Test
    public void fdTest() {
        
        List<String> tokens = Arrays.asList("This is a first test that contains a first test example".split(" "));
        
        FrequencyDistribution<String> fd = new FrequencyDistribution<String>();
        fd.incAll(tokens);
        
        System.out.println(fd);
        
        assertEquals(11, fd.getN());
        assertEquals(8, fd.getB());
        
        assertEquals(0, fd.getCount("humpelgrumpf"));
        assertEquals(1, fd.getCount("This"));
        assertEquals(2, fd.getCount("test"));
    }

    @Test
    public void saveAndLoadFdTest() throws Exception {
        List<String> tokens = Arrays.asList("This is a first test that contains a first test example".split(" "));
        
        FrequencyDistribution<String> fd = new FrequencyDistribution<String>();
        fd.incAll(tokens);
        
        File outputFile = folder.newFile();
        
        fd.save(outputFile);
        
        FrequencyDistribution<String> loadedFd = new FrequencyDistribution<String>();
        loadedFd.load(outputFile);

        assertEquals(11, loadedFd.getN());
        assertEquals(8, loadedFd.getB());
        
        assertEquals(0, loadedFd.getCount("humpelgrumpf"));
        assertEquals(1, loadedFd.getCount("This"));
        assertEquals(2, loadedFd.getCount("test"));
    }
    
    @Test
    public void fdTest_specialToken() {
        
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