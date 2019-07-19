/*
 * Copyright 2014
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
package org.dkpro.core.api.io;

import static java.util.Arrays.asList;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.junit.Test;

public class JCasFileWriter_ImplBaseTest
{
    @Test
    public void writeToZip() throws Exception
    {
        AnalysisEngine ae = createEngine(DummyWriter.class,
                DummyWriter.PARAM_TARGET_LOCATION, "jar:file:target/out.zip",
                DummyWriter.PARAM_OVERWRITE, true);
        JCas jcas = JCasFactory.createJCas();
        ae.process(jcas);
        ae.process(jcas);
        ae.process(jcas);
        ae.collectionProcessComplete();
     
        assertEquals(asList("file-0.txt", "file-1.txt", "file-2.txt"),
                listContents("target/out.zip"));
    }

    @Test
    public void writeToZip2() throws Exception
    {
        AnalysisEngine ae = createEngine(DummyWriter.class,
                DummyWriter.PARAM_TARGET_LOCATION, "jar:file:target/out2.zip!test",
                DummyWriter.PARAM_OVERWRITE, true);
        JCas jcas = JCasFactory.createJCas();
        ae.process(jcas);
        ae.process(jcas);
        ae.process(jcas);
        ae.collectionProcessComplete();
        
        assertEquals(asList("test/file-0.txt", "test/file-1.txt", "test/file-2.txt"),
                listContents("target/out2.zip"));
    }

    @Test
    public void writeToSingularTarget() throws Exception
    {
        File target = new File("target/test-output/singular.txt");
        
        AnalysisEngine ae = createEngine(DummyWriter.class,
                DummyWriter.PARAM_TARGET_LOCATION, target,
                DummyWriter.PARAM_SINGULAR_TARGET, true,
                DummyWriter.PARAM_OVERWRITE, true);
        JCas jcas = JCasFactory.createJCas();
        ae.process(jcas);
        ae.process(jcas);
        ae.process(jcas);
        ae.collectionProcessComplete();
        
        String expected = "This is the file 0\n" + 
                "This is the file 1\n" + 
                "This is the file 2\n";
        
        assertEquals(expected, FileUtils.readFileToString(target, "UTF-8"));
    }

    private List<String> listContents(String aFile)
        throws IOException
    {
        List<String> contents = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(aFile)) {
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                String fname = zipEntries.nextElement().getName();
                contents.add(fname);
            }
        }
        return contents;
    }

    public static final class DummyWriter
        extends JCasFileWriter_ImplBase
    {
        private int count = 0;
        
        @Override
        public void process(JCas aJCas)
            throws AnalysisEngineProcessException
        {
            Writer docOS = null;
            try {
                docOS = new OutputStreamWriter(getOutputStream("file-" + count, ".txt"), "UTF-8");
                docOS.write("This is the file " + count + "\n");
                count++;
            }
            catch (Exception e) {
                throw new AnalysisEngineProcessException(e);
            }
            finally {
                closeQuietly(docOS);
            }
        }
    }
}
