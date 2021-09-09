/*
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
 */
package org.dkpro.core.io.conll;

import static org.dkpro.core.testing.IOTestRunner.testOneWay;

import java.io.IOException;

import org.dkpro.core.testing.DkproTestContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class Conll2008ReaderWriterTest
{
    @Before
    public void setUp() throws IOException {
        DkproTestContext.get().initializeTestWorkspace();
    }
    
    @Test
    public void test()
        throws Exception
    {
        testOneWay(Conll2008Reader.class, Conll2008Writer.class, "conll/2008/en-ref.conll",
                "conll/2008/en-orig.conll");
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
