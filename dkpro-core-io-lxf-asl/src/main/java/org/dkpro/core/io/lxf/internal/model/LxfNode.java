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
package org.dkpro.core.io.lxf.internal.model;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.dkpro.core.io.lxf.internal.model.LxfVocabulary.FEAT_CLASS;

import java.util.List;

public class LxfNode
    extends LxfAnnotatedObject
{
    private List<List<String[]>> links;

    public LxfNode()
    {
        // Required for Jackson
    }
    
    public LxfNode(String aLayer, String aOrigin, int aIndex, int aRank)
    {
        this(aLayer, aOrigin, aIndex, aRank, null);
    }
    
    public LxfNode(String aLayer, String aOrigin, int aIndex, int aRank, LxfRegion aRegion)
    {
        setOrigin(aOrigin);
        setIndex(aIndex);
        setRank(aRank);
        // We assume here that the ID derives directly from origin, index and rank!
        setId(String.format("%s-n%d@%d", getOrigin(), getIndex() + 1, getRank() + 1));
        
        if (aRegion != null) {
            setLinks(asList(singletonList(new String[] { aRegion.getOrigin(), aRegion.getId() })));
        }
        
        setFeature(FEAT_CLASS, aLayer);
    }
    
    public List<List<String[]>> getLinks()
    {
        return links;
    }

    public void setLinks(List<List<String[]>> aLinks)
    {
        links = aLinks;
    }
}
