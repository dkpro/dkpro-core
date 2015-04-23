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
package de.tudarmstadt.ukp.dkpro.core.flextag.features.character;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.tc.api.exception.TextClassificationException;
import de.tudarmstadt.ukp.dkpro.tc.api.features.ClassificationUnitFeatureExtractor;
import de.tudarmstadt.ukp.dkpro.tc.api.features.Feature;
import de.tudarmstadt.ukp.dkpro.tc.api.features.FeatureExtractorResource_ImplBase;
import de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationUnit;

public class ContainsComma
    extends FeatureExtractorResource_ImplBase
    implements ClassificationUnitFeatureExtractor
{
    private static final String FEATURE_NAME = "containsComma";

    public List<Feature> extract(JCas aView, TextClassificationUnit aClassificationUnit)
        throws TextClassificationException
    {
        LogFactory.getLog(getClass()).debug("START");
        String token = aClassificationUnit.getCoveredText();
        Feature feature = new Feature(FEATURE_NAME, containsComma(token) ? 1 : 0);
        ArrayList<Feature> features = new ArrayList<Feature>();
        features.add(feature);
        LogFactory.getLog(getClass()).debug("FINISH");
        return features;
    }

    static boolean containsComma(String aToken)
    {
        return aToken.contains(",");
    }
}
