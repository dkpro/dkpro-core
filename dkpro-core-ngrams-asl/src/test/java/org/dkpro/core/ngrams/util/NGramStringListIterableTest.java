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
package org.dkpro.core.ngrams.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

public class NGramStringListIterableTest
{
    @Test
    public void ngramTest()
    {
        String[] tokens = "This is a simple example sentence .".split(" ");

        int i = 0;
        for (List<String> ngram : new NGramStringListIterable(tokens, 2, 2)) {
            if (i == 0) {
                assertEquals(2, ngram.size());
                assertEquals("This is", StringUtils.join(ngram, " "));
            }

            System.out.println(ngram);
            i++;
        }
        assertEquals(6, i);
    }
}
