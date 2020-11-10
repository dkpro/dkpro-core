/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.bioc;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.dkpro.core.testing.IOTestRunner.testOneWay;

import org.dkpro.core.io.bioc.BioCReader;
import org.dkpro.core.io.bioc.BioCWriter;
import org.dkpro.core.testing.TestOptions;
import org.junit.Test;

public class BioCReaderWriterTest
{
    @Test
    public void testEverything() throws Exception
    {
        testOneWay(
                createReaderDescription(BioCReader.class), 
                createEngineDescription(BioCWriter.class),
                "xml/everything-formatted-ref.xml",
                "xml/everything-formatted.xml", 
                new TestOptions().keepDocumentMetadata());
    }

    @Test
    public void testPMC() throws Exception
    {
        testOneWay(
                createReaderDescription(BioCReader.class), 
                createEngineDescription(BioCWriter.class),
                "pmc/PMC1790863-ref.xml", 
                "pmc/PMC1790863.xml", 
                new TestOptions().keepDocumentMetadata());
    }

    @Test
    public void testSimplifiedSentences() throws Exception
    {
        testOneWay(
                createReaderDescription(BioCReader.class), 
                createEngineDescription(BioCWriter.class),
                "xml/PMID-8557975-simplified-sentences-tokens-ref.xml", 
                "xml/PMID-8557975-simplified-sentences-tokens.xml", 
                new TestOptions().keepDocumentMetadata());
    }
}
