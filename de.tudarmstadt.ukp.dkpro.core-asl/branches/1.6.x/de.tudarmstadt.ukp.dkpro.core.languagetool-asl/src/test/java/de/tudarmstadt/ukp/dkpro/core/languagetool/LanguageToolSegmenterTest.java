/*******************************************************************************
 * Copyright 2010
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
package de.tudarmstadt.ukp.dkpro.core.languagetool;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.harness.SegmenterHarness;

public class LanguageToolSegmenterTest
{
    @Test
    public void testTwoSentences() throws Exception
    {
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("This is a test. This is another one.");
        
        AnalysisEngine aed = createEngine(LanguageToolSegmenter.class);
        aed.process(jcas);
        
        String[] sentences = new String[] {
                "This is a test.", 
                "This is another one." };
        
        AssertAnnotations.assertSentence(sentences, select(jcas, Sentence.class));
    }

    @Test
    public void testTwoSentences2() throws Exception
    {
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("I bought a car for my little brother. He said, he likes it a lot.");
        
        AnalysisEngine aed = createEngine(LanguageToolSegmenter.class);
        aed.process(jcas);
        
        String[] sentences = new String[] {
                "I bought a car for my little brother.", 
                "He said, he likes it a lot." };
        
        AssertAnnotations.assertSentence(sentences, select(jcas, Sentence.class));
    }

    @Test
    public void run()
        throws Throwable
    {
        AnalysisEngineDescription aed = createEngineDescription(LanguageToolSegmenter.class);

        SegmenterHarness.run(aed, "de.1", "en.1", "en.3", "en.6", "en.7", "en.9", "ar.1", "zh.2");
    }
}
