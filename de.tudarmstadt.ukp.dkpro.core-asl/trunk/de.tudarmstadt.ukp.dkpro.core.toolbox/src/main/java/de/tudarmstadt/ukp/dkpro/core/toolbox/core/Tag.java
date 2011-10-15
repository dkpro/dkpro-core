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

import java.net.MalformedURLException;

import de.tudarmstadt.ukp.dkpro.core.toolbox.util.TagUtil;

public class Tag
{

    private String tag;
    private String simplifiedTag;

    public Tag(String tag, String language) throws MalformedURLException
    {
        super();
        this.tag = tag;
        this.simplifiedTag = TagUtil.getSimplifiedTag(tag, language);
    }
    
    public String getTag()
    {
        return tag;
    }
    public void setTag(String tag)
    {
        this.tag = tag;
    }
    public String getSimplifiedTag()
    {
        return simplifiedTag;
    }
    public void setSimplifiedTag(String simplifiedTag)
    {
        this.simplifiedTag = simplifiedTag;
    }

    @Override
    public String toString()
    {
        return this.tag + "/" + this.simplifiedTag; 
    }
}