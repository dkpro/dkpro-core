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
package de.tudarmstadt.ukp.dkpro.core.nlp4j.internal;

import java.util.List;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.emory.mathcs.nlp.component.template.node.NLPNode;

public class Uima2EmoryNlp
{
    public static NLPNode[] convertSentence(List<Token> aTokens)
    {
        NLPNode[] nodes = new NLPNode[aTokens.size()+1];
        
        nodes[0] = new NLPNode();
        nodes[0].toRoot();
        
        int i = 1;
        for (Token t : aTokens) {
            nodes[i] = new NLPNode(i, t.getCoveredText());
            nodes[i].setStartOffset(t.getBegin());
            nodes[i].setEndOffset(t.getEnd());
            
            if (t.getPos() != null) {
                nodes[i].setPartOfSpeechTag(t.getPos().getPosValue());
            }
            // FIXME should throw an exception if POS not set but is a required feature and
            // ignoreMissingFeatures is not enabled
            
            if (t.getLemma() != null) {
                nodes[i].setLemma(t.getLemma().getValue());
            }
            // FIXME should throw an exception if lemma not set but is a required feature and
            // ignoreMissingFeatures is not enabled
            
            i++;
        }
        
        EmoryNlpUtils.assignGlobalLexica(nodes);
        
        return nodes;
    }
}
