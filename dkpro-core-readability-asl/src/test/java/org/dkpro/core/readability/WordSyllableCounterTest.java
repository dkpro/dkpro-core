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
package org.dkpro.core.readability;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.dkpro.core.readability.measure.WordSyllableCounter;
import org.junit.jupiter.api.Test;

public class WordSyllableCounterTest
{
    @Test
    public void countWordSyllTest_vowelPairs() throws Exception
    {
        WordSyllableCounter wsc = new WordSyllableCounter("en");

        assertEquals(4, wsc.countSyllables("analysis"));
        assertEquals(2, wsc.countSyllables("teacher"));

        // TODO
        /*
         * According to Linux 'style' algorithm, the syllables number of "readability" is 4. But in
         * fact it should be 5. This means Linux'Style' algorithm is not always precise.
         */
        assertEquals(4, wsc.countSyllables("readability"));
    }

    @Test
    public void countWordSyllTest_case() throws Exception
    {
        WordSyllableCounter wsc = new WordSyllableCounter("en");

        assertEquals(1, wsc.countSyllables("pEA"));
    }
}
