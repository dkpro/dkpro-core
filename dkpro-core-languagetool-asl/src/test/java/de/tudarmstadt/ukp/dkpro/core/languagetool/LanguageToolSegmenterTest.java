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
package de.tudarmstadt.ukp.dkpro.core.languagetool;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.languagetool.Languages;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.harness.SegmenterHarness;

public class LanguageToolSegmenterTest
{
    @Ignore("Only needed to get the list of the supported languages for the @LanguageCapability")
    @Test
    public void listLocales() throws Exception
    {
        List<String> supportedLanguages = Languages.get().stream()
            .map(l -> l.getLocale().getLanguage())
            .distinct()
            .filter(lang -> lang.length() == 2)
            .collect(Collectors.toList());
        
        System.out.printf("[");
        for (String l : supportedLanguages) {
            System.out.printf("\"%s\", ", l);
        }
        System.out.printf("]");
    }
    
    
    @Test
    public void testTwoSentences() throws Exception
    {
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("This is a test. This is another one.");
        
        AnalysisEngine aed = createEngine(LanguageToolSegmenter.class);
        aed.process(jcas);
        
        String[] sentences = { "This is a test.", "This is another one." };
        
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
        
        String[] sentences = { "I bought a car for my little brother.",
                "He said, he likes it a lot." };
        
        AssertAnnotations.assertSentence(sentences, select(jcas, Sentence.class));
    }

    @Test
    public void testTraditionalChinese() throws Exception
    {
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentLanguage("zh");
        jcas.setDocumentText("毛澤東住在北京");
        
        AnalysisEngine aed = createEngine(LanguageToolSegmenter.class);
        aed.process(jcas);
        
        String[] tokens = { "毛澤東", "住", "在", "北京" };
        
        AssertAnnotations.assertToken(tokens, select(jcas, Token.class));
    }

    @Test
    public void run()
        throws Throwable
    {
        AnalysisEngineDescription aed = createEngineDescription(LanguageToolSegmenter.class);

        SegmenterHarness.run(aed, "de.1", "en.1", "en.3", "en.6", "en.7", "en.9", "ar.1", "zh.2");
    }

    @Test
    public void testZoning() throws Exception
    {
        SegmenterHarness.testZoning(LanguageToolSegmenter.class);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
