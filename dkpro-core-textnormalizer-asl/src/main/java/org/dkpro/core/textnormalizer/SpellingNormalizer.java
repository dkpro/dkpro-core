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

import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.transform.JCasTransformerChangeBased_ImplBase;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SpellingAnomaly;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Converts annotations of the type SpellingAnomaly into a SofaChangeAnnoatation.
 */
@Component(OperationType.NORMALIZER)
@ResourceMetaData(name = "Spelling Normalizer")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@TypeCapability(
        inputs = { 
                "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SpellingAnomaly" })
public class SpellingNormalizer
    extends JCasTransformerChangeBased_ImplBase
{
    @Override
    public void process(JCas aInput, JCas aOutput)
        throws AnalysisEngineProcessException
    {
        for (SpellingAnomaly anomaly : select(aInput, SpellingAnomaly.class)) {
            replace(anomaly.getBegin(), anomaly.getEnd(), getBestSuggestion(anomaly));
        }
    }

    /**
     * Just gets the Suggestion with the highest Certainty. In case there are more suggestions with
     * certainty 100, it passes the first highest one.
     * 
     * @param anomaly
     *            the anomaly.
     * @return the best suggestion.
     */
    private String getBestSuggestion(SpellingAnomaly anomaly)
    {
        Float bestCertainty = 0.0f;
        String bestReplacement = "";

        for (int i = 0; i < anomaly.getSuggestions().size(); i++) {
            Float currentCertainty = anomaly.getSuggestions(i).getCertainty();
            String currentReplacement = anomaly.getSuggestions(i).getReplacement();

            if (currentCertainty > bestCertainty) {
                bestCertainty = currentCertainty;
                bestReplacement = currentReplacement;
            }

            if (bestCertainty == 100) {
                break;
            }
        }

        return bestReplacement;
    }
}
