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

import java.io.IOException;
import java.util.Map;

import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.FrequencyCountResourceBase;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.provider.FrequencyCountProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.frequency.Web1TInMemoryProvider;

/**
 * External resource wrapper for the Web1T in memory frequency count provider.
 * 
 * @author zesch
 *
 */
public final class Web1TInMemoryFrequencyCountResource
	extends FrequencyCountResourceBase
{
    
    // Attention! Can only have String parameters in external resources.
    public static final String PARAM_MAX_NGRAM_LEVEL = "MaxLevel";
    @ConfigurationParameter(name = PARAM_MAX_NGRAM_LEVEL, mandatory = true, defaultValue = "5")
    protected String maxLevel;
    
    public static final String PARAM_MODEL_LOCATION= ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = true)
    protected String modelLocation;

    @SuppressWarnings("unchecked")
    @Override
	public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
		throws ResourceInitializationException
	{
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}

        try {
    		provider = new Web1TInMemoryProvider(
    		        modelLocation,
    		        Integer.parseInt(maxLevel)
    		);
    		
    		// FIXME should not be necessary to call that here - other implementations might forget to call it
            ((FrequencyCountProviderBase) provider).setScaleDownFactor(Integer.parseInt(this.scaleDownFactor));
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

		return true;
	}
}