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
package de.tudarmstadt.ukp.dkpro.core.io.tiger;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.custommonkey.xmlunit.XMLAssert;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.TestRunner;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpParser;

public class TigerXmlWriterTest
{
    @Test
    public void test() throws Exception
    {
        File targetFolder = testContext.getTestOutputFolder();
        
        AnalysisEngine parser = createEngine(OpenNlpParser.class,
                OpenNlpParser.PARAM_WRITE_POS, true);
        JCas jcas = TestRunner.runTest(parser, "en", "This is a test .");

        DocumentMetaData meta = DocumentMetaData.create(jcas);
        meta.setCollectionId("nocollection");
        meta.setDocumentId("dummy");
        
        AnalysisEngine writer = createEngine(TigerXmlWriter.class,
                TigerXmlWriter.PARAM_TARGET_LOCATION, targetFolder);
        writer.process(jcas);
        
        try (
                Reader expected = new InputStreamReader(new FileInputStream(
                        "src/test/resources/simple-sentence.xml"), "UTF-8");
                Reader actual = new InputStreamReader(new FileInputStream(
                        new File(targetFolder, "dummy.xml")), "UTF-8");
        ) {
            XMLAssert.assertXMLEqual(expected, actual);
        }
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
