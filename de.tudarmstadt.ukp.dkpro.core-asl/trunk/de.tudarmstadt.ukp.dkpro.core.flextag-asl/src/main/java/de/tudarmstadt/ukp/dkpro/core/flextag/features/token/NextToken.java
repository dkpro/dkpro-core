/*******************************************************************************
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.core.flextag.features.token;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

public class NextToken
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{

    static final String FEATURE_NAME = "nextToken";
    static final String END_OF_SEQUENCE = "EOS";

    public List<Feature> extract(JCas aView, TextClassificationUnit aClassificationUnit)
        throws TextClassificationException
    {
        String featureVal = getNextTokenValue(aView, aClassificationUnit);
        Feature feature = new Feature(FEATURE_NAME, featureVal);

        ArrayList<Feature> features = new ArrayList<Feature>();
        features.add(feature);
        return features;

    }

    private String getNextTokenValue(JCas aView, TextClassificationUnit aClassificationUnit)
    {
        List<Token> tokens = getTokensOfSequenceContainingTheClassificationUnit(aView,
                aClassificationUnit);

        String featureVal = null;
        for (int i = 0; i < tokens.size(); i++) {
            Token currentToken = tokens.get(i);
            if (currentToken.getBegin() != aClassificationUnit.getBegin()) {
                continue;
            }
            if (i + 1 < tokens.size()) {
                featureVal = tokens.get(i + 1).getCoveredText();
            }
            else {
                featureVal = END_OF_SEQUENCE;
            }
            break;
        }
        return featureVal;
    }

    private List<Token> getTokensOfSequenceContainingTheClassificationUnit(JCas aView,
            TextClassificationUnit aClassificationUnit)
    {
        Map<Sentence, Collection<TextClassificationUnit>> indexCovered = JCasUtil.indexCovered(
                aView, Sentence.class, TextClassificationUnit.class);

        Sentence sentence = null;
        List<Sentence> keySet = new ArrayList<Sentence>(indexCovered.keySet());
        for (int i = 0; i < keySet.size(); i++) {
            sentence = keySet.get(i);
            Collection<TextClassificationUnit> collection = indexCovered.get(sentence);
            if (collection.contains(aClassificationUnit)) {
                // we found the sentence with the current TextClassificationUnit
                break;
            }
            sentence = null;
        }

        List<Token> tokens = JCasUtil.selectCovered(aView, Token.class, sentence);

        return tokens;
    }

}
