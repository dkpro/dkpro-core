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

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * One sentence of the passage.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = { "infons", "offset", "text", "annotations", "relations" })
public class BioCSentence
    extends BioCContainer_ImplBase
{
    private int offset;
    private String text;
    private List<BioCAnnotation> annotations = new ArrayList<>();

    /**
     * A document offset to where the sentence begins in the passage. This value is the sum of the
     * passage offset and the local offset within the passage.
     */
    @XmlElement(name = "offset")
    public int getOffset()
    {
        return offset;
    }

    public void setOffset(int aOffset)
    {
        offset = aOffset;
    }

    /**
     * The original text of the sentence. (optional)
     */
    @XmlElement(name = "text")
    public String getText()
    {
        return text;
    }

    public void setText(String aText)
    {
        text = aText;
    }

    public void addAnnotation(BioCAnnotation aAnnotation)
    {
        annotations.add(aAnnotation);
    }
    
    /**
     * Stand-off annotation.
     */
    @XmlElement(name = "annotation")
    public List<BioCAnnotation> getAnnotations()
    {
        return annotations;
    }
    
    public List<BioCAnnotation> getAnnotationsMatching(String aKey, String aValue)
    {
        return annotations.stream()
                .filter(anno -> Objects.equals(anno.getInfonValue(aKey), aValue))
                .collect(toList());
    }

    public void setAnnotations(List<BioCAnnotation> aAnnotations)
    {
        annotations = aAnnotations;
    }
}
