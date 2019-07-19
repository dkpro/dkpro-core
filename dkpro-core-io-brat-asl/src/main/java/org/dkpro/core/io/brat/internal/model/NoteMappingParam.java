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
package org.dkpro.core.io.brat.internal.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoteMappingParam
{
    public static final String FLAG_ANCHOR = "A";
    
    private static final Pattern PATTERN = Pattern.compile(
            "(?<TYPE>[a-zA-Z_][a-zA-Z0-9_\\-.]+)" +
            "[:](?<FEAT>[a-zA-Z][a-zA-Z0-9]+)");
    
    private static final String TYPE = "TYPE";
    private static final String FEAT = "FEAT";

    private final String type;
    private final String feature;
    
    public NoteMappingParam(String aType, String aFeature)
    {
        super();
        type = aType;
        feature = aFeature;
    }
    
    public String getType()
    {
        return type;
    }

    public String getFeature()
    {
        return feature;
    }

    public static NoteMappingParam parse(String aValue)
    {
        Matcher m = PATTERN.matcher(aValue);
        
        if (!m.matches()) {
            throw new IllegalArgumentException("Illegal note mapping parameter format [" + aValue + "]");
        }

        return new NoteMappingParam(m.group(TYPE), m.group(FEAT));
    }

}