/*******************************************************************************
 * Copyright 2016
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
package de.tudarmstadt.ukp.dkpro.core.io.lif;

import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testOneWay;
import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testRoundTrip;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class LifReaderWriterTest
{
    @Test
    public void roundTrip()
        throws Exception
    {
        testRoundTrip(
                LifReader.class, // the reader
                LifWriter.class,  // the writer
                "conll/2006/fi-ref.json"); // the input also used as output reference
    }
    
    @Ignore("Doesn't work")
    @Test
    public void oneWay()
        throws Exception
    {
        testOneWay(
                LifReader.class, // the reader
                LifWriter.class,  // the writer
                "lif/dependencystructure-ref.json", // the reference file for the output
                "lif/dependencystructure.json"); // the input file for the test
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
