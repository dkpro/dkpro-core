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
package org.dkpro.core.io.brat.internal.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class BratAttributeTest
{
    @Test
    public void parseBinaryAttributeTest()
    {
        final String in = "A1\tNegation E1";
        BratAttribute v = BratAttribute.parse(in);
        assertEquals(in, v.toString());
    }

    @Test
    public void parseSingleValueAttributeTest()
    {
        final String in = "A2\tConfidence E2 L1";
        BratAttribute v = BratAttribute.parse(in);
        assertEquals(in, v.toString());
    }

    @Test
    public void parseMultiValueAttributeTest()
    {
        final String in = "A2\tConfidence E2 L1 L2";
        BratAttribute v = BratAttribute.parse(in);
        assertEquals(in, v.toString());
    }
}
