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

import com.fasterxml.jackson.annotation.JsonProperty;

// "attributions":[
// {"subj":string:denotation-id, "pred":string:attribute-name,"obj":string:attribute-value}
// ]
public class PAAttribute
{
    @JsonProperty("subj")
    private String subject;
    
    @JsonProperty("pred")
    private String predicate;
    
    @JsonProperty("obj")
    private String object;
    
    public PAAttribute()
    {
        // Default constructor
    }

    public PAAttribute(String aSubject, String aKey, String aValue)
    {
        subject = aSubject;
        predicate = aKey;
        object = aValue;
    }

    public String getSubject()
    {
        return subject;
    }

    public void setSubject(String aSubject)
    {
        subject = aSubject;
    }

    public String getPredicate()
    {
        return predicate;
    }

    public void setPredicate(String aKey)
    {
        predicate = aKey;
    }

    public String getObject()
    {
        return object;
    }

    public void setObject(String aValue)
    {
        object = aValue;
    }
}
