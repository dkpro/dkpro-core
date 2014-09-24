/*******************************************************************************
 * Copyright 2012
 * Ubiquitous Knowledge Processing (UKP) Lab and FG Language Technology
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.conll;

import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testOneWay;
import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testRoundTrip;

import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class Conll2006ReaderWriterTest
{
    @Test
    public void roundTrip()
        throws Exception
    {
        testRoundTrip(Conll2006Reader.class, Conll2006Writer.class,
                "conll/2006/fk003_2006_08_ZH1.conll");
    }

    @Test
    public void testFinnTreeBank()
        throws Exception
    {
        testOneWay(Conll2006Reader.class, Conll2006Writer.class, "conll/2006/fi-ref.conll",
                "conll/2006/fi-orig.conll");
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
