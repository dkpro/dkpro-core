/*
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
