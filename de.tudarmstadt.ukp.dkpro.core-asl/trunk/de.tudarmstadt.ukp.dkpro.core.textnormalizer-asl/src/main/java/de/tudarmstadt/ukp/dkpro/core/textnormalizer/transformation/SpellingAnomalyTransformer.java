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

import static org.apache.uima.fit.util.JCasUtil.*;

import java.util.List;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SpellingAnomaly;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

@TypeCapability(
        inputs = { "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SpellingAnomaly" })
public class SpellingAnomalyTransformer
    extends JCasTransformerChangeBased_ImplBase
{
    @Override
    public void process(JCas aInput, JCas aOutput)
        throws AnalysisEngineProcessException
    {
        // Get anomalies per token. We don't want to assume here that anomalies may not overlap
        // and this approach is an easy way to avoid this.
        for (Token token : select(aInput, Token.class)) {
            List<SpellingAnomaly> anomalyList = selectCovered(SpellingAnomaly.class, token);

            if (!anomalyList.isEmpty()) {
                SpellingAnomaly anomaly = anomalyList.get(0);
                
                replace(anomaly.getBegin(), anomaly.getEnd(), getBestSuggestion(anomaly));
            }
        }
    }

    /**
     * Just gets the Suggestion with the highest Certainty. In case there are more suggestions with
     * certainty 100, it passes the first highest one.
     * 
     * @param anomaly a anomaly.
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
