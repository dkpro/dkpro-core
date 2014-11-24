/*******************************************************************************
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.api.io;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;

import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

public class JCasFileWriter_ImplBaseTest
{
    @Test
    public void writeToZip() throws Exception
    {
        AnalysisEngine ae = createEngine(DummyWriter.class,
                DummyWriter.PARAM_TARGET_LOCATION, "jar:file:target/out.zip");
        JCas jcas = JCasFactory.createJCas();
        ae.process(jcas);
        ae.process(jcas);
        ae.process(jcas);
        ae.collectionProcessComplete();
    }

    @Test
    public void writeToZip2() throws Exception
    {
        AnalysisEngine ae = createEngine(DummyWriter.class,
                DummyWriter.PARAM_TARGET_LOCATION, "jar:file:target/out2.zip!test");
        JCas jcas = JCasFactory.createJCas();
        ae.process(jcas);
        ae.process(jcas);
        ae.process(jcas);
        ae.collectionProcessComplete();
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
                docOS.write("This is the file " + count);
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
