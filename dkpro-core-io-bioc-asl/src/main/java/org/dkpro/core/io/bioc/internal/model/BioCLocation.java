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
import javax.xml.bind.annotation.XmlType;

/**
 * Location of the annotated text. Multiple locations indicate a multi-span annotation.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = { "offset", "length" })
public class BioCLocation
{
    private int offset;
    private int length;

    public BioCLocation()
    {
        // Needed by JAXB
    }
    
    public BioCLocation(int aBegin, int aLength)
    {
        offset = aBegin;
        length = aLength;
    }
    
    /**
     * Document offset to where the annotated text begins in the passage or sentence. The value is
     * the sum of the passage or sentence offset and the local offset within the passage or
     * sentence.
     */
    @XmlAttribute(name = "offset")
    public int getOffset()
    {
        return offset;
    }

    public void setOffset(int aOffset)
    {
        offset = aOffset;
    }

    /**
     * Length of the annotated text. While unlikely, this could be zero to describe an annotation
     * that belongs between two characters.
     */
    @XmlAttribute(name = "length")
    public int getLength()
    {
        return length;
    }

    public void setLength(int aLength)
    {
        length = aLength;
    }
    
    @Override
    public String toString()
    {
        return "[" + offset + "+" + length + "]";
    }
}
