/*
 * Copyright 2017
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
 */
package de.tudarmstadt.ukp.dkpro.core.api.resources;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MappingUtils
{
    public static final String META_TYPE_BASE = "__META_TYPE_BASE__";
    public static final String META_REDIRECT = "__META_REDIRECT__";
    public static final String META_OVERRIDE = "__META_OVERRIDE__";
    public static final String META_SOURCE_URL = "__META_SOURCE_URL__";

    public static Set<String> stripMetadata(Set<String> aKeys)
    {
        Set<String> tags = new LinkedHashSet<String>(aKeys);
        List<String> toRemove = new ArrayList<>();
        for (String tag : tags) {
            if (tag.startsWith(META_OVERRIDE) || tag.startsWith(META_SOURCE_URL)) {
                toRemove.add(tag);
            }
        }
        tags.remove(META_TYPE_BASE);
        tags.remove(META_REDIRECT);
        tags.removeAll(toRemove);
        return tags;
    }
}
