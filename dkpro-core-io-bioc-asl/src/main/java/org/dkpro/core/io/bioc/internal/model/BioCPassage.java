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

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.dkpro.core.io.bioc.internal.BioCInfonConstants.KEY_TYPE;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * One portion of the document. For now PubMed documents have a title and an abstract. Structured
 * abstracts could have additional passages. For a full text document, passages could be sections
 * such as Introduction, Materials and Methods, or Conclusion. Another option would be paragraphs.
 * Passages impose a linear structure on the document. Further structure in the document can be
 * implied by the infon["type"] value.
 */
@XmlAccessorType(XmlAccessType.NONE)
public class BioCPassage
    extends BioCContainer_ImplBase
{
    private int offset;
    private String text;
    private List<BioCAnnotation> annotations = new ArrayList<>();
    private List<BioCSentence> sentences = new ArrayList<>();

    public BioCPassage()
    {
        // TODO Auto-generated constructor stub
    }
    
    public BioCPassage(String aType, int aOffset)
    {
        if (isNotBlank(aType)) {
            putInfon(KEY_TYPE, aType);
        }
        offset = aOffset;
        // text = aAnnotation.getCoveredText();
    }
    
    /**
     * Where the passage occurs in the parent document. Depending on the source corpus, this might
     * be a very relevant number. They should be sequential and identify a passage's position in the
     * document. Since pubmed is extracted from an XML file, the title has an offset of zero, while
     * the abstract is assumed to begin after the title and one space.
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
     * The original text of the passage.
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

    /**
     * Stand-off annotations.
     */
    @XmlElement(name = "annotation")
    public List<BioCAnnotation> getAnnotations()
    {
        return annotations;
    }

    public void setAnnotations(List<BioCAnnotation> aAnnotations)
    {
        annotations = aAnnotations;
    }

    public void addSentence(BioCSentence aSentence)
    {
        sentences.add(aSentence);
    }

    /**
     * Sentences of the passage.
     */
    @XmlElement(name = "sentence")
    public List<BioCSentence> getSentences()
    {
        return sentences;
    }

    public void setSentences(List<BioCSentence> aSentences)
    {
        sentences = aSentences;
    }
}
