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
package de.tudarmstadt.ukp.dkpro.core.api.metadata;

public class TagsetMetaData
{
    private String componentName;
    private String modelLocation;
    private String modelLanguage;
    private String modelVariant;
    private String modelVersion;
    private boolean input;

    public String getComponentName()
    {
        return componentName;
    }

    public void setComponentName(String aComponentName)
    {
        componentName = aComponentName;
    }

    public String getModelLocation()
    {
        return modelLocation;
    }

    public void setModelLocation(String aModelLocation)
    {
        modelLocation = aModelLocation;
    }

    public String getModelLanguage()
    {
        return modelLanguage;
    }

    public void setModelLanguage(String aModelLanguage)
    {
        modelLanguage = aModelLanguage;
    }

    public String getModelVariant()
    {
        return modelVariant;
    }

    public void setModelVariant(String aModelVariant)
    {
        modelVariant = aModelVariant;
    }

    public String getModelVersion()
    {
        return modelVersion;
    }

    public void setModelVersion(String aModelVersion)
    {
        modelVersion = aModelVersion;
    }

    public boolean isInput()
    {
        return input;
    }

    public void setInput(boolean aInput)
    {
        input = aInput;
    }
}
