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

package org.dkpro.core.textnormalizer.transformation;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.dkpro.core.testing.AssertAnnotations.assertTransformedText;

import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.tokit.BreakIteratorSegmenter;
import org.junit.jupiter.api.Test;

public class DictionaryBasedTokenTransformerTest
{
    private static final String MAPPINGS_FILE = "src/test/resources/mappings.txt";

    @Test
    public void test() throws Exception
    {
        String expected = "Ich lebe in Braunschweig.";
        String input = "Ich lebe in Brannfchweig.";

        assertTransformedText(expected, input, "de",
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(DictionaryBasedTokenTransformer.class,
                        DictionaryBasedTokenTransformer.PARAM_MODEL_LOCATION, MAPPINGS_FILE));
    }

    @Test
    public void testFileNotFound() throws ResourceInitializationException
    {
        String expected = "Ich lebe in Braunschweig.";
        String input = "Ich lebe in Brannfchweig.";
        String model = "noModelHere";

        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> {
            assertTransformedText(expected, input, "de",
                    createEngineDescription(BreakIteratorSegmenter.class),
                    createEngineDescription(DictionaryBasedTokenTransformer.class,
                            DictionaryBasedTokenTransformer.PARAM_MODEL_LOCATION, model));
        });
    }
}
