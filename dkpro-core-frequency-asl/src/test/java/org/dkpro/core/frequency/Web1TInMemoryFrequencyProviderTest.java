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
package org.dkpro.core.frequency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class Web1TInMemoryFrequencyProviderTest
{
    @Test
    public void web1tTestInMemoryTest() throws Exception
    {
        Web1TProviderBase web1t = new Web1TInMemoryProvider("en", "src/test/resources/web1t/", 2);

        assertEquals(2147436244l, web1t.getFrequency("!"));
        assertEquals(528, web1t.getFrequency("Nilmeier"));
        assertEquals(106, web1t.getFrequency("influx takes"));
        assertEquals(69, web1t.getFrequency("frist will"));

        assertEquals(13893397919l, web1t.getNrOfNgrams(1));
        assertEquals(6042, web1t.getNrOfNgrams(2));
        assertEquals(11, web1t.getNrOfDistinctNgrams(1));
        assertEquals(21, web1t.getNrOfDistinctNgrams(2));
    }
}
