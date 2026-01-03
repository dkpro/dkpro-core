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

import static org.dkpro.core.io.bioc.BioCComponent.E_DATE;
import static org.dkpro.core.io.bioc.BioCComponent.E_DOCUMENT;
import static org.dkpro.core.io.bioc.BioCComponent.E_KEY;
import static org.dkpro.core.io.bioc.BioCComponent.E_SOURCE;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "collection")
@JsonPropertyOrder({ "source", "date", "key", "infons", "documents" })
public class BioCCollection
    extends BioCObject
{
    @JacksonXmlProperty(localName = E_SOURCE)
    private String source;

    @JacksonXmlProperty(localName = E_DATE)
    private String date;

    @JacksonXmlProperty(localName = E_KEY)
    private String key;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = E_DOCUMENT)
    private List<BioCDocument> documents;

    public String getSource()
    {
        return source;
    }

    public void setSource(String aSource)
    {
        source = aSource;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String aDate)
    {
        date = aDate;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String aKey)
    {
        key = aKey;
    }

    public List<BioCDocument> getDocuments()
    {
        return documents;
    }

    public void setDocuments(List<BioCDocument> aDocuments)
    {
        documents = aDocuments;
    }

    public void addDocument(BioCDocument aDocument)
    {
        if (documents == null) {
            documents = new ArrayList<>();
        }

        documents.add(aDocument);
    }
}
