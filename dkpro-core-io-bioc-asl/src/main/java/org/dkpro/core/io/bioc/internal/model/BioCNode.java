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

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class BioCNode
{
    @JacksonXmlProperty(isAttribute = true, localName = "refid")
    private String refId;

    @JacksonXmlProperty(isAttribute = true, localName = "role")
    private String role;

    public BioCNode()
    {
        // Needed for serialization
    }

    public BioCNode(String aRefId, String aRole)
    {
        refId = aRefId;
        role = aRole;
    }

    public String getRefId()
    {
        return refId;
    }

    public void setRefId(String aRefId)
    {
        refId = aRefId;
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String aRole)
    {
        role = aRole;
    }
}
