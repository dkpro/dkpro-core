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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Group of documents, usually from a larger corpus. If a group of documents is from several
 * corpora, use several collections.
 */
@XmlRootElement(name = "collection")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = { "source", "date", "key", "infons", "documents" })
public class BioCCollection
    extends BioCObject_ImplBase
{
    private String source;
    private String date;
    private String key;
    private List<BioCDocument> documents;

    /**
     * Name of the source corpus from which the documents were selected.
     */
    @XmlElement(name = "source")
    public String getSource()
    {
        return source;
    }

    public void setSource(String aSource)
    {
        source = aSource;
    }

    /**
     * Date documents extracted from original source. Can be as simple as yyyymmdd or an ISO
     * timestamp.
     */
    @XmlElement(name = "date")
    public String getDate()
    {
        return date;
    }

    public void setDate(String aDate)
    {
        date = aDate;
    }

    /**
     * Separate file describing the types used and any other useful information about the data in
     * the file. For example, if a file includes part-of-speech tags, this file should describe the
     * part-of-speech tags used.
     */
    @XmlElement(name = "key")
    public String getKey()
    {
        return key;
    }

    public void setKey(String aKey)
    {
        key = aKey;
    }

    /**
     * A document in the collection. A single, complete stand-alone document as described by it's
     * parent source.
     */
    @XmlElement(name = "document")
    public List<BioCDocument> getDocuments()
    {
        return documents;
    }

    public void setDocuments(List<BioCDocument> aDocuments)
    {
        documents = aDocuments;
    }
}
