/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.udpipe.internal;

import java.util.Collection;

import cz.cuni.mff.ufal.udpipe.Sentence;
import cz.cuni.mff.ufal.udpipe.Word;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class DKPro2UDPipe
{
    public static void convert(Collection<Token> tokens, Sentence sentence)
    {
        for (Token t : tokens) {
            Word w = sentence.addWord(t.getCoveredText());
            if (t.getPos() != null) {
                w.setXpostag(t.getPosValue());
                w.setUpostag(t.getPos().getCoarseValue());
            }
            
            if (t.getLemma() != null) {
                w.setLemma(t.getLemmaValue());
            }
            
            if (t.getMorph() != null) {
                w.setFeats(t.getMorph().getValue());
            }
        }
    }
}
