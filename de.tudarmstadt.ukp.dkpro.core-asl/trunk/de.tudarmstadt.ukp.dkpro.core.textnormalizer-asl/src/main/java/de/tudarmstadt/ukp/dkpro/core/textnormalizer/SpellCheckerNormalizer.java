/*******************************************************************************
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.textnormalizer;

import static de.tudarmstadt.ukp.dkpro.core.castransformation.ApplyChangesAnnotator.OP_REPLACE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SpellingAnomaly;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation;
import de.tudarmstadt.ukp.dkpro.core.castransformation.alignment.AlignedString;

/**
 * Converts annotations of the type SpellingAnomaly into a SofaChangeAnnoatation.
 * 
 * @author Sebastian Kneise
 * 
 */

@TypeCapability
(
        inputs = { "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SpellingAnomaly" }, 
        outputs = { "de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation"}
        )

public class SpellCheckerNormalizer extends Normalizer_ImplBase 
{
    @Override
    protected Map<Integer, List<SofaChangeAnnotation>> createSofaChangesMap( JCas jcas) 
    {
        Map<Integer, List<SofaChangeAnnotation>> changesMap = new TreeMap<Integer, List<SofaChangeAnnotation>>();
        int changesMapIndex = 1;

        for (Token token : JCasUtil.select(jcas, Token.class)) 
        {	    
            List<SofaChangeAnnotation> tokenChangeList = new ArrayList<SofaChangeAnnotation>();

            List<SpellingAnomaly> anomalyList = JCasUtil.selectCovered(jcas, SpellingAnomaly.class, token.getBegin(), token.getEnd());

            if (!anomalyList.isEmpty()) 
            {
                SpellingAnomaly anomaly = anomalyList.get(0);
                SofaChangeAnnotation sca = new SofaChangeAnnotation(jcas);
                sca.setBegin(anomaly.getBegin());
                sca.setEnd(anomaly.getEnd());
                sca.setOperation(OP_REPLACE);
                sca.setValue(getBestSuggestion(anomaly));
                tokenChangeList.add(sca);
            }

            changesMap.put(changesMapIndex, tokenChangeList);
            changesMapIndex++;
        }

        return changesMap;
    }

    @Override
    protected Map<Integer, Boolean> createTokenReplaceMap(JCas jcas, AlignedString as) throws AnalysisEngineProcessException 
    {
        Map<Integer, Boolean> tokenReplaceMap = new TreeMap<Integer, Boolean>();

        int replaceMapIndex = 1;

        for (Token token : JCasUtil.select(jcas, Token.class)) 
        {
            List<SpellingAnomaly> anomalyList = JCasUtil.selectCovered(jcas, SpellingAnomaly.class, token.getBegin(), token.getEnd());

            if (!anomalyList.isEmpty()) 
            {
                tokenReplaceMap.put(replaceMapIndex, true);
            } 
            else 
            {
                tokenReplaceMap.put(replaceMapIndex, false);
            }

            replaceMapIndex++;
        }

        return tokenReplaceMap;

    }

    // Just gets the Suggestion with the highest Certainty.
    // In case there are more suggestions with certainty 100, it passes the
    // first highest one.
    private String getBestSuggestion(SpellingAnomaly anomaly) 
    {
        Float bestCertainty = 0.0f;
        String bestReplacement = "";

        for (int i = 0; i < anomaly.getSuggestions().size(); i++) 
        {
            Float currentCertainty = anomaly.getSuggestions(i).getCertainty();
            String currentReplacement = anomaly.getSuggestions(i).getReplacement();

            if (currentCertainty > bestCertainty) 
            {
                bestCertainty = currentCertainty;
                bestReplacement = currentReplacement;
            }

            if (bestCertainty == 100) 
            {
                break;
            }
        }

        return bestReplacement;
    }

}
