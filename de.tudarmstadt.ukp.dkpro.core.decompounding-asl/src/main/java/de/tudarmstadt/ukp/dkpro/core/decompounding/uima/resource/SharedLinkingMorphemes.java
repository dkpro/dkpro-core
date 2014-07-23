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
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.apache.uima.fit.component.Resource_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.Dictionary;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.LinkingMorphemes;
import de.tudarmstadt.ukp.dkpro.core.decompounding.dictionary.SimpleDictionary;

public class SharedLinkingMorphemes
    extends Resource_ImplBase
{

    /**
     * Use this language instead of the default language.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    protected String language;

    /**
     * Override the default variant used to locate the model.
     */
    public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
    protected String variant;

    /**
     * Load the model from this location instead of locating the model automatically.
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    protected String modelLocation;

    private CasConfigurableProviderBase<LinkingMorphemes> modelProvider;
    private LinkingMorphemes morphemes;


    @Override
    public boolean initialize(ResourceSpecifier aSpecifier,
            Map aAdditionalParams)
        throws ResourceInitializationException
    {

        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        modelProvider = new ModelProviderBase<LinkingMorphemes>() {
            {
                setContextObject(SharedLinkingMorphemes.this);

                setDefault(ARTIFACT_ID, "${groupId}.decompounding-model-spelling-${language}-"
                        + "${variant}");
                setDefault(LOCATION, "classpath:de/tudarmstadt/ukp/dkpro/core/decompounding/lib/"
                        + "spelling-${language}-${variant}.properties");
                setDefault(VARIANT, "linking");
                setDefault(LANGUAGE, "de");

                setOverride(LOCATION, modelLocation);
                setOverride(LANGUAGE, language);
                setOverride(VARIANT, variant);
            }

            @Override
            protected LinkingMorphemes produceResource(InputStream aStream)
                throws Exception
            {
                return new LinkingMorphemes(aStream);
            }
        };

        return true;

    }

    public LinkingMorphemes getLinkingMorphemes() throws IOException
    {
        if(morphemes == null){
            modelProvider.configure();
            morphemes = modelProvider.getResource();
        }
        return morphemes;
    }

}
