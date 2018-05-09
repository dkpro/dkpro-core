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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LxfGraph
{
    private LxfText media;
    private List<LxfEdge> edges = new ArrayList<>();
    private List<LxfNode> nodes = new ArrayList<>();
    private List<LxfRegion> regions = new ArrayList<>();

    public LxfText getMedia()
    {
        return media;
    }

    public void setMedia(LxfText aMedia)
    {
        media = aMedia;
    }

    public List<LxfEdge> getEdges()
    {
        return edges;
    }

    public void setEdges(List<LxfEdge> aEdges)
    {
        edges = aEdges;
    }

    public List<LxfNode> getNodes()
    {
        return nodes;
    }

    public void setNodes(List<LxfNode> aNodes)
    {
        nodes = aNodes;
    }

    public List<LxfRegion> getRegions()
    {
        return regions;
    }

    public void setRegions(List<LxfRegion> aRegions)
    {
        regions = aRegions;
    }
    
    public LxfRegion getRegion(String[] aRegionId)
    {
        // We only match on the ID, not on the origin
        return regions.stream().filter(r -> r.getId().equals(aRegionId[1])).findFirst().get();
    }

    public void addNode(LxfNode aNode)
    {
        nodes.add(aNode);
    }

    public void addEdge(LxfEdge aEdge)
    {
        edges.add(aEdge);
    }

    public void addRegion(LxfRegion aRegion)
    {
        regions.add(aRegion);
    }

    public List<LxfEdge> getEdges(LxfNode aFrom, String aRange)
    {
        // For the domain, we only match on the ID, not on the origin
        return edges.stream().filter(e -> (aFrom.getId().equals(e.getFrom()[1])
                && aRange.equals(getNode(e.getTo()).getLayer()))).collect(Collectors.toList());
    }

    public List<LxfEdge> getEdges(String aDomain, LxfNode aTo)
    {
        // For the domain, we only match on the ID, not on the origin
        return edges.stream().filter(e -> aDomain.equals(getNode(e.getFrom()).getLayer())
                && aTo.getId().equals(e.getTo()[1])).collect(Collectors.toList());
    }

    public LxfNode getNode(String[] aOID)
    {
        // We only match on the ID, not on the origin
        Optional<LxfNode> node = nodes.stream().filter(n -> n.getId().equals(aOID[1])).findFirst();
        if (node.isPresent()) {
            return node.get();
        }
        else {
            throw new IllegalArgumentException("Unable to find node with ID " + asList(aOID));
        }
    }

    public LxfNode getNode(String aLayer, String aOrigin, int aIndex, int aRank)
    {
        return getNode(new String[] { aOrigin,
                String.format("%s-n%d@%d", aOrigin, aIndex + 1, aRank + 1) });
    }
}
