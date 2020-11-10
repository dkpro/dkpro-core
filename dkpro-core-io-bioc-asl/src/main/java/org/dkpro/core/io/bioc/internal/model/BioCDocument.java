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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * A document in the collection. A single, complete stand-alone document as described by it's parent
 * source.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class BioCDocument
    extends BioCContainer_ImplBase
{
    private String id;
    private List<BioCPassage> passages = new ArrayList<>();
    
    /**
     * Typically, the id of the document in the parent source. Should at least be unique in the
     * collection.
     */
    @XmlElement(name = "id")
    public String getId()
    {
        return id;
    }

    public void setId(String aId)
    {
        id = aId;
    }

    public void addPassage(BioCPassage aPassage)
    {
        passages.add(aPassage);
    }

    /**
     * One portion of the document. For now PubMed documents have a title and an abstract.
     * Structured abstracts could have additional passages. For a full text document, passages could
     * be sections such as Introduction, Materials and Methods, or Conclusion. Another option would
     * be paragraphs. Passages impose a linear structure on the document. Further structure in the
     * document can be implied by the infon["type"] value.
     */
    @XmlElement(name = "passage")
    public List<BioCPassage> getPassages()
    {
        return passages;
    }

    public void setPassages(List<BioCPassage> aPassages)
    {
        passages = aPassages;
    }
}
