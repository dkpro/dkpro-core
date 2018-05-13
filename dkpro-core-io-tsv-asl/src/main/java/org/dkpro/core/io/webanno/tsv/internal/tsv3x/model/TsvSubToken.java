/*
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab and FG Language Technology
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */package org.dkpro.core.io.webanno.tsv.internal.tsv3x.model;

public class TsvSubToken extends TsvToken
{
    private final TsvToken token;
    private final int begin;
    private final int end;
    
    public TsvSubToken(TsvToken aToken, int aBegin, int aEnd)
    {
        super(aToken.getDocument(), aToken.getSentence(), aToken.getUimaToken(),
                aToken.getPosition());
        token = aToken;
        begin = aBegin;
        end = aEnd;
    }
    
    @Override
    public int getBegin()
    {
        return begin;
    }
    
    @Override
    public int getEnd()
    {
        return end;
    }
    
    @Override
    public String getId()
    {
        return String.format("%s.%d", token.getId(), token.getSubTokens().indexOf(this) + 1);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + begin;
        result = prime * result + end;
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
        TsvSubToken other = (TsvSubToken) obj;
        if (begin != other.begin) {
            return false;
        }
        if (end != other.end) {
            return false;
        }
        return true;
    }
}
