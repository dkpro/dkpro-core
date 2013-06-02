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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.frequency.resources;

import java.util.Map;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.FrequencyCountResourceBase;
import de.tudarmstadt.ukp.dkpro.core.api.frequency.provider.FrequencyCountProvider;
import de.tudarmstadt.ukp.dkpro.core.frequency.BerkeleyLMProvider;

/**
 * External resource wrapper for the Berkeley LM frequency count provider.
 * 
 * @author zesch
 * 
 */
public final class BerkeleyLMFrequencyCountProvider
	extends FrequencyCountResourceBase
	implements FrequencyCountProvider
{

	public static final String PARAM_BINARY = "BinaryFile";
	@ConfigurationParameter(name = PARAM_BINARY, mandatory = true)
	protected String file;
	
	public static final String PARAM_PROVIDER_LANGUAGE = "ProviderLanguage";
	@ConfigurationParameter(name = PARAM_PROVIDER_LANGUAGE, mandatory = true)
	protected String language;

	@Override
	public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
		throws ResourceInitializationException
	{
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}

		try {
			provider = new BerkeleyLMProvider(file, language);
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}

		return true;
	}
}