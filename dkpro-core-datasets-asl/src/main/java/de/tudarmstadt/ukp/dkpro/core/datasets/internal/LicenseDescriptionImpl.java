/*
 * Copyright 2016
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
package de.tudarmstadt.ukp.dkpro.core.datasets.internal;

import java.util.List;

import de.tudarmstadt.ukp.dkpro.core.datasets.LicenseDescription;

public class LicenseDescriptionImpl
    implements LicenseDescription
{
    private String name;
    private String url;
    private String comment;
    private List<String> files;

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
    public String getUrl()
    {
        return url;
    }

    public void setUrl(String aUrl)
    {
        url = aUrl;
    }

    @Override
    public String getComment()
    {
        return comment;
    }

    public void setComment(String aComment)
    {
        comment = aComment;
    }

    public List<String> getFiles()
    {
        return files;
    }

    public void setFiles(List<String> aFiles)
    {
        files = aFiles;
    }
}
