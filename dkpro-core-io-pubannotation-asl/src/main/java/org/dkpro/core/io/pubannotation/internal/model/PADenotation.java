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

// "denotations": [
//   {"id": "T1", "span": {"begin": 0, "end": 5}, "obj": "Protein"},
//   {"id": "T2", "span": {"begin": 42, "end": 47}, "obj": "Protein"},
//   {"id": "E1", "span": {"begin": 6, "end": 16}, "obj": "Expression"},
//   {"id": "E2", "span": {"begin": 31, "end": 38}, "obj": "Regulation"}
// ]
public class PADenotation
{
    private String id;
    private PAOffsets span;
    private String obj;
    
    public PADenotation()
    {
        // Default constructor
    }

    public PADenotation(int aBegin, int aEnd)
    {
        span = new PAOffsets(aBegin, aEnd);
    }

    public String getId()
    {
        return id;
    }

    public void setId(String aId)
    {
        id = aId;
    }

    public PAOffsets getSpan()
    {
        return span;
    }

    public void setSpan(PAOffsets aSpan)
    {
        span = aSpan;
    }

    public String getObj()
    {
        return obj;
    }

    public void setObj(String aObj)
    {
        obj = aObj;
    }
}
