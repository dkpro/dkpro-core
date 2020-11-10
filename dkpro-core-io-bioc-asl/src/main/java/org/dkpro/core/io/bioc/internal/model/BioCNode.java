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

@XmlAccessorType(XmlAccessType.PROPERTY)
public class BioCNode
{
    private String refid;
    private String role;

    /**
     * Id of an annotated object or other relation.
     */
    @XmlAttribute(name = "refid")
    public String getRefId()
    {
        return refid;
    }

    public void setRefId(String aRefid)
    {
        refid = aRefid;
    }

    /**
     * Describes how the referenced annotated object or other relation participates in the current
     * relationship. Has a default value so can be left out if there is no meaningful value.
     */
    @XmlAttribute(name = "role")
    public String getRole()
    {
        return role;
    }

    public void setRole(String aRole)
    {
        role = aRole;
    }
}
