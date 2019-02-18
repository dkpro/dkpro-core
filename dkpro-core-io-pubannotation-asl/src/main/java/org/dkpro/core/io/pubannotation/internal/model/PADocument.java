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
package org.dkpro.core.io.pubannotation.internal.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @see <a href="http://www.pubannotation.org/docs/annotation-format/">PubAnnotation documentation</a>
 */
@JsonPropertyOrder({ "target", "sourcedb", "sourceid", "text", "project", "denotations",
        "relations", "modifications", "namespaces" })
public class PADocument
{
    @JsonInclude(Include.NON_NULL)
    private String target;

    @JsonInclude(Include.NON_NULL)
    private String project;
    
    @JsonProperty("sourcedb")
    @JsonInclude(Include.NON_NULL)
    private String sourceDb;
    
    @JsonProperty("sourceid")
    @JsonInclude(Include.NON_NULL)
    private String sourceId;
    
    private String text;
    
    @JsonInclude(Include.NON_EMPTY)
    private List<PADenotation> denotations = new ArrayList<>();
    
    @JsonInclude(Include.NON_EMPTY)
    private List<PARelation> relations = new ArrayList<>();

    @JsonInclude(Include.NON_EMPTY)
    private List<PAAttribute> attributes = new ArrayList<>();

    @JsonInclude(Include.NON_EMPTY)
    private List<PANamespace> namespaces = new ArrayList<>();
    
    @JsonInclude(Include.NON_EMPTY)
    private List<PAModification> modifications = new ArrayList<>();
    
    public PADocument()
    {
        // Default constructor
    }

    public String getTarget()
    {
        return target;
    }

    public void setTarget(String aTarget)
    {
        target = aTarget;
    }

    public String getProject()
    {
        return project;
    }

    public void setProject(String aProject)
    {
        project = aProject;
    }

    public String getSourceDb()
    {
        return sourceDb;
    }

    public void setSourceDb(String aSourcedb)
    {
        sourceDb = aSourcedb;
    }

    public String getSourceId()
    {
        return sourceId;
    }

    public void setSourceId(String aSourceid)
    {
        sourceId = aSourceid;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String aText)
    {
        text = aText;
    }

    public boolean addDenotation(PADenotation aE)
    {
        return denotations.add(aE);
    }

    public List<PADenotation> getDenotations()
    {
        return denotations;
    }

    public void setDenotations(List<PADenotation> aDenotations)
    {
        denotations = aDenotations;
    }

    public List<PARelation> getRelations()
    {
        return relations;
    }

    public void setRelations(List<PARelation> aRelations)
    {
        relations = aRelations;
    }

    public List<PANamespace> getNamespaces()
    {
        return namespaces;
    }

    public void setNamespaces(List<PANamespace> aNamespaces)
    {
        namespaces = aNamespaces;
    }

    public List<PAModification> getModifications()
    {
        return modifications;
    }

    public void setModifications(List<PAModification> aModifications)
    {
        modifications = aModifications;
    }
    
    public List<PAAttribute> getAttributes()
    {
        return attributes;
    }

    public void setAttributes(List<PAAttribute> aAttributes)
    {
        attributes = aAttributes;
    }
    
    public void addAttribute(PAAttribute aAttribute)
    {
        attributes.add(aAttribute);
    }

    public Optional<PANamespace> getNamespace(String aPrefix)
    {
        return namespaces.stream()
                .filter(it -> it.getPrefix().equals(aPrefix))
                .findFirst();
    }
}
