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
package org.dkpro.core.textnormalizer;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.dkpro.core.testing.AssertAnnotations.assertTransformedText;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.dkpro.core.jazzy.JazzyChecker;
import org.dkpro.core.textnormalizer.transformation.HyphenationRemover;
import org.dkpro.core.tokit.BreakIteratorSegmenter;
import org.junit.jupiter.api.Test;

public class MultiNormalizationTest
{
    @Test
    public void test()
            throws Exception
    {
        runTest("Isch habe ein- en super-tollen Bär-\nen.", "ich habe einen spirituellen Bären.");
    }
    
    public void runTest(String inputText, String normalizedText)
        throws Exception
    {

        AnalysisEngineDescription hyphens = createEngineDescription(
                HyphenationRemover.class,
                HyphenationRemover.PARAM_MODEL_LOCATION, "src/test/resources/dictionary/ngerman");

        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription spellchecker = createEngineDescription(
                JazzyChecker.class,
                JazzyChecker.PARAM_MODEL_LOCATION, "src/test/resources/dictionary/ngerman");

        AnalysisEngineDescription spelling = createEngineDescription(SpellingNormalizer.class);

        assertTransformedText(normalizedText, inputText, "de",hyphens, segmenter, spellchecker, 
                spelling);
    }
}
