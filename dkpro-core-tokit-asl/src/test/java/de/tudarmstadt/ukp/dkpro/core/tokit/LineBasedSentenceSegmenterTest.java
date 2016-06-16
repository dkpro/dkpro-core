/*
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.core.tokit;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;

public class LineBasedSentenceSegmenterTest
{
    @Test
    public void testUnix()
        throws Exception
    {
        String text = "This is \n" + "a test.\n\n" + "Right now!\nEnd";

        String[] sentences = { "This is", "a test.", "Right now!", "End" };
        
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentText(text);
        SimplePipeline.runPipeline(jcas, createEngineDescription(LineBasedSentenceSegmenter.class));
        
        AssertAnnotations.assertSentence(sentences, select(jcas, Sentence.class));
    }

    @Test
    public void testWindows()
        throws Exception
    {
        String text = "This is \r\n" + "a test.\r\n\r\n" + "Right now!\r\nEnd";

        String[] sentences = { "This is", "a test.", "Right now!", "End" };
        
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentText(text);
        SimplePipeline.runPipeline(jcas, createEngineDescription(LineBasedSentenceSegmenter.class));
        
        AssertAnnotations.assertSentence(sentences, select(jcas, Sentence.class));
    }
    
    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
    
    @Before
    public void setupLogging()
    {
        System.setProperty("org.apache.uima.logger.class", "org.apache.uima.util.impl.Log4jLogger_impl");
    }
}
