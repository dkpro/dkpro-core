/*
 * Copyright 2007-2018
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.core.lingpipe;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.sentences.IndoEuropeanSentenceModel;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.Tokenization;
import com.aliasi.tokenizer.TokenizerFactory;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterBase;

/**
 * LingPipe segmenter.
 */
@ResourceMetaData(name = "LingPipe Segmenter")
@TypeCapability(
        outputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public class LingPipeSegmenter
    extends SegmenterBase
{
    private SentenceModel sentenceModel;
    private TokenizerFactory tokenizerFactory;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        sentenceModel = new IndoEuropeanSentenceModel();
        tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
    }

    @Override
    protected void process(JCas aJCas, String aText, int aZoneBegin)
        throws AnalysisEngineProcessException
    {
        String text = aJCas.getDocumentText();
        
        // Generate tokens
        Tokenization toks = new Tokenization(text, tokenizerFactory);        
        for (int n = 0; n < toks.numTokens(); n++) {
            createToken(aJCas, toks.tokenStart(n), toks.tokenEnd(n));
        }
        
        // Generate sentences
        int[] sentenceBoundaries = sentenceModel.boundaryIndices(toks.tokens(), toks.whitespaces());
        if (sentenceBoundaries.length == 0) {
            if (toks.numTokens() > 0) {
                createSentence(aJCas, toks.tokenStart(0), toks.tokenEnd(toks.numTokens() - 1));
            }
        }
        else {
            int startToken = 0;
            for (int i = 0; i < sentenceBoundaries.length; ++i) {
                int endToken = sentenceBoundaries[i];
                createSentence(aJCas, toks.tokenStart(startToken), toks.tokenEnd(endToken));
                startToken = endToken + 1;
            }
            // If there are trailing tokens after the last sentence, turn them into a final sentence
            if (startToken < toks.numTokens()) {
                createSentence(aJCas, toks.tokenStart(startToken),
                        toks.tokenEnd(toks.numTokens() - 1));
            }
        }
    }
}
