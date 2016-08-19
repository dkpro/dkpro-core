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

import static java.util.Collections.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.tudarmstadt.ukp.dkpro.core.datasets.ArtifactDescription;
import de.tudarmstadt.ukp.dkpro.core.datasets.DatasetDescription;
import de.tudarmstadt.ukp.dkpro.core.datasets.DatasetFactory;

public class DatasetDescriptionImpl
    implements DatasetDescription
{
    private DatasetFactory owner;
    
    /**
     * @deprecated to be superseded by groupId/datasetId/version/language/mediaType
     */
    @Deprecated
    private String id;

    private String groupId;
    private String datasetId;
    private String version;
    private String language;
    private String mediaType;

    private String name;
    private String attribution;
    private String description;
    private String url;

    private Map<String, List<String>> roles;
    private Map<String, ArtifactDescription> artifacts;

    public DatasetFactory getOwner()
    {
        return owner;
    }

    public void setOwner(DatasetFactory aOwner)
    {
        owner = aOwner;
    }

    /**
     * @deprecated to be superseded by groupId/datasetId/version/language/mediaType
     */
    @Deprecated
    @Override
    public String getId()
    {
        return id;
    }

    /**
     * @deprecated to be superseded by groupId/datasetId/version/language/mediaType
     */
    @Deprecated
    public void setId(String aId)
    {
        id = aId;
    }

    @Override
    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String aLanguage)
    {
        language = aLanguage;
    }

    @Override
    public Map<String, List<String>> getRoles()
    {
        return roles != null ? unmodifiableMap(roles) : emptyMap();
    }

    public void setRoles(Map<String, List<String>> aRoles)
    {
        roles = aRoles;
    }

    @Override
    public Map<String, ArtifactDescription> getArtifacts()
    {
        return artifacts != null ? unmodifiableMap(artifacts) : emptyMap();
    }

    public void setArtifacts(Map<String, ArtifactDescription> aArtifacts)
    {
        artifacts = aArtifacts;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String aName)
    {
        name = aName;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String aVersion)
    {
        version = aVersion;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String aDescription)
    {
        description = aDescription;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String aUrl)
    {
        url = aUrl;
    }

    public void putRoles(String aRole, List<String> aFiles)
    {
        if (roles == null) {
            roles = new LinkedHashMap<>();
        }
        roles.put(aRole, aFiles);
    }

    public String getAttribution()
    {
        return attribution;
    }

    public void setAttribution(String aAttribution)
    {
        attribution = aAttribution;
    }

    public String getMediaType()
    {
        return mediaType;
    }

    public void setMediaType(String aMediaType)
    {
        mediaType = aMediaType;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId(String aGroupId)
    {
        groupId = aGroupId;
    }

    public String getDatasetId()
    {
        return datasetId;
    }

    public void setDatasetId(String aDatasetId)
    {
        datasetId = aDatasetId;
    }
}
