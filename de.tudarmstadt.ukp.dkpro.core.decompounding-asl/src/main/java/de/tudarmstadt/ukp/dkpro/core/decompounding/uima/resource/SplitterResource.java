/*******************************************************************************
 * Copyright 2010
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
 *******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.decompounding.uima.resource;

import java.util.Map;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.uimafit.component.Resource_ImplBase;
import org.uimafit.descriptor.ExternalResource;

import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.SplitterAlgorithm;

public abstract class SplitterResource
    extends Resource_ImplBase
    implements SplitterAlgorithm
{

    // Dictionary resource
    public static final String PARAM_DICT_RESOURCE = "dictionaryResource";
    @ExternalResource(key = PARAM_DICT_RESOURCE)
    private SharedDictionary dictResource;

    // Linking morphemes resource
    public static final String PARAM_MORPHEME_RESOURCE = "linkingMorphemeResource";
    @ExternalResource(key = PARAM_MORPHEME_RESOURCE)
    private SharedLinkingMorphemes morphemesResource;

    protected SplitterAlgorithm splitter;

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }
        return true;
    }

    @Override
    public void afterResourcesInitialized(){
        splitter.setDictionary(dictResource.getDictionary());
        splitter.setLinkingMorphemes(morphemesResource.getLinkingMorphemes());
    }

}
