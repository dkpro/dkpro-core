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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RelationParam
{
    private static final Pattern PATTERN = Pattern.compile(
            "(?<TYPE>[a-zA-Z_][a-zA-Z0-9_\\-.]+):" +
            "(?<ARG1>[a-zA-Z][a-zA-Z0-9]+):" +
            "(?<ARG2>[a-zA-Z][a-zA-Z0-9]+)");
    
    private static final String TYPE = "TYPE";
    private static final String ARG1 = "ARG1";
    private static final String ARG2 = "ARG2";

    private final String type;
    private final String arg1;
    private final String arg2;
    
    public RelationParam(String aType, String aArg1, String aArg2)
    {
        super();
        type = aType;
        arg1 = aArg1;
        arg2 = aArg2;
    }
    
    public String getType()
    {
        return type;
    }

    public String getArg1()
    {
        return arg1;
    }

    public String getArg2()
    {
        return arg2;
    }

    public static RelationParam parse(String aValue)
    {
        Matcher m = PATTERN.matcher(aValue);
        
        if (!m.matches()) {
            throw new IllegalArgumentException("Illegal relation parameter format [" + aValue + "]");
        }

        return new RelationParam(m.group(TYPE), m.group(ARG1), m.group(ARG2));
    }
}
