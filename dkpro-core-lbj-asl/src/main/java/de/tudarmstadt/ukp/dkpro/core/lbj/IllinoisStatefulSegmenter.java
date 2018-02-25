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
package de.tudarmstadt.ukp.dkpro.core.lbj;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterBase;
import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.nlp.tokenizer.StatefulTokenizer;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer;
import edu.illinois.cs.cogcomp.nlp.tokenizer.Tokenizer.Tokenization;

/**
 * Illinois stateful segmenter.
 */
@ResourceMetaData(name = "Illinois CCG Stateful Segmenter")
@TypeCapability(outputs = { 
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class IllinoisStatefulSegmenter
    extends SegmenterBase
{
    private Tokenizer tokenizer;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        tokenizer = new StatefulTokenizer();
    }
    
    @Override
    protected void process(JCas aJCas, String text, int zoneBegin)
        throws AnalysisEngineProcessException
    {
        Tokenization tokens = tokenizer.tokenizeTextSpan(text);

        IntPair[] ts = tokens.getCharacterOffsets();
        for (IntPair t : ts) {
            createToken(aJCas, t.getFirst() + zoneBegin, t.getSecond() + zoneBegin);
        }

        int lastBegin = 0;
        for (int i : tokens.getSentenceEndTokenIndexes()) {
            createSentence(aJCas, ts[lastBegin].getFirst() + zoneBegin,
                    ts[i - 1].getSecond() + zoneBegin);
            lastBegin = i;
        }
        
        tokens.getSentenceEndTokenIndexes();
        

//        
//        for (Paragraph paragraph : paragraphs) {
//            if (writeParagraph) {
//                Annotation p = new de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph(
//                        aJCas, paragraph.getStartIndex(), paragraph.getEndIndex());
//                p.addToIndexes();
//            }
//            
//            for (TextUnit tu : paragraph.getTextUnits()) {
//                if (isWriteSentence()) {
//                    createSentence(aJCas, tu.getStartIndex(), tu.getEndIndex());
//                }
//                
//                for (Token t : tu.getTokens()) {
//                    if (isWriteToken()) {
//                        createToken(aJCas, t.getStartIndex(), t.getEndIndex());
//                    }
//                }
//            }
//        }
    }
}
