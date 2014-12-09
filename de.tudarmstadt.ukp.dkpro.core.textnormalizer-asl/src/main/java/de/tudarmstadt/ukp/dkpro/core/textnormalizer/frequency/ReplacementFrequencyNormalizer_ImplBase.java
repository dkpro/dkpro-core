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
package de.tudarmstadt.ukp.dkpro.core.textnormalizer.frequency;

import static org.apache.uima.fit.util.JCasUtil.select;

import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.provider.FrequencyCountProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.transform.JCasTransformerChangeBased_ImplBase;

/**
 * This base class is for all normalizers that need a frequency provider and replace based on a
 * list.
 */
@TypeCapability(
        inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public abstract class ReplacementFrequencyNormalizer_ImplBase
    extends JCasTransformerChangeBased_ImplBase
{
    public static final String FREQUENCY_PROVIDER = "FrequencyProvider";
    @ExternalResource(key = FREQUENCY_PROVIDER, mandatory = true)
    protected FrequencyCountProvider frequencyProvider;
    
    public static final String PARAM_MIN_FREQUENCY_THRESHOLD = "MinFrequencyThreshold";
    @ConfigurationParameter(name = PARAM_MIN_FREQUENCY_THRESHOLD, mandatory = true, defaultValue = "100")
    private int minFrequencyThreshold;

    protected Map<String, String> replacementMap;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        replacementMap = getReplacementMap();
    }

    protected abstract Map<String, String> getReplacementMap()
        throws ResourceInitializationException;

    @Override
    public void process(JCas aInput, JCas aOutput)
        throws AnalysisEngineProcessException
    {
        for (Token token : select(aInput, Token.class)) {
            String tokenString = token.getCoveredText();

            for (Map.Entry<String, String> entry : replacementMap.entrySet()) {
                int currentIndex = 0;
                int index = 0;

                while ((index = tokenString.indexOf(entry.getKey(), currentIndex)) >= 0) {
                    currentIndex = index + 1;
                    
                    String changedToken = tokenString.substring(0, index)
                            + entry.getValue()
                            + tokenString.substring(index + entry.getKey().length(),
                                    tokenString.length());

                    // check for frequency of original and changed token
                    try {
                        long freqOrigToken = frequencyProvider.getFrequency(tokenString);
                        long freqChangedToken = frequencyProvider.getFrequency(changedToken);

                        System.out.println(tokenString + " - " + freqOrigToken);
                        System.out.println(changedToken + " - " + freqChangedToken);

                        // if absolute counts of replacement are too low or zero, do not change
                        if (freqChangedToken == 0 || freqChangedToken < minFrequencyThreshold) {
                            // do nothing
                        }
                        else {
                            if (((double) freqOrigToken / freqChangedToken) > 1) {
                                // do nothing
                            }
                            else {
                                tokenString = changedToken;
                            }
                        }
                    }
                    catch (Exception e) {
                        throw new AnalysisEngineProcessException(e);
                    }                    
                }
            }
            replace(token.getBegin(), token.getEnd(), tokenString);
        }
    }
}