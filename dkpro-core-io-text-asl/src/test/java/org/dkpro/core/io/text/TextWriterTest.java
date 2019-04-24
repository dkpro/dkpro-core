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
package org.dkpro.core.io.text;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.io.text.TextWriter;
import org.dkpro.core.testing.DkproTestContext;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionMethod;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;

public class TextWriterTest
{
    @Test
    public void testWriteWithDocumentUri() throws Exception
    {
        File outputPath = testContext.getTestOutputFolder();
        
        AnalysisEngineDescription writer = createEngineDescription(TextWriter.class,
                TextWriter.PARAM_TARGET_LOCATION, outputPath,
                TextWriter.PARAM_STRIP_EXTENSION, true,
                TextWriter.PARAM_OVERWRITE, true);
        
        JCas jcas = JCasFactory.createJCas();
        
        DocumentMetaData dmd = DocumentMetaData.create(jcas);
        dmd.setDocumentBaseUri("file:/dummy");
        dmd.setDocumentUri("file:/dummy/text1.txt");
        
        runPipeline(jcas, writer);
        
        assertTrue(new File(outputPath, "text1.txt").exists());
    }

    @Test
    public void testWriteWithDocumentId() throws Exception
    {
        File outputPath = testContext.getTestOutputFolder();
        
        AnalysisEngineDescription writer = createEngineDescription(TextWriter.class,
                TextWriter.PARAM_TARGET_LOCATION, outputPath,
                TextWriter.PARAM_STRIP_EXTENSION, true,
                TextWriter.PARAM_OVERWRITE, true);
        
        JCas jcas = JCasFactory.createJCas();
        
        DocumentMetaData dmd = DocumentMetaData.create(jcas);
        dmd.setCollectionId("dummy");
        dmd.setDocumentId("text1.txt");
        
        runPipeline(jcas, writer);
        
        assertTrue(new File(outputPath, "text1.txt").exists());
    }

    @Test
    public void testStdOut()
        throws Exception
    {
        final String text = "This is a test";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(baos));

            JCas jcas = JCasFactory.createJCas();
            jcas.setDocumentText(text);

            AnalysisEngineDescription writer = createEngineDescription(TextWriter.class);
            runPipeline(jcas, writer);

            System.out.close();
        }
        finally {
            System.setOut(originalOut);
        }

        assertEquals(text, baos.toString("UTF-8"));
    }

    @Test
    public void testCompressed()
        throws Exception
    {
        String text = StringUtils.repeat("This is a test. ", 100000);
        
        File outputPath = testContext.getTestOutputFolder();
        
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentText(text);
        
        DocumentMetaData meta = DocumentMetaData.create(jcas);
        meta.setDocumentId("dummy");

        AnalysisEngineDescription writer = createEngineDescription(TextWriter.class,
                TextWriter.PARAM_COMPRESSION, CompressionMethod.GZIP,
                TextWriter.PARAM_TARGET_LOCATION, outputPath);
        runPipeline(jcas, writer);
        
        File input = new File(outputPath, "dummy.txt.gz");
        InputStream is = CompressionUtils.getInputStream(input.getPath(),
                new FileInputStream(input));
        assertEquals(text, IOUtils.toString(is));
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
