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
package org.dkpro.core.tokit;

import java.text.BreakIterator;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.LanguageCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.dkpro.core.api.segmentation.SegmenterBase;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.TrimUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * BreakIterator segmenter.
 */
@ResourceMetaData(name = "Java BreakIterator Segmenter")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@LanguageCapability({ "ar", "be", "bg", "ca", "cs", "da", "de", "el", "en", "es", "et", "fi", "fr",
        "ga", "hi", "hr", "hu", "is", "it", "ja", "ko", "lt", "lv", "mk", "ms", "mt", "nl", "no",
        "pl", "pt", "ro", "ru", "sk", "sl", "sq", "sr", "sv", "th", "tr", "uk", "vi", "zh" })
@TypeCapability(
    outputs = { 
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class BreakIteratorSegmenter
    extends SegmenterBase
{
    /**
     * Per default the Java {@link BreakIterator} does not split off contractions like
     * {@code John's} into two tokens. When this parameter is enabled, a non-default token split is
     * generated when an apostrophe ({@code '}) is encountered.
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
                TrimUtils.trim(aJCas.getDocumentText(), span);
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
