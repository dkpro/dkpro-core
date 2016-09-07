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

public class LxfEdge
    extends LxfAnnotatedObject
{
    private String[] from;
    private String[] to;

    public LxfEdge()
    {
        // Required for Jackson
    }

    public LxfEdge(String aOrigin, int aIndex, int aRank, LxfNode aFrom, LxfNode aTo)
    {
        setOrigin(aOrigin);
        setIndex(aIndex);
        setRank(aRank);
        // We assume here that the ID derives directly from origin, index and rank!
        setId(String.format("%s-e%d@%d", getOrigin(), getIndex() + 1, getRank() + 1));
        setFrom(new String[] { aFrom.getOrigin(), aFrom.getId() });
        setTo(new String[] { aTo.getOrigin(), aTo.getId() });
        
        // TODO: REMOVE THE COMMENTED CODE BELOW
        // don't set this - linkage information is database internal
        // setFeature(FEAT_CLASS, LAYER_LINKAGE);
        // setFeature(FEAT_DOMAIN, aFrom.getLayer());
        // setFeature(FEAT_RANGE, aTo.getLayer());
    }
    
    public String[] getFrom()
    {
        return from;
    }

    public void setFrom(String[] aFrom)
    {
        from = aFrom;
    }

    public String[] getTo()
    {
        return to;
    }

    public void setTo(String[] aTo)
    {
        to = aTo;
    }
}
