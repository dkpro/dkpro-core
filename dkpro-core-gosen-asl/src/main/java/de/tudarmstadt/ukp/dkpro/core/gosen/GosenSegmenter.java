/*
 * Copyright 2016
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
package de.tudarmstadt.ukp.dkpro.core.gosen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.Messages;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterBase;
import net.java.sen.SenFactory;
import net.java.sen.StringTagger;
import net.java.sen.dictionary.Token;

/**
 * Segmenter for Japanese text based on GoSen.
 */
@TypeCapability(
        outputs = { 
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class GosenSegmenter
    extends SegmenterBase
{
    @Override
    protected void process(JCas aJCas, String text, int zoneBegin)
        throws AnalysisEngineProcessException
    {
        String language = getLanguage(aJCas);
        if (!"ja".equals(language)) {
            throw new AnalysisEngineProcessException(Messages.BUNDLE,
                    Messages.ERR_UNSUPPORTED_LANGUAGE, new String[] { language });
        }
        
        StringTagger tagger = SenFactory.getStringTagger(null);
        
        List<Token> tokens = new ArrayList<>();
        try {
            tokens = tagger.analyze(text, tokens);
        }
        catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        }
        
        int sentenceBegin = -1;
        for (Token t : tokens) {
            Annotation ut = createToken(aJCas, t.getStart() + zoneBegin, t.end() + zoneBegin);
            if (sentenceBegin == -1) {
                sentenceBegin = ut.getBegin();
            }
            
            // End of sentence?
            if ("。".equals(ut.getCoveredText())) {
                createSentence(aJCas, sentenceBegin, ut.getEnd());
                sentenceBegin = -1;
            }
        }
    }
}
