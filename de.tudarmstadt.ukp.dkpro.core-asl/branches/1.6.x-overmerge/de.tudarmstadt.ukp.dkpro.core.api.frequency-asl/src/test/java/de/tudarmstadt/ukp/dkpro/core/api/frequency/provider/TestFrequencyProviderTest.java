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
package de.tudarmstadt.ukp.dkpro.core.api.frequency.provider;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestFrequencyProviderTest
{

    @Test
    public void testTestFrequencyCountProvider() throws Exception
    {
        FrequencyCountProviderBase provider = new TestFrequencyCountProvider();
        assertEquals(10, provider.getFrequency("a"));
        assertEquals(100, provider.getFrequency("aa"));
        assertEquals(1000, provider.getFrequency("aaa"));
        assertEquals(1000, provider.getFrequency("a a"));

        provider.setScaleDownFactor(10);
        assertEquals(1, provider.getFrequency("a"));
        assertEquals(10, provider.getFrequency("aa"));
        assertEquals(100, provider.getFrequency("aaa"));
        assertEquals(100, provider.getFrequency("a a"));
    
        provider.setScaleDownFactor(20);
        assertEquals(0, provider.getFrequency("a"));
        assertEquals(5, provider.getFrequency("aa"));
        assertEquals(50, provider.getFrequency("aaa"));
        assertEquals(50, provider.getFrequency("a a"));
    }
}
