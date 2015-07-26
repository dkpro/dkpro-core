/*******************************************************************************
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.core.io.brat.internal.model;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class BratAttributeDecl
{
    private final String type;
    private final String targetType;
    private final Set<String> values = new LinkedHashSet<>();

    public BratAttributeDecl(String aType, String aTargetType)
    {
        type = aType;
        targetType = aTargetType;
    }

    public String getType()
    {
        return type;
    }
    
    public String getTargetType()
    {
        return targetType;
    }

    public Set<String> getValues()
    {
        return values;
    }
    
    public void addValue(String aValue)
    {
        if (aValue == null || aValue.length() == 0 || aValue.equals(",") ) {
            return;
        }
        
        values.add(aValue);
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(type);
        sb.append('\t');
        sb.append("Arg:");
        sb.append(targetType);
        if (!values.isEmpty()) {
            sb.append(", Value:");
            sb.append(StringUtils.join(values, "|"));
        }
        return sb.toString();
    }
}
