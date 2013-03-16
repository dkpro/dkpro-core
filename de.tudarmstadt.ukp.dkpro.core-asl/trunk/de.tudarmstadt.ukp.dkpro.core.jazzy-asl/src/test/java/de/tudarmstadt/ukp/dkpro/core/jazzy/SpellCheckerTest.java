/*******************************************************************************
 * Copyright 2010
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
package de.tudarmstadt.ukp.dkpro.core.jazzy;

import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.util.JCasUtil.select;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Test;
import org.uimafit.testing.factory.TokenBuilder;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SpellingAnomaly;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class SpellCheckerTest
{
    @Test
    public void spellCheckerTest()
        throws Exception
    {
        String testDocumentEnglish = "The cat sta on the mat . Some errosr occur in user "
                + "discourse morre often . What do you tink ?";

        List<String> errorsEnglish = new ArrayList<String>();
        errorsEnglish.add("sta");
        errorsEnglish.add("errosr");
        errorsEnglish.add("morre");
        errorsEnglish.add("tink");

        runEnglishSpellChecker(testDocumentEnglish, errorsEnglish);
    }

    private void runEnglishSpellChecker(String testDocument, List<String> errors)
        throws Exception
    {
		AnalysisEngine engine = createPrimitive(SpellChecker.class,
				SpellChecker.PARAM_MODEL_LOCATION, "src/test/resources/testdict.txt");
        JCas aJCas = engine.newJCas();

        TokenBuilder<Token, Sentence> tb = TokenBuilder.create(Token.class, Sentence.class);
        tb.buildTokens(aJCas, testDocument);
        engine.process(aJCas);

        int i = 0;
        for (SpellingAnomaly errorAnnotation : select(aJCas, SpellingAnomaly.class)) {
            System.out.println(errorAnnotation.getCoveredText() + " - "
                    + errorAnnotation.getSuggestions(0).getReplacement());
            assertEquals(errors.get(i), errorAnnotation.getCoveredText());
            i++;
        }
        System.out.println("Found " + i + " errors");
    }
}
