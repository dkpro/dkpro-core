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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = { "relations" })
public abstract class BioCAnnotationContainer
    extends BioCObject
{
    private List<BioCRelation> relations;

    @XmlElement(name = "relation")
    public List<BioCRelation> getRelations()
    {
        return relations;
    }

    public void setRelations(List<BioCRelation> aRelations)
    {
        relations = aRelations;
    }

    public void addRelation(BioCRelation aRelation)
    {
        if (relations == null) {
            relations = new ArrayList<>();
        }
        relations.add(aRelation);
    }

    // Methods for annotations - subclasses must implement
    public abstract List<BioCAnnotation> getAnnotations();

    public abstract void setAnnotations(List<BioCAnnotation> aAnnotations);

    public abstract void addAnnotation(BioCAnnotation aAnnotation);
}
