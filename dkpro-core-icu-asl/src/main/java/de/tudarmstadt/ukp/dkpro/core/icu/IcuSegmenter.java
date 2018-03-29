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
package de.tudarmstadt.ukp.dkpro.core.icu;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.LanguageCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import com.ibm.icu.text.BreakIterator;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * ICU segmenter.
 */
@ResourceMetaData(name = "ICU Segmenter")
@LanguageCapability({ "af", "ak", "am", "ar", "as", "az", "be", "bg", "bm", "bn", "bo", "br", "bs",
        "ca", "ce", "cs", "cy", "da", "de", "dz", "ee", "el", "en", "eo", "es", "et", "eu", "fa",
        "ff", "fi", "fo", "fr", "fy", "ga", "gd", "gl", "gu", "gv", "ha", "hi", "hr", "hu", "hy",
        "ig", "ii", "is", "it", "ja", "ka", "ki", "kk", "kl", "km", "kn", "ko", "ks", "kw", "ky",
        "lb", "lg", "ln", "lo", "lt", "lu", "lv", "mg", "mk", "ml", "mn", "mr", "ms", "mt", "my",
        "nb", "nd", "ne", "nl", "nn", "om", "or", "os", "pa", "pl", "ps", "pt", "qu", "rm", "rn",
        "ro", "ru", "rw", "se", "sg", "si", "sk", "sl", "sn", "so", "sq", "sr", "sv", "sw", "ta",
        "te", "tg", "th", "ti", "to", "tr", "tt", "ug", "uk", "ur", "uz", "vi", "wo", "yo", "zh",
        "zu" })
@TypeCapability(
    outputs = { 
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class IcuSegmenter
    extends SegmenterBase
{
    /**
     * Per default, the segmenter does not split off contractions like {@code John's} into two
     * tokens. When this parameter is enabled, a non-default token split is generated when an
     * apostrophe ({@code '}) is encountered.
     */
    public static final String PARAM_SPLIT_AT_APOSTROPHE = "splitAtApostrophe";
    @ConfigurationParameter(name = PARAM_SPLIT_AT_APOSTROPHE, mandatory = true, defaultValue = "false")
    private boolean splitAtApostrophe;

    @Override
    protected void process(JCas aJCas, String text, int zoneBegin)
        throws AnalysisEngineProcessException
    {
        BreakIterator bi = BreakIterator.getSentenceInstance(getLocale(aJCas));
        bi.setText(text);
        int last = bi.first() + zoneBegin;
        int cur = bi.next();
        while (cur != BreakIterator.DONE) {
            cur += zoneBegin;
            if (isWriteSentence()) {
                Annotation segment = createSentence(aJCas, last, cur);
                if (segment != null) {
                    processSentence(aJCas, segment.getCoveredText(), segment.getBegin());
                }
            }
            else {
                int[] span = new int[] { last, cur };
                trim(aJCas.getDocumentText(), span);
                processSentence(aJCas, aJCas.getDocumentText().substring(span[0], span[1]),
                        span[0]);
            }
            last = cur;
            cur = bi.next();
        }
    }

    /**
     * Process the sentence to create tokens.
     */
    private void processSentence(JCas aJCas, String text, int zoneBegin)
    {
        BreakIterator bi = BreakIterator.getWordInstance(getLocale(aJCas));
        bi.setText(text);
        int last = bi.first() + zoneBegin;
        int cur = bi.next();
        while (cur != BreakIterator.DONE) {
            cur += zoneBegin;
            Token token = createToken(aJCas, last, cur);
            if (token != null) {
                if (splitAtApostrophe) {
                    int i = token.getText().indexOf("'");
                    if (i > 0) {
                        i += token.getBegin();
                        createToken(aJCas, i, token.getEnd());
                        token.setEnd(i);
                    }
                }
            }

            last = cur;
            cur = bi.next();
        }
    }
}
