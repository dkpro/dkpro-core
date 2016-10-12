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
package org.dkpro.core.io.lxf.internal;

import java.util.HashMap;
import java.util.Map;

import org.dkpro.core.io.lxf.internal.model.LxfGraph;
import org.dkpro.core.io.lxf.internal.model.LxfNode;

/**
 * Iterator over nodes of certain annotation. Used by DKPro2Lxf
 * 
 * @author Milen Kouylekov
 *
 */
public class NodeIterator
{

    private LxfGraph graph;
    private Map<String, Integer> position;

    public NodeIterator(LxfGraph graph_)
    {
        graph = graph_;
        position = new HashMap<>();
    }

    public LxfNode next(String tool, String level)
    {
        String key = tool + "." + level;

        int current = 0;

        if (position.containsKey(key))
            current = position.get(key) + 1;

        for (int i = current; i < graph.getNodes().size(); i++) {
            LxfNode node = graph.getNodes().get(i);
            if (!node.getAnnotations().containsKey(tool))
                continue;
            if (!node.getAnnotations().get(tool).get("class").equals(level))
                continue;
            position.put(key, i);
            return node;
        }
        throw new RuntimeException(
                "Run out of nodes " + tool + " " + level + "! Must never happen!");
    }

}
