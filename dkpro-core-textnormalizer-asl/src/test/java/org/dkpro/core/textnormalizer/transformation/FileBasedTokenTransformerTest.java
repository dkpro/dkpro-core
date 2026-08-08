/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
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
 * @see FileBasedTokenTransformer
 *
 */
public class FileBasedTokenTransformerTest
{
    private static final String WORDLIST = "src/test/resources/dictionary/ngerman";

    @Test
    public void test() throws Exception
    {
        String replacement = "INLIST";

        String expected = "Ich INLIST INLIST INLIST, yeah!";
        String input = "Ich lebe in Braunschweig, yeah!";

        assertTransformedText(expected, input, "de",
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(FileBasedTokenTransformer.class,
                        FileBasedTokenTransformer.PARAM_MODEL_LOCATION, WORDLIST,
                        FileBasedTokenTransformer.PARAM_REPLACEMENT, replacement));
    }

    @Test
    public void testIgnoreCase() throws Exception
    {
        String replacement = "INLIST";

        String expected = "INLIST INLIST INLIST INLIST, yeah!";
        String input = "Ich lebe in Braunschweig, yeah!";

        assertTransformedText(expected, input, "de",
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(FileBasedTokenTransformer.class,
                        FileBasedTokenTransformer.PARAM_MODEL_LOCATION, WORDLIST,
                        FileBasedTokenTransformer.PARAM_IGNORE_CASE, true,
                        FileBasedTokenTransformer.PARAM_REPLACEMENT, replacement));
    }
}
