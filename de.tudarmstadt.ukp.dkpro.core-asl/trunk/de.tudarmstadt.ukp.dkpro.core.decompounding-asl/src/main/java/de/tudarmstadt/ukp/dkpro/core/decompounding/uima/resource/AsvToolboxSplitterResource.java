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

import static org.apache.uima.util.Level.SEVERE;

import java.io.IOException;
import java.util.Map;

import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.Dictionary;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.LinkingMorphemes;
import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.DecompoundingTree;

public class AsvToolboxSplitterResource
    extends SplitterResource
{

    /**
    *
    * This external resource wraps the patricia trie which shall be used by the ASV Toolbox splitter.
    *
    * */

   public static final String PARAM_PATRICIA_TRIES_RESOURCE = "patriciaTriesResource";
   @ExternalResource(key = PARAM_PATRICIA_TRIES_RESOURCE)
   private SharedPatriciaTries patriciaTriesResource;

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
    public void afterResourcesInitialized() throws RuntimeException {
        try {
            splitter = patriciaTriesResource.getSplitter();
        }
        catch (IOException e) {
            getLogger().log(SEVERE, "IOException caught when getting the patricia trie resource");
            getLogger().log(SEVERE, e.getLocalizedMessage());
            getLogger().log(SEVERE, e.getMessage());
            throw new RuntimeException(e);
        }
        catch (ResourceInitializationException e) {
            getLogger().log(SEVERE, "RuntimeException caught when getting the patrica trie resource");
            getLogger().log(SEVERE, e.getLocalizedMessage());
            getLogger().log(SEVERE, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public DecompoundingTree split(String aWord) throws ResourceInitializationException
    {
        return splitter.split(aWord);
    }

    @Override
    public void setDictionary(Dictionary aDict)
    {
        splitter.setDictionary(aDict);
    }

    @Override
    public void setLinkingMorphemes(LinkingMorphemes aMorphemes)
    {
        splitter.setLinkingMorphemes(aMorphemes);
    }

    @Override
    public void setMaximalTreeDepth(int aDepth)
    {
        splitter.setMaximalTreeDepth(aDepth);
    }

}
