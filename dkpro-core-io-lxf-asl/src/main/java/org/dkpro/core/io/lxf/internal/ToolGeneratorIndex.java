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
package org.dkpro.core.io.lxf.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Returns the next available index for a tool. Used by DKPro2Lxf
 * 
 * @author Milen Kouylekov
 *
 */
public class ToolGeneratorIndex
{

    private Map<String, Integer> index;

    public ToolGeneratorIndex(Collection<String> values)
    {
        index = new HashMap<>();
        for (String v : values)
            index.put(v, 0);
    }

    public int nextIndex(String tool)
    {
        int value = index.get(tool);
        index.put(tool, value + 1);
        return value;
    }

}
