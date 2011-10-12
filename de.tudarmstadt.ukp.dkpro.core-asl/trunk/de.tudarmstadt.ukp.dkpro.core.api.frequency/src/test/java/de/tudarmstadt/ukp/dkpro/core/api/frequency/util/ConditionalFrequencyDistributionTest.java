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
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ConditionalFrequencyDistributionTest
{

    @Test
    public void cfdTest() {
        
        String condition1 = "text1";
        String condition2 = "text2";
        
        List<String> tokens1 = Arrays.asList("This is a first test that contains a first test example".split(" "));
        List<String> tokens2 = Arrays.asList("This second example contains other example tokens".split(" "));
        
        ConditionalFrequencyDistribution<String, String> cfd = new ConditionalFrequencyDistribution<String, String>();
        cfd.addSamples(condition1, tokens1);
        cfd.addSamples(condition2, tokens2);
        
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
}
