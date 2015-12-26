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

//NOTE: This file contains Asciidoc markers for partial inclusion of this file in the documentation
//Do not remove these tags!
public class Conll2006ReaderWriterTest
{
    @Test
    public void roundTrip()
        throws Exception
    {
// tag::testRoundTrip[]
        testRoundTrip(
                Conll2006Reader.class, // the reader
                Conll2006Writer.class,  // the writer
                "conll/2006/fk003_2006_08_ZH1.conll"); // the input also used as output reference
// end::testRoundTrip[]
    }

    @Test
    public void testFinnTreeBank()
        throws Exception
    {
// tag::testOneWay[]
        testOneWay(
                Conll2006Reader.class, // the reader
                Conll2006Writer.class,  // the writer
                "conll/2006/fi-ref.conll", // the reference file for the output
                "conll/2006/fi-orig.conll"); // the input file for the test
// end::testOneWay[]
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
