/*
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
 */
package org.dkpro.core.api.io;

import java.util.Collection;
import java.util.Map;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.util.CasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * Converts a chunk annotations into IOB-style 
 */
public class IobEncoder
{  
    private Int2ObjectMap<String> iobBeginMap;
    private Int2ObjectMap<String> iobInsideMap;

    private boolean iob1 = false;

    public IobEncoder(CAS aCas, Type aType, Feature aValueFeature)
    {
        this(aCas, aType, aValueFeature, false);
    }

    public IobEncoder(CAS aCas, Type aType, Feature aValueFeature, boolean aIob1)
    {
        iob1 = aIob1;
        
        // fill map for whole JCas in order to efficiently encode IOB
        iobBeginMap = new Int2ObjectOpenHashMap<String>();
        iobInsideMap = new Int2ObjectOpenHashMap<String>();

        Map<AnnotationFS, Collection<AnnotationFS>> idx = CasUtil.indexCovered(aCas, aType,
                CasUtil.getType(aCas, Token.class));
        
        String lastValue = null;
        for (AnnotationFS chunk : CasUtil.select(aCas, aType)) {
            String value = chunk.getStringValue(aValueFeature);

            for (AnnotationFS token : idx.get(chunk)) {
                if (
                        token.getBegin() == chunk.getBegin() && 
                        (!iob1 || (lastValue != null && lastValue.equals(value)))
                ) {
                    iobBeginMap.put(token.getBegin(), value);
                }
                else {
                    iobInsideMap.put(token.getBegin(), value);
                }
            }
            
            lastValue = value;
        }
    }
    
    /**
     * Returns the IOB tag for a given token.
     * 
     * @param token
     *            a token.
     * @return the IOB tag.
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
