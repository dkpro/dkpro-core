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
package org.dkpro.core.textnormalizer.frequency;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.ExternalResourceFactory.createResourceDescription;
import static org.dkpro.core.testing.AssertAnnotations.assertTransformedText;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.dkpro.core.frequency.resources.Web1TFrequencyCountResource;
import org.dkpro.core.tokit.BreakIteratorSegmenter;
import org.junit.jupiter.api.Test;

public class SharpSNormalizerTest
{
    @Test
    public void test() throws Exception
    {
        //check sharpS normalization
        test("süss", "süß");
    }

    public void test(String inputText, String normalizedText)
        throws Exception
    {
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription normalizer = createEngineDescription(
                SharpSNormalizer.class,
                SharpSNormalizer.PARAM_MIN_FREQUENCY_THRESHOLD,0,
                SharpSNormalizer.RES_FREQUENCY_PROVIDER, createResourceDescription(
                        Web1TFrequencyCountResource.class,
                        Web1TFrequencyCountResource.PARAM_LANGUAGE, "de",
                        Web1TFrequencyCountResource.PARAM_MIN_NGRAM_LEVEL, "1",
                        Web1TFrequencyCountResource.PARAM_MAX_NGRAM_LEVEL, "1",
                        Web1TFrequencyCountResource.PARAM_INDEX_PATH, "src/test/resources/jweb1t"));

        assertTransformedText(normalizedText, inputText, "de", segmenter, normalizer);
    }    
}
