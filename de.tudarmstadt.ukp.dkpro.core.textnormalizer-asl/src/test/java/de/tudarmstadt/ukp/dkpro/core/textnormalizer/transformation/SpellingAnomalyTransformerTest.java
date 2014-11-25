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

import static de.tudarmstadt.ukp.dkpro.core.textnormalizer.transformation.AssertTransformations.assertTransformedText;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.jazzy.JazzyChecker;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class SpellingAnomalyTransformerTest
{
    @Test
    public void test() throws Exception
    {
        String inputText = "ich tesde meiine neue Rechtschreibbeprüfung.";
        String normalizedText = "ich teste meine neue Rechtschreibprüfung.";

        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription spellchecker = createEngineDescription(JazzyChecker.class, 
                JazzyChecker.PARAM_MODEL_LOCATION, "src/test/resources/dictionary/ngerman");
        
        AnalysisEngineDescription normalizer = createEngineDescription(SpellingAnomalyTransformer.class);

        assertTransformedText(normalizedText, inputText, "de", segmenter, spellchecker, normalizer);
        
        // Doesn't work
        // test("LauFEN SCHIEßEN BaföG ÜBERlegen","laufen schießen BAföG überlegen");
    }
}
