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
package org.dkpro.core.api.datasets.internal;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

import java.util.Map;

import org.dkpro.core.api.datasets.ActionDescription;

public class ActionDescriptionImpl
    implements ActionDescription
{
    private String action;
    private Map<String, Object> configuration;

    @Override
    public String getAction()
    {
        return action;
    }

    public void setAction(String aName)
    {
        action = aName;
    }

    @Override
    public Map<String, Object> getConfiguration()
    {
        return configuration != null ? unmodifiableMap(configuration) : emptyMap();
    }

    public void setConfiguration(Map<String, Object> aConfiguration)
    {
        configuration = aConfiguration;
    }
}
