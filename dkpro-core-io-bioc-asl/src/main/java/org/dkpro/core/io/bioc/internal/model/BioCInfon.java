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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * key-value pairs. Can record essentially arbitrary information. "type" will be a particular common
 * key in the major sub elements below. For PubMed references, passage "type" might signal "title"
 * or "abstract". For annotations, it might indicate "noun phrase", "gene", or "disease". In the
 * programming language data structures, infons are typically represented as a map from strings to
 * strings. This means keys should be unique within each parent element.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class BioCInfon
{
    private String key;
    private String value;

    public BioCInfon()
    {
        // Required for JAXB
    }

    public BioCInfon(String aKey, String aValue)
    {
        key = aKey;
        value = aValue;
    }

    @XmlAttribute(name = "key")
    public String getKey()
    {
        return key;
    }

    public void setKey(String aKey)
    {
        key = aKey;
    }

    @XmlValue
    public String getValue()
    {
        return value;
    }

    public void setValue(String aValue)
    {
        value = aValue;
    }
}
