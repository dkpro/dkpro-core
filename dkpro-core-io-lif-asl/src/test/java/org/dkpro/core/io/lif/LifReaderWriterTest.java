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
import static org.dkpro.core.testing.IOTestRunner.testRoundTrip;

import org.dkpro.core.testing.DkproTestContext;
import org.junit.Rule;
import org.junit.Test;

public class LifReaderWriterTest
{
    @Test
    public void roundTrip()
        throws Exception
    {
        testRoundTrip(
                createReaderDescription(LifReader.class), // the reader
                createEngineDescription(LifWriter.class,  // the writer
                        LifWriter.PARAM_WRITE_TIMESTAMP, false),  
                "conll/2006/fi-ref.lif"); // the input also used as output reference
    }
    
    @Test
    public void authenticPosLifFileWithWrapper()
        throws Exception
    {
        testOneWay(
                createReaderDescription(LifReader.class), // the reader
                createEngineDescription(LifWriter.class,  // the writer
                        LifWriter.PARAM_WRITE_TIMESTAMP, false,
                        LifWriter.PARAM_ADD_ENVELOPE, true),  
                "lif/stanford-pos-ref.lif", // the reference file for the output
                "lif/stanford-pos.lif"); // the input file for the test
    }
    
    @Test
    public void oneDependencyStructure()
        throws Exception
    {
        testOneWay(
                createReaderDescription(LifReader.class), // the reader
                createEngineDescription(LifWriter.class,  // the writer
                        LifWriter.PARAM_WRITE_TIMESTAMP, false),  
                "lif/dependencystructure-ref.lif", // the reference file for the output
                "lif/dependencystructure.lif"); // the input file for the test
    }
    
    @Test
    public void onePhraseStructure()
        throws Exception
    {
        testOneWay(
                createReaderDescription(LifReader.class), // the reader
                createEngineDescription(LifWriter.class,  // the writer
                        LifWriter.PARAM_WRITE_TIMESTAMP, false),  
                "lif/phrasestructure-ref.lif", // the reference file for the output
                "lif/phrasestructure.lif"); // the input file for the test
    }


    @Test
    public void oneNamedEntity()
            throws Exception
    {
        testOneWay(
                createReaderDescription(LifReader.class), // the reader
                createEngineDescription(LifWriter.class,  // the writer
                        LifWriter.PARAM_WRITE_TIMESTAMP, false),
                "lif/specification-ner-ref.lif", // the reference file for the output
                "lif/specification-ner.lif"); // the input file for the test
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
