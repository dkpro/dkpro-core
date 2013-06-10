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

import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.Dictionary;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.LinkingMorphemes;
import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.AsvToolboxSplitterAlgorithm;
import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.DecompoundingTree;

public class AsvToolboxSplitterResource
    extends SplitterResource
{

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }
        splitter = new AsvToolboxSplitterAlgorithm();
        return true;
    }

    @Override
    public DecompoundingTree split(String aWord)
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
