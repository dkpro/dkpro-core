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
@XmlType(propOrder = { "offset", "text", "annotations" })
public class BioCSentence
    extends BioCAnnotationContainer
{
    private int offset;
    private String text;
    private List<BioCAnnotation> annotations;

    @XmlElement(name = "offset")
    public int getOffset()
    {
        return offset;
    }

    public void setOffset(int aOffset)
    {
        offset = aOffset;
    }

    @XmlElement(name = "text")
    public String getText()
    {
        return text;
    }

    public void setText(String aText)
    {
        text = aText;
    }

    @Override
    @XmlElement(name = "annotation")
    public List<BioCAnnotation> getAnnotations()
    {
        return annotations;
    }

    @Override
    public void setAnnotations(List<BioCAnnotation> aAnnotations)
    {
        annotations = aAnnotations;
    }

    @Override
    public void addAnnotation(BioCAnnotation aAnnotation)
    {
        if (annotations == null) {
            annotations = new ArrayList<>();
        }
        annotations.add(aAnnotation);
    }
}
