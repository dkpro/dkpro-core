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
import java.util.Map;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.FrequencyCountResourceBase;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.provider.FrequencyCountProvider;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.provider.FrequencyCountProviderBase;
import de.tudarmstadt.ukp.dkpro.core.frequency.Web1TFrequencyCountProvider;

/**
 * External resource wrapper for the Web1T frequency count provider.
 * 
 * @author zesch
 *
 */
public final class Web1TFrequencyCountResource
	extends FrequencyCountResourceBase
	implements FrequencyCountProvider
{

    // Attention! Can only have String parameters in external resources.
    
    public static final String PARAM_MIN_NGRAM_LEVEL = "MinLevel";
    @ConfigurationParameter(name = PARAM_MIN_NGRAM_LEVEL, mandatory = true, defaultValue = "1")
    protected String minLevel;
    
    public static final String PARAM_MAX_NGRAM_LEVEL = "MaxLevel";
    @ConfigurationParameter(name = PARAM_MAX_NGRAM_LEVEL, mandatory = true, defaultValue = "5")
    protected String maxLevel;
    
    public static final String PARAM_INDEX_PATH = "IndexPath";
    @ConfigurationParameter(name=PARAM_INDEX_PATH, mandatory=false)
    private String indexPath;
    
    @SuppressWarnings("unchecked")
    @Override
	public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
		throws ResourceInitializationException
	{
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}

        try {
    		provider = new Web1TFrequencyCountProvider(
    		        new File(indexPath),
    		        new Integer(minLevel),
    		        new Integer(maxLevel)
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