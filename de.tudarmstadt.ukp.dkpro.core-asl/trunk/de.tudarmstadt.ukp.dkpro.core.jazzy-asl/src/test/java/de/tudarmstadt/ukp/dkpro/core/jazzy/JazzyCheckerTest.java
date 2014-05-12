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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.fit.testing.factory.TokenBuilder;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ExternalResourceDescription;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SpellingAnomaly;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.TestFrequencyCountResource;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class JazzyCheckerTest
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

        AnalysisEngine engine = createEngine(JazzyChecker.class,
                JazzyChecker.PARAM_MODEL_LOCATION, "src/test/resources/testdict.txt");
        
        JCas aJCas = engine.newJCas();

        TokenBuilder<Token, Sentence> tb = TokenBuilder.create(Token.class, Sentence.class);
        tb.buildTokens(aJCas, testDocumentEnglish);
        engine.process(aJCas);

        int i = 0;
        for (SpellingAnomaly errorAnnotation : select(aJCas, SpellingAnomaly.class)) {
//            System.out.println(errorAnnotation.getCoveredText() + " - "
//                    + errorAnnotation.getSuggestions(0).getReplacement());
            assertEquals(errorsEnglish.get(i), errorAnnotation.getCoveredText());
            i++;
        }
        assertEquals(4, i);
    }
    
    @Test
    public void contextualizedSpellCheckerTest()
        throws Exception
    {
        String testDocumentEnglish = "The cat sta on the mat .";

        ExternalResourceDescription resource = ExternalResourceFactory.createExternalResourceDescription(TestFrequencyCountResource.class);

//        String context = DkproContext.getContext().getWorkspace("web1t").getAbsolutePath();
//        String workspace = "en";
//        ExternalResourceDescription resource = ExternalResourceFactory.createExternalResourceDescription(
//                Web1TFrequencyCountResource.class,
//                Web1TFrequencyCountResource.PARAM_MIN_NGRAM_LEVEL, "1",
//                Web1TFrequencyCountResource.PARAM_MAX_NGRAM_LEVEL, "3",
//                Web1TFrequencyCountResource.PARAM_INDEX_PATH, new File(context, workspace).getAbsolutePath()
//        );
        
        AnalysisEngine engine = createEngine(
                createEngineDescription(
                    createEngineDescription(
                        JazzyChecker.class,
                        JazzyChecker.PARAM_SCORE_THRESHOLD, 3,
                        JazzyChecker.PARAM_MODEL_LOCATION, "src/test/resources/testdict_variants.txt"
                    ),
                    createEngineDescription(
                        CorrectionsContextualizer.class,
                        CorrectionsContextualizer.FREQUENCY_PROVIDER_RESOURCE, resource
                    )
                )
        );
        
        JCas aJCas = engine.newJCas();

        TokenBuilder<Token, Sentence> tb = TokenBuilder.create(Token.class, Sentence.class);
        tb.buildTokens(aJCas, testDocumentEnglish);
        engine.process(aJCas);

        int i = 0;
        for (SpellingAnomaly errorAnnotation : select(aJCas, SpellingAnomaly.class)) {
            assertEquals("sat", errorAnnotation.getSuggestions(0).getReplacement());
            i++;
        }
        assertEquals(1, i);
    }
}
