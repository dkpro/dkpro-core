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
package org.dkpro.core.io.tei;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.dkpro.core.testing.IOTestRunner.testRoundTrip;

import org.dkpro.core.io.tei.TeiReader;
import org.dkpro.core.io.tei.TeiWriter;
import org.dkpro.core.testing.DkproTestContext;
import org.junit.Rule;
import org.junit.Test;

public class TeiReaderWriterTest
{
    @Test
    public void test()
        throws Exception
    {
        testRoundTrip(TeiReader.class, TeiWriter.class, "reference/example1.xml");
    }

    @Test
    public void test2()
        throws Exception
    {
        testRoundTrip(
                createReaderDescription(TeiReader.class), 
                createEngineDescription(TeiWriter.class,
                        TeiWriter.PARAM_WRITE_CONSTITUENT, true), 
                "reference/example2.xml");
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
