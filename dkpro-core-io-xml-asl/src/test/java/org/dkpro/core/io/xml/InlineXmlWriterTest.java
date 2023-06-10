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
package org.dkpro.core.io.xml;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class InlineXmlWriterTest
{
    @Test
    public void testInlineXmlCasConsumer(@TempDir File tempDir)
        throws Exception
    {
        String testDocument = "This is a test.";

        AnalysisEngine consumer = createEngine(InlineXmlWriter.class,
                InlineXmlWriter.PARAM_TARGET_LOCATION, tempDir.getPath(),
                InlineXmlWriter.PARAM_STRIP_EXTENSION, true);

        JCas jcas = consumer.newJCas();
        jcas.setDocumentText(testDocument);

        DocumentMetaData meta = DocumentMetaData.create(jcas);
        meta.setDocumentId("testId");
        meta.setDocumentTitle("title");
        meta.setDocumentBaseUri(tempDir.toURI().toString());
        meta.setDocumentUri(new File(tempDir, "test.txt").toURI().toString());

        JCas view = jcas.createView("plainTextDocument");
        view.setDocumentText(testDocument);

        consumer.process(jcas);

        File writtenFile = new File(tempDir, "test.xml");
        if (!writtenFile.exists()) {
            fail("File not correctly written.");
        }
    }
}
