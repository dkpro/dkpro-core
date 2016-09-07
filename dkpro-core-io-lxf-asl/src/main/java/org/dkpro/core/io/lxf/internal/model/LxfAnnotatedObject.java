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
package org.dkpro.core.io.lxf.internal.model;

import static java.util.Collections.singletonMap;
import static org.dkpro.core.io.lxf.internal.model.LxfVocabulary.FEAT_CLASS;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class LxfAnnotatedObject
    extends LxfObject
{
    private int rank;
    private Map<String, Map<String, String>> annotations;

    @JsonIgnore
    public String getLayer()
    {
        return getAnnotations().get(getOrigin()).get(FEAT_CLASS);
    }
    
    public String getFeature(String aName)
    {
        if (annotations != null) {             
            return annotations.get(getOrigin()).get(aName);
        }
        else {
            return null;
        }
    }
    
    public void setFeature(String aName, String aValue)
    {
        if (annotations == null) {
            annotations = singletonMap(getOrigin(), new LinkedHashMap<>());
        }
        
        Map<String, String> annos = annotations.get(getOrigin());
        annos.put(aName, aValue);
    }

    public int getRank()
    {
        return rank;
    }

    public void setRank(int aRank)
    {
        rank = aRank;
    }

    public Map<String, Map<String, String>> getAnnotations()
    {
        return annotations;
    }

    public void setAnnotations(Map<String, Map<String, String>> aAnnotations)
    {
        annotations = aAnnotations;
    }
}
