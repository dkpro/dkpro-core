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
package de.tudarmstadt.ukp.dkpro.core.ixa;

import java.util.Properties;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterBase;
import eus.ixa.ixa.pipe.seg.RuleBasedSegmenter;
import eus.ixa.ixa.pipe.seg.SentenceSegmenter;
import eus.ixa.ixa.pipe.tok.RuleBasedTokenizer;
import eus.ixa.ixa.pipe.tok.Tokenizer;

/**
 * JTok segmenter.
 */
@TypeCapability(outputs = { 
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class IxaSegmenter
    extends SegmenterBase
{
    @Override
    protected void process(JCas aJCas, String text, int zoneBegin)
        throws AnalysisEngineProcessException
    {
        Properties properties = buildProperties(getLanguage(aJCas));
        SentenceSegmenter segmenter = new RuleBasedSegmenter(text, properties);
        Tokenizer tokenizer = new RuleBasedTokenizer(text, properties);
        
        String[] sentences = segmenter.segmentSentence();
        tokenizer.tokenize(sentences);
    }
    
    private Properties buildProperties(String aLanguage)
    {
        boolean untokenizable = false;
        boolean hardParagraph = false;
        
        Properties properties = new Properties();
        properties.setProperty("language", aLanguage);
        //properties.setProperty("normalize", normalize);
        properties.setProperty("untokenizable", untokenizable ? "yes" : "no");
        properties.setProperty("hardParagraph", hardParagraph ? "yes" : "no");
        return properties;
    }
}
