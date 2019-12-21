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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
public abstract class BioCObject_ImplBase
{
    private boolean infonMapSynced = false;
    private Map<String, String> infonMap = new LinkedHashMap<>();
    private List<BioCInfon> infons = new ArrayList<>();

    public String getInfonValue(String aKey)
    {
        syncMap();
        return infonMap.get(aKey);
    }

    public void putInfon(String aKey, String aValue)
    {
        infonMapSynced = false;
        infons.add(new BioCInfon(aKey, aValue));
    }

    /**
     * key-value pairs. Can record essentially arbitrary information. "type" will be a particular
     * common key in the major sub elements below. For PubMed references, passage "type" might
     * signal "title" or "abstract". For annotations, it might indicate "noun phrase", "gene", or
     * "disease". In the programming language data structures, infons are typically represented as a
     * map from strings to strings. This means keys should be unique within each parent element.
     */
    @XmlElement(name = "infon")
    public List<BioCInfon> getInfons()
    {
        return infons;
    }

    public void setInfons(List<BioCInfon> aInfons)
    {
        infons = aInfons;
    }
    
    private void syncMap()
    {
        if (infonMapSynced) {
            return;
        }
        
        infonMap.clear();
        
        if (infons == null || infons.isEmpty()) {
            return;
        }

        infons.forEach(infon -> infonMap.put(infon.getKey(), infon.getValue()));
    }
}
