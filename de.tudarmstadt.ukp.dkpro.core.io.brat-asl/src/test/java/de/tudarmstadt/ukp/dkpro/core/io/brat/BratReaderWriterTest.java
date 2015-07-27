/*******************************************************************************
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.brat;

import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testOneWay;
import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testRoundTrip;

import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2009Reader;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2012Reader;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class BratReaderWriterTest
{
    @Test
    public void testConll2009()
        throws Exception
    {
        testOneWay(Conll2009Reader.class, BratWriter.class, "conll/2009/en-ref.ann",
                "conll/2009/en-orig.conll", BratWriter.PARAM_WRITE_RELATION_ATTRIBUTES, true);
    }

    @Test
    public void testConll2009_2()
        throws Exception
    {
        testRoundTrip(BratReader.class, BratWriter.class, "conll/2009/en-ref.ann",
                BratWriter.PARAM_WRITE_RELATION_ATTRIBUTES, true);
    }

    @Test
    public void testConll2012()
        throws Exception
    {
        testOneWay(Conll2012Reader.class, BratWriter.class, "conll/2012/en-ref.ann",
                "conll/2012/en-orig.conll");
    }

    @Test
    public void testWithShortNames()
        throws Exception
    {
        testRoundTrip(BratReader.class, BratWriter.class, "brat/document0a.ann",
                BratWriter.PARAM_ENABLE_TYPE_MAPPINGS, true);
    }

    @Test
    public void testWithLongNames()
        throws Exception
    {
        testRoundTrip(BratReader.class, BratWriter.class, "brat/document0b.ann",
                BratWriter.PARAM_ENABLE_TYPE_MAPPINGS, false);
    }

    @Test
    public void test1()
        throws Exception
    {
        testOneWay(BratReader.class, BratWriter.class, "brat/document1-ref.ann",
                "brat/document1.ann", BratWriter.PARAM_ENABLE_TYPE_MAPPINGS, true);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
