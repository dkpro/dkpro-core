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
package org.dkpro.core.api.datasets.internal;

import java.util.List;

import org.dkpro.core.api.datasets.ActionDescription;
import org.dkpro.core.api.datasets.ArtifactDescription;

public class ArtifactDescriptionImpl
    implements ArtifactDescription
{
    private String name;
    private String text;
    private String url;
    private String sha1;
    private boolean shared;
    private List<ActionDescription> actions;

    @Override
    public String getName()
    {
        return name;
    }

    public void setName(String aName)
    {
        name = aName;
    }

    @Override
    public String getText()
    {
        return text;
    }

    public void setText(String aText)
    {
        text = aText;
    }

    @Override
    public String getUrl()
    {
        return url;
    }

    public void setUrl(String aUrl)
    {
        url = aUrl;
    }

    @Override
    public String getSha1()
    {
        return sha1;
    }

    public void setSha1(String aSha1)
    {
        sha1 = aSha1;
    }

    @Override
    public List<ActionDescription> getActions()
    {
        return actions;
    }

    public void setActions(List<ActionDescription> aActions)
    {
        actions = aActions;
    }

    @Override
    public boolean isShared()
    {
        return shared;
    }

    public void setShared(boolean aShared)
    {
        shared = aShared;
    }
}
