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
package org.dkpro.core.api.datasets.internal;

import java.util.List;

import org.dkpro.core.api.datasets.LicenseDescription;

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
