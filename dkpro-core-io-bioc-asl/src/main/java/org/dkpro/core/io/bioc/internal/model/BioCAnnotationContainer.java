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

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public abstract class BioCAnnotationContainer
    extends BioCObject
{
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "annotation")
    private List<BioCAnnotation> annotations;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "relation")
    private List<BioCRelation> relations;

    public List<BioCAnnotation> getAnnotations()
    {
        return annotations;
    }

    public void setAnnotations(List<BioCAnnotation> aAnnotations)
    {
        annotations = aAnnotations;
    }

    public void addAnnotation(BioCAnnotation aAnnotation)
    {
        if (annotations == null) {
            annotations = new ArrayList<>();
        }
        annotations.add(aAnnotation);
    }

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
}
