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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Aggregator providing a unified access to multiple {@link Tagset}s. It is
 * assumed that for each layer only a single deletate provider is responsible.
 */
public class AggregateTagset
    extends TagsetBase
{
    private List<Tagset> delegates = new ArrayList<Tagset>();

    public AggregateTagset()
    {
        // Nothing to do.
    }

    public AggregateTagset(Tagset... aProviders)
    {
        for (Tagset p : aProviders) {
            add(p);
        }
    }

    public void add(Tagset aProvider)
    {
        delegates.add(aProvider);
    }

    private Tagset getTagsetDelegate(String aLayerName)
    {
        for (Tagset provider : delegates) {
            if (provider.getLayers().containsKey(aLayerName)) {
                return provider;
            }
        }

        return null;
    }

    @Override
    public Map<String, String> getLayers()
    {
        Map<String, String> tagsets = new HashMap<String, String>();

        for (Tagset provider : delegates) {
            tagsets.putAll(provider.getLayers());
        }

        return tagsets;
    }

    @Override
    public Set<String> listTags(String aLayerName, String aTagsetName)
    {
        return getTagsetDelegate(aLayerName).listTags(aLayerName, aTagsetName);
    }
}
