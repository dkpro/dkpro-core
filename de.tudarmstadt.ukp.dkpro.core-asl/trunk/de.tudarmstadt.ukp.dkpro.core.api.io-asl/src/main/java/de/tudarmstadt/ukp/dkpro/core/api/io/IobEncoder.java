/*******************************************************************************
 * Copyright 2013
 * Ubiquitous Knowledge Processing (UKP) Lab and FG Language Technology
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.api.io;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk.Chunk;

/**
 * Converts a chunk annotations into IOB-style 
 * 
 * @author Torsten Zesch
 *
 */
public class IobEncoder
{
    private JCas jcas;
    private Map<Integer, String> iobBeginMap;
    private Map<Integer, String> iobInsideMap;

    public IobEncoder(JCas aJCas)
    {
        super();
        jcas = aJCas;
        
        // fill map for whole jcas in order to efficiently encode IOB
        iobBeginMap = new HashMap<Integer, String>();
        iobInsideMap = new HashMap<Integer, String>();

        for (Chunk chunk : JCasUtil.select(jcas, Chunk.class)) {
            for (Token token : JCasUtil.selectCovered(jcas, Token.class, chunk)) {
                if (token.getBegin() == chunk.getBegin()) {
                    iobBeginMap.put(token.getBegin(), chunk.getChunkValue());
                }
                else {
                    iobInsideMap.put(token.getBegin(), chunk.getChunkValue());
                }
            }
        }
    }
    
    /**
     * Returns the IOB tag for a given token.
     * @param annotation
     * @return
     */
    public String encode(Token token)
    {
        if (iobBeginMap.containsKey(token.getBegin())) {
            return "B-" + iobBeginMap.get(token.getBegin());
        }
        
        if (iobInsideMap.containsKey(token.getBegin())) {
            return "I-" + iobInsideMap.get(token.getBegin());
        }
        
        return "O";
    }
}