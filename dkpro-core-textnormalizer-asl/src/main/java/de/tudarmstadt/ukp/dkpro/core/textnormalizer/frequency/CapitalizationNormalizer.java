/*
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
 */
package de.tudarmstadt.ukp.dkpro.core.textnormalizer.frequency;

import static org.apache.uima.fit.util.JCasUtil.select;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.provider.FrequencyCountProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.transform.JCasTransformerChangeBased_ImplBase;

/**
 * Takes a text and replaces wrong capitalization
 */
@ResourceMetaData(name="Capitalization Normalizer")
@TypeCapability(
        inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class CapitalizationNormalizer
    extends JCasTransformerChangeBased_ImplBase
{
    public static final String FREQUENCY_PROVIDER = "FrequencyProvider";
    @ExternalResource(key = FREQUENCY_PROVIDER, mandatory = true)
    protected FrequencyCountProvider frequencyProvider;

    @Override
    public void process(JCas aInput, JCas aOutput)
        throws AnalysisEngineProcessException
    {
        // Pattern for repetitions of one character more than 2 times
        Pattern moreThanTwoCapitalLetter = Pattern.compile("[A-Z].*[A-Z]");

        try {
            for (Token token : select(aInput, Token.class)) {
                if (moreThanTwoCapitalLetter.matcher(token.getCoveredText()).find()) {
                    String origTokenText = token.getCoveredText();
                    String replacement = "";
                    replacement = getBestReplacement(origTokenText);
                    replace(token.getBegin(), token.getEnd(), replacement);
                }
            }
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
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
