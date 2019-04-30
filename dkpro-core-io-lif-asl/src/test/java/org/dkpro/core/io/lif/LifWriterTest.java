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
package org.dkpro.core.io.lif;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.dkpro.core.testing.IOTestRunner.testOneWay;

import org.dkpro.core.io.conll.Conll2006Reader;
import org.dkpro.core.io.lif.LifWriter;
import org.dkpro.core.testing.DkproTestContext;
import org.junit.Rule;
import org.junit.Test;

public class LifWriterTest
{
    @Test
    public void oneWay()
        throws Exception
    {
        testOneWay(
                createReaderDescription(Conll2006Reader.class), // the reader
                createEngineDescription(LifWriter.class,  // the writer
                        LifWriter.PARAM_WRITE_TIMESTAMP, false),  
                "conll/2006/fi-ref.lif", // the reference file for the output
                "conll/2006/fi-orig.conll"); // the input file for the test
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
