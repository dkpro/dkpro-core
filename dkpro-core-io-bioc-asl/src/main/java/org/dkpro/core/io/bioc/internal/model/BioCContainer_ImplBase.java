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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
public abstract class BioCContainer_ImplBase
    extends BioCObject_ImplBase
{
    private List<BioCRelation> relations = new ArrayList<>();

    /**
     * Relationship between multiple annotations.
     */
    @XmlElement(name = "relation")
    public List<BioCRelation> getRelations()
    {
        return relations;
    }

    public void setRelations(List<BioCRelation> aRelations)
    {
        relations = aRelations;
    }
}
