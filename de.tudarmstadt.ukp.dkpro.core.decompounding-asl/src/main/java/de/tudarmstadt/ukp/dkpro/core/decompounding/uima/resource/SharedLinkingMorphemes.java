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

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.uimafit.component.Resource_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.LinkingMorphemes;

public class SharedLinkingMorphemes
    extends Resource_ImplBase
{

    public static final String PARAM_MORPHEMES_PATH = "morphemesPath";
    @ConfigurationParameter(name = PARAM_MORPHEMES_PATH, mandatory = false, defaultValue = "classpath:de/tudarmstadt/ukp/dkpro/core/decompounding/lib/spelling/de/igerman98/de_DE.linking")
    private String morphemesPath;

    private LinkingMorphemes morphemes;

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier,
            Map aAdditionalParams)
        throws ResourceInitializationException
    {

        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }
        try {
            URL url = ResourceUtils.resolveLocation(morphemesPath, this, null);

            morphemes = new LinkingMorphemes(url.openStream());
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        return true;

    }

    public LinkingMorphemes getLinkingMorphemes()
    {
        return morphemes;
    }

}
