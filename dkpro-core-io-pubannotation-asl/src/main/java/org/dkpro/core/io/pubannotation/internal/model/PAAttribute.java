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

// "attributes": [
//   {"id": "T1", "key": "lala", "value": "lolo"},
// ]
public class PAAttribute
{
    private String subject;
    private String key;
    private String value;
    
    public PAAttribute()
    {
        // Default constructor
    }

    public PAAttribute(String aSubject, String aKey, String aValue)
    {
        subject = aSubject;
        key = aKey;
        value = aValue;
    }

    public String getSubject()
    {
        return subject;
    }

    public void setSubject(String aSubject)
    {
        subject = aSubject;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String aKey)
    {
        key = aKey;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String aValue)
    {
        value = aValue;
    }
}
