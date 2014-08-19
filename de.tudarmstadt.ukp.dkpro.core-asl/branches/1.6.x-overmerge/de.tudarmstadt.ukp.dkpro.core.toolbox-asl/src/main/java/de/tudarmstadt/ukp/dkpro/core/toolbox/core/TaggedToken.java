/*******************************************************************************
 * Copyright 2011
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.toolbox.core;

public class TaggedToken
{

    private String token;
    private Tag pos;
    
    public TaggedToken(String token, Tag pos)
    {
        super();
        this.token = token;
        this.pos = pos;
    }

    public String getToken()
    {
        return token;
    }
    public void setToken(String token)
    {
        this.token = token;
    }
    public Tag getPos()
    {
        return pos;
    }
    public void setPos(Tag pos)
    {
        this.pos = pos;
    }
    
    @Override
    public String toString()
    {
    	return token + " (" + pos.toString() + ")";
    }
}
