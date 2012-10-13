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
package de.tudarmstadt.ukp.dkpro.core.frequency;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class Web1TInMemoryFrequencyProviderTest
{

    @Test
    public void web1tTestInMemoryTest() throws Exception
    {
        Web1TProviderBase web1t = new Web1TInMemoryProvider(
                "src/test/resources/web1t/",
                2
        );

        assertEquals(2147436244l, web1t.getFrequency("!"));
        assertEquals(528,         web1t.getFrequency("Nilmeier"));
        assertEquals(106,         web1t.getFrequency("influx takes"));
        assertEquals(69,          web1t.getFrequency("frist will"));
        
        assertEquals(13893397919l, web1t.getNrOfNgrams(1));
        assertEquals(6042, web1t.getNrOfNgrams(2));
        assertEquals(11, web1t.getNrOfDistinctNgrams(1));
        assertEquals(21, web1t.getNrOfDistinctNgrams(2));
    }
}
