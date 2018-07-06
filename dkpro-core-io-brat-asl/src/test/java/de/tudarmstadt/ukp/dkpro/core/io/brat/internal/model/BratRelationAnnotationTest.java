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
package de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BratRelationAnnotationTest
{
    @Test
    public void parseTest()
    {
        final String in = "R1\tOrigin Arg1:T3 Arg2:T4";
        BratRelationAnnotation v = BratRelationAnnotation.parse(in);
        assertEquals(in, v.toString());
    }
}
