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
package org.dkpro.core.api.lexmorph.morph.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalysisMapping
{
    private String feature;
    private String value;
    private Matcher matcher;

    public AnalysisMapping(String aFeature, String aValue, String aMarker)
    {
        feature = aFeature;
        value = aValue;

        String marker = aMarker;
        if (!marker.startsWith("^")) {
            marker = ".*" + marker + ".*";
        }

        matcher = Pattern.compile(marker).matcher("");
    }

    public boolean matches(String aAnalysis)
    {
        matcher.reset(aAnalysis);
        return matcher.matches();
    }

    public String getFeature()
    {
        return feature;
    }

    public void setFeature(String aFeature)
    {
        feature = aFeature;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String aValue)
    {
        value = aValue;
    }

    public Matcher getMatcher()
    {
        return matcher;
    }

    public void setMatcher(Matcher aMatcher)
    {
        matcher = aMatcher;
    }
}
