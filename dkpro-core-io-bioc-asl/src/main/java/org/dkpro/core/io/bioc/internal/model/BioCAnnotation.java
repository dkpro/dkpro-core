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

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(propOrder = { "locations", "text" })
public class BioCAnnotation
    extends BioCObject
{
    private String id;
    private List<BioCLocation> locations;
    private String text;

    @XmlAttribute(name = "id")
    public String getId()
    {
        return id;
    }

    public void setId(String aId)
    {
        id = aId;
    }

    @XmlElement(name = "location")
    public List<BioCLocation> getLocations()
    {
        return locations;
    }

    public void setLocations(List<BioCLocation> aLocations)
    {
        locations = aLocations;
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
}
