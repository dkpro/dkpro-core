/*******************************************************************************
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
 ******************************************************************************/

package de.tudarmstadt.ukp.dkpro.core.textnormalizer.transformation;

import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertTransformedText;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;

public class HyphenationRemoverTest
{
    @Test
    public void testHyphenationRemover()
        throws Exception
    {
        String inputText = "Ich habe ein- en super-tollen Bär-\nen.";
        String normalizedText = "Ich habe einen super-tollen Bären.";

        AnalysisEngineDescription normalizer = createEngineDescription(HyphenationRemover.class,
                HyphenationRemover.PARAM_MODEL_LOCATION, "src/test/resources/dictionary/ngerman");

        assertTransformedText(normalizedText, inputText, "de", normalizer);
    }

    @Test
    public void testHyphenationRemoverInPipeline()
        throws Exception
    {
        final String language = "de";
        final String variant = "maxent";
        final String text = "Ich habe ein- en super-tollen Bär-\nen. "
                + "Für eine Registrierung einer Organisation und eine EMail Adresse.";
        final String[] sentences = { "Ich habe einen super-tollen Bären.",
                "Für eine Registrierung einer Organisation und eine EMail Adresse." };
        final String[] tokens = { "Ich", "habe", "einen", "super-tollen", "Bären", ".", "Für",
                "eine", "Registrierung", "einer", "Organisation", "und", "eine", "EMail", "Adresse",
                "." };

        AnalysisEngineDescription normalizerAndSegmenter = createEngineDescription(
                createEngineDescription(HyphenationRemover.class,
                        HyphenationRemover.PARAM_MODEL_LOCATION,
                        "src/test/resources/dictionary/ngerman"),
                createEngineDescription(OpenNlpSegmenter.class, OpenNlpSegmenter.PARAM_LANGUAGE,
                        "de", OpenNlpSegmenter.PARAM_VARIANT, variant));
        AnalysisEngine engine = createEngine(normalizerAndSegmenter);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage(language);
        jcas.setDocumentText(text);
        engine.process(jcas);

        AssertAnnotations.assertSentence(sentences, select(jcas, Sentence.class));
        AssertAnnotations.assertToken(tokens, select(jcas, Token.class));
    }
}