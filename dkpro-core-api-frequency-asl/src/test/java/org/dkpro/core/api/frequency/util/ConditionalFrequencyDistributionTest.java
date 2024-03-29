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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ConditionalFrequencyDistributionTest
{

    @Test
    public void cfdTest() {
        
        String condition1 = "text1";
        String condition2 = "text2";
        
        List<String> tokens1 = Arrays.asList(
                "This is a first test that contains a first test example".split(" "));
        List<String> tokens2 = Arrays.asList(
                "This second example contains other example tokens".split(" "));
        
        ConditionalFrequencyDistribution<String, String> cfd = 
                new ConditionalFrequencyDistribution<String, String>();
        cfd.incAll(condition1, tokens1);
        cfd.incAll(condition2, tokens2);
        
        System.out.println(cfd);
        
        assertEquals(2, cfd.getConditions().size());
        for (String condition : cfd.getConditions()) {
            assertTrue(condition.equals(condition1) || condition.equals(condition2));
        }
        
        assertEquals(18, cfd.getN());
        
        assertEquals(0, cfd.getCount(condition1, "humpelgrumpf"));
        assertEquals(1, cfd.getCount(condition1, "This"));
        assertEquals(1, cfd.getCount(condition2, "This"));
        assertEquals(2, cfd.getCount(condition1, "test"));
        assertEquals(2, cfd.getCount(condition2, "example"));
    }
    
    @Test
    public void addSampleTest() {
                
        ConditionalFrequencyDistribution<String, String> cfd = 
                new ConditionalFrequencyDistribution<String, String>();
        cfd.addSample("condition", "key", 10);
                
        assertEquals(1, cfd.getConditions().size());
        assertEquals(10, cfd.getN());
        
        assertEquals(10, cfd.getCount("condition", "key"));
    }
}
