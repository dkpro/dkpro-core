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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.impl.FeatureStructureImplC;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.opennlp.OpenNlpParser;
import org.dkpro.core.testing.TestRunner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xmlunit.assertj3.XmlAssert;
import org.xmlunit.builder.Input;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class TigerXmlWriterTest
{
    @BeforeAll
    static void setupClass() {
        // V2 FS toString needed for CasDumpWriter. Also see comment in the root-level pom.xml
        // file where this property is globally set for all surefire runs
        System.setProperty(FeatureStructureImplC.V2_PRETTY_PRINT, "true");
    }
    
    @Test
    public void test(@TempDir File tempDir) throws Exception
    {
        AnalysisEngine parser = createEngine( //
                OpenNlpParser.class, //
                OpenNlpParser.PARAM_WRITE_POS, true);
        JCas jcas = TestRunner.runTest(parser, "en", "This is a test .");

        DocumentMetaData meta = DocumentMetaData.create(jcas);
        meta.setCollectionId("nocollection");
        meta.setDocumentId("dummy");

        AnalysisEngine writer = createEngine( //
                TigerXmlWriter.class, //
                TigerXmlWriter.PARAM_TARGET_LOCATION, tempDir);
        writer.process(jcas);

        XmlAssert.assertThat(Input.fromFile(new File(tempDir, "dummy.xml")))
                .and(Input.fromFile("src/test/resources/simple-sentence.xml")).areSimilar();
    }
}
