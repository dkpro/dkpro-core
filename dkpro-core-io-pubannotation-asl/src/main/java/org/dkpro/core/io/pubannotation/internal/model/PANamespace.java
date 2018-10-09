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

// "namespaces":[
//   {"prefix": "_base", "uri": "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?mode=Info\u0026id="}
// ]
public class PANamespace
{
    public static final String PREFIX_BASE = "_base";
    
    private String prefix;
    private String uri;
    
    public PANamespace()
    {
        // Default constructor
    }

    public String getPrefix()
    {
        return prefix;
    }

    public void setPrefix(String aPrefix)
    {
        prefix = aPrefix;
    }

    public String getUri()
    {
        return uri;
    }

    public void setUri(String aUri)
    {
        uri = aUri;
    }
}
