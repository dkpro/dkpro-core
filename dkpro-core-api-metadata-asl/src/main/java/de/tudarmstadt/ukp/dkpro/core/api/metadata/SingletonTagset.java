/*******************************************************************************
 * Copyright 2013
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.api.metadata;

import static java.util.Collections.singletonMap;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public class SingletonTagset
    extends TagsetBase
{
    private String layer;
    private String tagset;
    private Set<String> tags;
    
    public SingletonTagset(Class<?> aLayer, String aTagsetName)
    {
        layer = aLayer.getName();
        tagset = aTagsetName;
        tags = new TreeSet<String>();
    }

    @Override
    public Map<String, String> getLayers()
    {
        return singletonMap(layer, tagset);
    }

    @Override
    public Set<String> listTags(String aLayer, String aTagsetName)
    {
        return tags;
    }

    public void add(String aTag)
    {
        tags.add(aTag);
    }

    public void addAll(Collection<String> aTags)
    {
        tags.addAll(aTags);
    }
    
    public void remove(String aTag)
    {
        tags.remove(aTag);
    }
    
    public void removeAll(SingletonTagset aOther)
    {
        Entry<String, String> entry = aOther.getLayers().entrySet().iterator().next();
        tags.removeAll(aOther.listTags(entry.getKey(), entry.getValue()));
    }
}
