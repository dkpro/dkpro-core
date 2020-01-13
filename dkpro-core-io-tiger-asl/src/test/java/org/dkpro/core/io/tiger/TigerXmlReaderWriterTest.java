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
package org.dkpro.core.io.tiger;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.custommonkey.xmlunit.XMLAssert;
import org.dkpro.core.io.tiger.TigerXmlReader;
import org.dkpro.core.io.tiger.TigerXmlWriter;
import org.dkpro.core.testing.DkproTestContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class TigerXmlReaderWriterTest
{
    @Before
    public void setUp() throws IOException {
        DkproTestContext.get().initializeTestWorkspace();
    }    
    
    @Test
    public void test() throws Exception
    {
        File targetFolder = testContext.getTestOutputFolder();

        CollectionReaderDescription reader = createReaderDescription(
                TigerXmlReader.class,
                TigerXmlReader.PARAM_SOURCE_LOCATION, "src/test/resources",
                TigerXmlReader.PARAM_PATTERNS, "simple-sentence.xml");
                
        AnalysisEngineDescription writer = createEngineDescription(TigerXmlWriter.class,
                TigerXmlWriter.PARAM_STRIP_EXTENSION, true,
                TigerXmlWriter.PARAM_TARGET_LOCATION, targetFolder);
        
        SimplePipeline.runPipeline(reader, writer);
        
        try (
                Reader expected = new InputStreamReader(new FileInputStream(
                        "src/test/resources/simple-sentence.xml"), "UTF-8");
                Reader actual = new InputStreamReader(new FileInputStream(
                        new File(targetFolder, "simple-sentence.xml")), "UTF-8");
        ) {
            XMLAssert.assertXMLEqual(expected, actual);
        }
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
