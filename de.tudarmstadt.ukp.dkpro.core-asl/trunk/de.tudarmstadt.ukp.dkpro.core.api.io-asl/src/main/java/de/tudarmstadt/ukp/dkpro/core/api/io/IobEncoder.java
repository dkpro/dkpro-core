/*******************************************************************************
 * Copyright 2014
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

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.util.CasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Converts a chunk annotations into IOB-style 
 * 
 * @author Torsten Zesch
 *
 */
public class IobEncoder
{  
    private Map<Integer, String> iobBeginMap;
    private Map<Integer, String> iobInsideMap;

    public IobEncoder(CAS aCas, Type chunkType, Feature aChunkValueFeature)
    {
        super();
        
        // fill map for whole jcas in order to efficiently encode IOB
        iobBeginMap = new HashMap<Integer, String>();
        iobInsideMap = new HashMap<Integer, String>();

        for (AnnotationFS chunk : CasUtil.select(aCas, chunkType)) {
            String chunkValue = chunk.getStringValue(aChunkValueFeature);

            for (AnnotationFS token : CasUtil.selectCovered(aCas, CasUtil.getType(aCas, Token.class), chunk)) {

                if (token.getBegin() == chunk.getBegin()) {
                    iobBeginMap.put(token.getBegin(), chunkValue);
                }
                else {
                    iobInsideMap.put(token.getBegin(), chunkValue);
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