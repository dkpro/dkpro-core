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
import static org.dkpro.core.testing.AssertAnnotations.assertTransformedText;

import org.dkpro.core.tokit.BreakIteratorSegmenter;
import org.junit.jupiter.api.Test;

/**
 * 
 * @see RegexBasedTokenTransformer
 *
 */
public class RegexBasedTokenTransformerTest
{
    @Test
    public void test()
        throws Exception
    {
        String regex = "^....$";
        String replacement = "4letters";

        String expected = "Ich 4letters in Braunschweig.";
        String input = "Ich lebe in Braunschweig.";

        assertTransformedText(expected, input, "de",
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(RegexBasedTokenTransformer.class,
                        RegexBasedTokenTransformer.PARAM_REGEX, regex,
                        RegexBasedTokenTransformer.PARAM_REPLACEMENT, replacement));
    }

    @Test
    public void testNumbers()
        throws Exception
    {
        String regex = "[0-9]+";
        String replacement = "0";

        String input = "412- cats sat on 3 mats.";
        String expected = "0- cats sat on 0 mats.";

        assertTransformedText(expected, input, "de",
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(RegexBasedTokenTransformer.class,
                        RegexBasedTokenTransformer.PARAM_REGEX, regex,
                        RegexBasedTokenTransformer.PARAM_REPLACEMENT, replacement));

    }
}
