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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation;
import de.tudarmstadt.ukp.dkpro.core.castransformation.alignment.AlignedString;
import de.tudarmstadt.ukp.dkpro.core.castransformation.alignment.ImmutableInterval;
import de.tudarmstadt.ukp.dkpro.core.castransformation.alignment.Interval;
import de.tudarmstadt.ukp.dkpro.core.textnormalizer.FrequencyNormalizer_ImplBase;

/**
 * Takes a text and replaces umlauts and sharp s
 * 
 * @author Sebastian Kneise
 * 
 */
@TypeCapability(
        inputs={
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token"},
        outputs={
                "de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation"})

public class UmlautSharpSNormalizer extends FrequencyNormalizer_ImplBase
{

    public static final String PARAM_MIN_FREQUENCY_THRESHOLD = "MinFrequencyThreshold";
    @ConfigurationParameter(name = PARAM_MIN_FREQUENCY_THRESHOLD, mandatory = true, defaultValue="100")
    private int minFrequencyThreshold;

    @SuppressWarnings("serial")
    public final static Map<String,String> replacementMap = new HashMap<String,String>() {{
        put("ae", "ä");
        put("Ae", "Ä");
        put("AE", "Ä");
        
        put("oe", "ö");
        put("Oe", "Ö");
        put("OE", "Ö");
        
        put("ue", "ü");
        put("Ue", "Ü");
        put("UE", "Ü");
        
        put("ss", "ß");
    }};


    @Override
    protected Map<Integer,List<SofaChangeAnnotation>> createSofaChangesMap(JCas jcas) 
    {
        int tokenPosition = 0;
        Map<Integer,List<SofaChangeAnnotation>> changesMap = new TreeMap<Integer,List<SofaChangeAnnotation>>();
        for (Token token : JCasUtil.select(jcas, Token.class)) 
        {
            String tokenString = token.getCoveredText();
            tokenPosition++;

            List<SofaChangeAnnotation> tokenChangeList = new ArrayList<SofaChangeAnnotation>();
            for (Map.Entry<String, String> entry : replacementMap.entrySet()) 
            {
                int currentIndex = 0;
                int index = 0;

                while ((index = tokenString.indexOf(entry.getKey(), currentIndex)) >= 0) 
                {
                    currentIndex = index + 1;

                    SofaChangeAnnotation sca = new SofaChangeAnnotation(jcas);
                    sca.setBegin(token.getBegin() + index);
                    sca.setEnd(token.getBegin() + index + entry.getKey().length());
                    sca.setOperation(OP_REPLACE);
                    sca.setValue(entry.getValue());

                    tokenChangeList.add(sca);
                }
            }
            changesMap.put(tokenPosition, tokenChangeList);
        }

        return changesMap;
    }

    @Override
    protected Map<Integer,Boolean> createTokenReplaceMap(JCas jcas, AlignedString as)  throws AnalysisEngineProcessException
    {

        Map<Integer,Boolean> tokenReplaceMap = new TreeMap<Integer,Boolean>();
        int i=0;
        for (Token token : JCasUtil.select(jcas, Token.class)) 
        {
            i++;

            String origToken = token.getCoveredText();

            Interval resolved = as.inverseResolve(new ImmutableInterval(token.getBegin(), token.getEnd()));
            String changedToken = as.get(resolved.getStart(), resolved.getEnd());

            if (origToken.equals(changedToken)) 
            {
                tokenReplaceMap.put(i, false);
                continue;
            }

            // check for frequency of original and changed token
            try {
                long freqOrigToken    = frequencyProvider.getFrequency(origToken);
                long freqChangedToken = frequencyProvider.getFrequency(changedToken);

                System.out.println(origToken + " - " + freqOrigToken);
                System.out.println(changedToken + " - " + freqChangedToken);

                // increase by one as cheap check against division by zero
                freqChangedToken++;

                // if absolut counts are too low, do not change
                if (freqOrigToken < minFrequencyThreshold && freqChangedToken < minFrequencyThreshold) 
                {
                    tokenReplaceMap.put(i, false);
                }
                else 
                {
                    if (((double) freqOrigToken / freqChangedToken) > 1) 
                    {
                        tokenReplaceMap.put(i, false);
                    }
                    else 
                    {
                        tokenReplaceMap.put(i, true);
                    }
                }
            }
            catch (Exception e) 
            {
                throw new AnalysisEngineProcessException(e);
            }
        }

        return tokenReplaceMap;
    }
}