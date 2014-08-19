/*******************************************************************************
 * Copyright 2011
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
package de.tudarmstadt.ukp.dkpro.core.frequency.resources;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.FrequencyCountResourceBase;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.provider.FrequencyCountProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.frequency.Web1TFileAccessProvider;

/**
 * External resource wrapper for the Web1T frequency count provider.
 * 
 * @author zesch
 *
 */
public final class Web1TFrequencyCountResource
    extends FrequencyCountResourceBase
{

    // Attention! Can only have String parameters in external resources.
    
    public static final String PARAM_MIN_NGRAM_LEVEL = "MinLevel";
    @ConfigurationParameter(name = PARAM_MIN_NGRAM_LEVEL, mandatory = true, defaultValue = "1")
    protected String minLevel;
    
    public static final String PARAM_MAX_NGRAM_LEVEL = "MaxLevel";
    @ConfigurationParameter(name = PARAM_MAX_NGRAM_LEVEL, mandatory = true, defaultValue = "5")
    protected String maxLevel;
    
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
    public static final String PARAM_INDEX_PATH = "indexPath";
    @ConfigurationParameter(name = PARAM_INDEX_PATH, mandatory = false)
    protected String indexPath;
    
    private CasConfigurableProviderBase<File> web1TFolderProvider;
    
    @Override
	public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
		throws ResourceInitializationException
	{
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}

        web1TFolderProvider = new ModelProviderBase<File>()
        {
            {
                setContextObject(Web1TFrequencyCountResource.this);

                setDefault(ARTIFACT_ID, "${groupId}.umlautnormalizer-model-normalizer-${language}-"
                        + "${variant}");
                setDefault(LOCATION, "classpath:de/tudarmstadt/ukp/dkpro/core/umlautnormalizer/lib/"
                        + "normalizer-${language}-${variant}.properties");
                setDefault(VARIANT, "default");
                setDefault(LANGUAGE, "de");

                setOverride(LOCATION, indexPath);
                setOverride(LANGUAGE, language);
                setOverride(VARIANT, variant);
            }

            @Override
            protected File produceResource(URL aUrl) throws IOException
            {
                return ResourceUtils.getClasspathAsFolder(aUrl.toString(),true);
            }
        };

		return true;
	}
    
    protected void initializeProvider() throws ResourceInitializationException{
        try{
            web1TFolderProvider.configure();
            provider = new Web1TFileAccessProvider(
                    web1TFolderProvider.getResource(),
                    new Integer(minLevel),
                    new Integer(maxLevel)
            );
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
        ((FrequencyCountProviderBase) provider).setScaleDownFactor(Integer.parseInt(this.scaleDownFactor));
    }
    
}