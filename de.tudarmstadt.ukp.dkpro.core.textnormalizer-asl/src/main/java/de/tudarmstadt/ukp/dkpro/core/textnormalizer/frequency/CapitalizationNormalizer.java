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

import static de.tudarmstadt.ukp.dkpro.core.castransformation.ApplyChangesAnnotator.OP_REPLACE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation;
import de.tudarmstadt.ukp.dkpro.core.castransformation.alignment.AlignedString;
import de.tudarmstadt.ukp.dkpro.core.castransformation.alignment.ImmutableInterval;
import de.tudarmstadt.ukp.dkpro.core.castransformation.alignment.Interval;

/**
 * Takes a text and replaces wrong capitalization
 * 
 * @author Sebastian Kneise
 * 
 */
@TypeCapability(
        inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" }, 
        outputs = { "de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation" })
public class CapitalizationNormalizer
    extends FrequencyNormalizer_ImplBase
{
    @Override
    protected Map<Integer, List<SofaChangeAnnotation>> createSofaChangesMap(JCas jcas)
    {
        Map<Integer, List<SofaChangeAnnotation>> changesMap = new TreeMap<Integer, List<SofaChangeAnnotation>>();
        int mapKey = 1;

        // Pattern for repetitions of one character more than 2 times
        Pattern moreThanTwoCapitalLetter = Pattern.compile("[A-Z].*[A-Z]");

        for (Token token : JCasUtil.select(jcas, Token.class)) {
            List<SofaChangeAnnotation> scaChangesList = new ArrayList<SofaChangeAnnotation>();

            if (moreThanTwoCapitalLetter.matcher(token.getCoveredText()).find()) {
                String origTokenText = token.getCoveredText();

                // System.out.println(
                // "SCA: " + token.getCoveredText()
                // // + "			Value: " + token.getValue()
                // + "			Position:" + token.getBegin());

                String replacement = "";
                try {
                    replacement = getBestReplacement(origTokenText);
                }
                catch (IOException e) {

                }

                SofaChangeAnnotation sca = new SofaChangeAnnotation(jcas);
                sca.setBegin(token.getBegin());
                sca.setEnd(token.getEnd());
                sca.setOperation(OP_REPLACE);
                sca.setValue(replacement);
                scaChangesList.add(sca);
            }
            changesMap.put(mapKey++, scaChangesList);
        }

        return changesMap;
    }

    @Override
    protected Map<Integer, Boolean> createTokenReplaceMap(JCas jcas, AlignedString as)
        throws AnalysisEngineProcessException
    {
        Map<Integer, Boolean> tokenReplaceMap = new TreeMap<Integer, Boolean>();

        int mapKey = 1;
        for (Token token : JCasUtil.select(jcas, Token.class)) {
            String origToken = token.getCoveredText();

            Interval resolved = as.inverseResolve(new ImmutableInterval(token.getBegin(), token
                    .getEnd()));
            String changedToken = as.get(resolved.getStart(), resolved.getEnd());

            if (origToken.equals(changedToken)) {
                tokenReplaceMap.put(mapKey++, false);
            }
            else {
                tokenReplaceMap.put(mapKey++, true);
            }
        }

        return tokenReplaceMap;
    }

    private String getBestReplacement(String origTokenText)
        throws IOException
    {
        List<String[]> all = new ArrayList<String[]>();

        for (int i = 0; i < origTokenText.length(); i++) {
            String letter = origTokenText.substring(i, i + 1);
            String[] entry = { letter.toLowerCase(), letter.toUpperCase() };
            all.add(entry);
        }

        List<String> allVariants = new ArrayList<String>();
        allVariants = permute(all, 0, allVariants, "");

        String currentCandidate = origTokenText;
        long currentFrequency = frequencyProvider.getFrequency(origTokenText);

        for (String s : allVariants) {
            if (frequencyProvider.getFrequency(s) > currentFrequency) {
                currentCandidate = s;
                currentFrequency = frequencyProvider.getFrequency(s);
            }
        }

        return currentCandidate;
    }

    private static List<String> permute(List<String[]> input, int depth, List<String> output,
            String current)
    {
        if (depth == input.size()) {
            output.add(current);
            return output;
        }

        for (int i = 0; i < input.get(depth).length; ++i) {
            permute(input, depth + 1, output, current + input.get(depth)[i]);
        }

        return output;
    }

}
