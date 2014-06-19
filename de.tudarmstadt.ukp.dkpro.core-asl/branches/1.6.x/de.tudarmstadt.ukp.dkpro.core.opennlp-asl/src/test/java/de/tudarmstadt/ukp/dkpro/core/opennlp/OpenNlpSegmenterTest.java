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

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
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
        final String[] sentences = new String[] { "Questo è un test.", "E un altro ancora." };
        final String[] tokens = new String[] { "Questo", "è", "un", "test", ".", "E", "un",
                "altro", "ancora", "." };
        runTest(language, variant, text, sentences, tokens);
        runTestWithModelsLocation(language, variant, text, sentences, tokens);
    }

    @Test
    public void runHarness()
        throws Throwable
    {
        AnalysisEngineDescription aed = createEngineDescription(OpenNlpSegmenter.class);

        SegmenterHarness.run(aed, "de.1", "en.7", "en.9", "ar.1", "zh.1", "zh.2");
    }

    private void runTest(String language, String variant, String testDocument, String[] sentences,
            String[] tokens)
        throws Exception
    {
        AnalysisEngine engine = createEngine(OpenNlpSegmenter.class,
                OpenNlpSegmenter.PARAM_VARIANT, variant);
        processAndAssert(engine, language, testDocument, sentences, tokens);
    }

    private void runTestWithModelsLocation(final String language, final String variant, final String testDocument,
            final String[] sentences, final String[] tokens)
        throws Exception
    {
        final AnalysisEngine engine = createEngine(OpenNlpSegmenter.class,
                OpenNlpSegmenter.PARAM_VARIANT, variant,
                OpenNlpSegmenter.PARAM_SEGMENTATION_MODEL_LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/core/opennlp/lib/sentence-" + language + "-"
                        + variant + ".bin",
                OpenNlpSegmenter.PARAM_TOKENIZATION_MODEL_LOCATION, "classpath:/de/tudarmstadt/ukp/dkpro/core/opennlp/lib/token-" + language + "-"
                        + variant + ".bin");
        processAndAssert(engine, language, testDocument, sentences, tokens);

    }

    private void processAndAssert(final AnalysisEngine engine, final String language,
            final String testDocument, final String[] sentences, final String[] tokens)
        throws UIMAException
    {
        final JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentLanguage(language);
        jcas.setDocumentText(testDocument);
        engine.process(jcas);
        AssertAnnotations.assertSentence(sentences, select(jcas, Sentence.class));
        AssertAnnotations.assertToken(tokens, select(jcas, Token.class));
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}
