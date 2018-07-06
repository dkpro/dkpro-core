/*
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.eval.model;

public class Span<T>
{
    private String documentUri;
    private int begin;
    private int end;
    private T label;

    public Span(int aBegin, int aEnd, T aLabel)
    {
        this(null, aBegin, aEnd, aLabel);
    }
    
    public Span(String aDocumentUri, int aBegin, int aEnd, T aLabel)
    {
        documentUri = aDocumentUri;
        begin = aBegin;
        end = aEnd;
        label = aLabel;
    }

    public String getDocumentUri()
    {
        return documentUri;
    }

    public void setDocumentUri(String aDocumentUri)
    {
        documentUri = aDocumentUri;
    }

    public int getBegin()
    {
        return begin;
    }

    public void setBegin(int aBegin)
    {
        begin = aBegin;
    }

    public int getEnd()
    {
        return end;
    }

    public void setEnd(int aEnd)
    {
        end = aEnd;
    }

    public T getLabel()
    {
        return label;
    }

    public void setLabel(T aLabel)
    {
        label = aLabel;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + begin;
        result = prime * result + ((documentUri == null) ? 0 : documentUri.hashCode());
        result = prime * result + end;
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Span other = (Span) obj;
        if (begin != other.begin) {
            return false;
        }
        if (documentUri == null) {
            if (other.documentUri != null) {
                return false;
            }
        }
        else if (!documentUri.equals(other.documentUri)) {
            return false;
        }
        if (end != other.end) {
            return false;
        }
        if (label == null) {
            if (other.label != null) {
                return false;
            }
        }
        else if (!label.equals(other.label)) {
            return false;
        }
        return true;
    }
}
