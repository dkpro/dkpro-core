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

public class BratTextAnnotationTest
{
    @Test
    public void parseTest()
    {
        final String in = "T1\tOrganization 0 43\tInternational Business Machines Corporation";
        BratTextAnnotation v = BratTextAnnotation.parse(in);
        assertEquals(in, v.toString());
    }

    @Test
    public void parseTestZeroLength()
    {
        final String in = "T1\tOrganization 0 0\t";
        BratTextAnnotation v = BratTextAnnotation.parse(in);
        assertEquals(in, v.toString());
    }
    
    @Test
    public void parseTestDiscontinous1()
    {
        final String in = "T1\tOrganization 0 13;14 43\tInternational Business Machines Corporation";
        final String out = "T1\tOrganization 0 43\tInternational Business Machines Corporation";
        BratTextAnnotation v = BratTextAnnotation.parse(in);
        assertEquals(out, v.toString());
    }
    
    @Test
    public void parseTestDiscontinous2()
    {
        final String in = "T1\tOrganization 0 13;15 43\tInternational Business Machines Corporation";
        final String out = "T1\tOrganization 0 13;15 43\tInternational";
        BratTextAnnotation v = BratTextAnnotation.parse(in);
        System.out.println(v);
        assertEquals(out, v.toString());
    }
}
