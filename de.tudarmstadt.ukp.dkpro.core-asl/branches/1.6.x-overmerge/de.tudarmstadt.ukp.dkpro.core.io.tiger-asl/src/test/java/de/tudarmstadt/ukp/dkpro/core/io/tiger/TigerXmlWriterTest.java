/*******************************************************************************
 * Copyright 2013
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.tiger;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.InputSource;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpParser;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class TigerXmlWriterTest
{
    @Rule
    public TemporaryFolder folder= new TemporaryFolder();
    
    @Test
    public void test() throws Exception
    {
        File targetFolder = folder.newFolder();
        
        AnalysisEngine parser = createEngine(OpenNlpParser.class);
        JCas jcas = TestRunner.runTest(parser, "en", "This is a test .");

        DocumentMetaData meta = DocumentMetaData.create(jcas);
        meta.setCollectionId("nocollection");
        meta.setDocumentId("dummy");
        
        AnalysisEngine writer = createEngine(TigerXmlWriter.class,
                TigerXmlWriter.PARAM_TARGET_LOCATION, targetFolder);
        writer.process(jcas);
        
        XMLAssert.assertXMLEqual(
                new InputSource("src/test/resources/simple-sentence.xml"),
                new InputSource(new File(targetFolder, "dummy.xml").getPath()));
    }
}
