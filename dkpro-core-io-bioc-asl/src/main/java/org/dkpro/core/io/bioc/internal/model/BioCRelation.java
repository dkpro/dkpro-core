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
package org.dkpro.core.io.bioc.internal.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Relationship between multiple annotations.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class BioCRelation
    extends BioCObject_ImplBase
{
    private String id;
    private List<BioCNode> nodes;

    /**
     * Used to refer to this relation in other relationships.
     */
    @XmlElement(name = "id")
    public String getId()
    {
        return id;
    }

    public void setId(String aId)
    {
        id = aId;
    }

    @XmlElement(name = "node")
    public List<BioCNode> getNodes()
    {
        return nodes;
    }

    public void setNodes(List<BioCNode> aNodes)
    {
        nodes = aNodes;
    }

    
}
