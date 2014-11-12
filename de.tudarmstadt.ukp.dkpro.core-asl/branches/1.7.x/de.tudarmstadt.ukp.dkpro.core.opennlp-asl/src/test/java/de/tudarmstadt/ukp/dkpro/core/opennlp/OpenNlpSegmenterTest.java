/*******************************************************************************
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.opennlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.harness.SegmenterHarness;

public class OpenNlpSegmenterTest
{
    @Test
    public void testItalian() throws Exception
    {
        final String language = "it";
        final String variant = "maxent";
        final String text = "Questo è un test. E un altro ancora.";
        final String[] sentences = { "Questo è un test.", "E un altro ancora." };
        final String[] tokens = { "Questo", "è", "un", "test", ".", "E", "un", "altro", "ancora",
                "." };
        runTest(language, variant, text, sentences, tokens);
        runTestWithModelsLocation(language, variant, text, sentences, tokens);
    }
    
    @Ignore("We don't have these models integrated yet")
    @Test
    public void testPortugueseCogroo() throws Exception
    {
        final String text = "Este é um teste. E mais uma.";
        final String[] sentences = { "Este é um teste.", "E mais uma." };
        final String[] tokens = { "Este", "é", "um", "teste", ".", "E", "mais", "uma", "." };
        
        runTest("pt", "cogroo", text, sentences, tokens);
    }

    @Test
    public void runHarness()
        throws Throwable
    {
        AnalysisEngineDescription aed = createEngineDescription(OpenNlpSegmenter.class);

        SegmenterHarness.run(aed, "de.1", "en.7", "en.9", "ar.1", "zh.1", "zh.2");
    }

    private JCas runTest(String aLanguage, String aVariant, String aDocument, String[] sentences,
            String[] tokens)
        throws Exception
    {
        AnalysisEngine engine = createEngine(OpenNlpSegmenter.class,
                OpenNlpSegmenter.PARAM_VARIANT, aVariant);
        
        // Cannot use TestRunner because that uses TokenBuilder to create a segmentation.
        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage(aLanguage);
        jcas.setDocumentText(aDocument);
        engine.process(jcas);
        
        AssertAnnotations.assertSentence(sentences, select(jcas, Sentence.class));
        AssertAnnotations.assertToken(tokens, select(jcas, Token.class));

        return jcas;
    }

    private JCas runTestWithModelsLocation(final String aLanguage, final String variant, 
            final String aDocument, final String[] sentences, final String[] tokens)
        throws Exception
    {
        final AnalysisEngine engine = createEngine(OpenNlpSegmenter.class,
                OpenNlpSegmenter.PARAM_VARIANT, variant,
                OpenNlpSegmenter.PARAM_SEGMENTATION_MODEL_LOCATION,
                "classpath:/de/tudarmstadt/ukp/dkpro/core/opennlp/lib/sentence-" + aLanguage + "-"
                        + variant + ".bin", OpenNlpSegmenter.PARAM_TOKENIZATION_MODEL_LOCATION,
                "classpath:/de/tudarmstadt/ukp/dkpro/core/opennlp/lib/token-" + aLanguage + "-"
                        + variant + ".bin");
        
        // Cannot use TestRunner because that uses TokenBuilder to create a segmentation.
        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage(aLanguage);
        jcas.setDocumentText(aDocument);
        engine.process(jcas);

        AssertAnnotations.assertSentence(sentences, select(jcas, Sentence.class));
        AssertAnnotations.assertToken(tokens, select(jcas, Token.class));

        return jcas;
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}
