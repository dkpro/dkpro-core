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

package de.tudarmstadt.ukp.dkpro.core.textnormalizer.transformation;

import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertTransformedText;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class TokenCaseTransformerTest
{
    @Test
    public void testLowerCase()
        throws Exception
    {
        String inputText = "Ich lebe in Braunschweig-Stadt.";
        String normalizedText = "ich lebe in braunschweig-stadt.";

        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription normalizer = createEngineDescription(TokenCaseTransformer.class,
                TokenCaseTransformer.PARAM_CASE, TokenCaseTransformer.Case.LOWERCASE);

        assertTransformedText(normalizedText, inputText, "de", segmenter, normalizer);
    }

    @Test
    public void testNormalCase()
        throws Exception
    {
        String inputText = "Ich lebe in BRAUNSCHWEIG-Stadt.";
        String normalizedText = "Ich lebe in Braunschweig-Stadt.";

        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription normalizer = createEngineDescription(TokenCaseTransformer.class,
                TokenCaseTransformer.PARAM_CASE, TokenCaseTransformer.Case.NORMALCASE);

        assertTransformedText(normalizedText, inputText, "de", segmenter, normalizer);
    }

    @Test
    public void testNormalCaseHyphen()
        throws ResourceInitializationException
    {
        String inputText = "-Ich lebe in BRAUNSCHWEIG-Stadt-";
        String normalizedText = "-Ich lebe in Braunschweig-Stadt-";

        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription normalizer = createEngineDescription(TokenCaseTransformer.class,
                TokenCaseTransformer.PARAM_CASE, TokenCaseTransformer.Case.NORMALCASE);

        assertTransformedText(normalizedText, inputText, "de", segmenter, normalizer);
    }

    @Test
    public void testNormalCaseHyphen2()
        throws ResourceInitializationException
    {
        String inputText = "-Ich lebe in BRAUNSCHWEIG-stadt-";
        String normalizedText = "-Ich lebe in Braunschweig-stadt-";

        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription normalizer = createEngineDescription(TokenCaseTransformer.class,
                TokenCaseTransformer.PARAM_CASE, TokenCaseTransformer.Case.NORMALCASE);

        assertTransformedText(normalizedText, inputText, "de", segmenter, normalizer);
    }

    @Test
    public void testNormalCase2()
        throws UIMAException
    {
        String inputText = "Ich lebe in Braunschweig";
        String[] tokensExpected = new String[] { "Ich", "lebe", "in", "Braunschweig" };

        AnalysisEngineDescription normalizer;
        normalizer = createEngineDescription(TokenCaseTransformer.class,
                TokenCaseTransformer.PARAM_CASE, TokenCaseTransformer.Case.NORMALCASE);

        JCas jcas = TestRunner.runTest(normalizer, "de", inputText);
        AssertAnnotations.assertToken(tokensExpected, select(jcas, Token.class));

    }

    @Test
    public void testUpperCase()
        throws Exception
    {
        String inputText = "Ich lebe in Braunschweig-Stadt.";
        String normalizedText = "ICH LEBE IN BRAUNSCHWEIG-STADT.";

        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription normalizer = createEngineDescription(TokenCaseTransformer.class,
                TokenCaseTransformer.PARAM_CASE, TokenCaseTransformer.Case.UPPERCASE);

        assertTransformedText(normalizedText, inputText, "de", segmenter, normalizer);
    }
}
