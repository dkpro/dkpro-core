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

import static org.dkpro.core.io.bioc.internal.BioCInfonConstants.KEY_TYPE;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.uima.cas.text.AnnotationFS;

/**
 * Stand-off annotation.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class BioCAnnotation
    extends BioCObject_ImplBase
{
    private String id;
    private List<BioCLocation> locations = new ArrayList<>();
    private String text;

    public BioCAnnotation()
    {
        // Needed by JAXB
    }

    public BioCAnnotation(String aType, AnnotationFS aAnnotation)
    {
        putInfon(KEY_TYPE, aType);
        locations.add(new BioCLocation(aAnnotation.getBegin(),
                aAnnotation.getEnd() - aAnnotation.getBegin()));
        text = aAnnotation.getCoveredText();
    }

    /**
     * Used to refer to this annotation in relations.
     */
    @XmlAttribute(name = "id")
    public String getId()
    {
        return id;
    }

    public void setId(String aId)
    {
        id = aId;
    }

    /**
     * Location of the annotated text. Multiple locations indicate a multi-span annotation.
     */
    @XmlElement(name = "location")
    public List<BioCLocation> getLocations()
    {
        return locations;
    }

    public void setLocations(List<BioCLocation> aLocations)
    {
        locations = aLocations;
    }

    /**
     * Unless something else is defined one would be expect the annotated text. The length is
     * redundant in this case. Other uses for this text could be the normalized ID for a gene in a
     * gene database.
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

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.NO_FIELD_NAMES_STYLE).append("id", id)
                .append("text", text).append("locations", locations).toString();
    }
}
