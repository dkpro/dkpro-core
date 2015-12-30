/*******************************************************************************
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.core.io.text;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

public class TextWriterTest
{
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
            runPipeline(jcas,  writer);
            
            System.out.close();
        }
        finally {
            System.setOut(originalOut);
        }
        
        assertEquals(text, baos.toString("UTF-8"));
    }
}
