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

import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testRoundTrip;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class Conll2000ReaderWriterTest
{
    @Test
    public void roundTrip()
        throws Exception
    {
        testRoundTrip(Conll2000Reader.class, Conll2000Writer.class,
                "conll/2000/chunk2000_test.conll");
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
