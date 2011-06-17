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
package de.tudarmstadt.ukp.dkpro.core.api.frequency;

import java.util.Map;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.uimafit.component.Resource_ImplBase;

import de.tudarmstadt.ukp.dkpro.teaching.frequency.FrequencyCountProvider;

/**
 * External resource wrapper for the Web1T frequency count provider.
 * 
 * @author zesch
 *
 */
public final class Web1TFrequencyCountProvider
	extends Resource_ImplBase
	implements FrequencyCountProvider
{
	private FrequencyCountProvider provider;

	@SuppressWarnings("unchecked")
    @Override
	public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
		throws ResourceInitializationException
	{
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}

		provider = new Web1TFrequencyCountProvider();

		return true;
	}

	@Override
	public long getFrequency(String phrase)
		throws Exception
	{
		return provider.getFrequency(phrase);
	}

	@Override
	public double getNormalizedFrequency(String phrase)
		throws Exception
	{
		return provider.getNormalizedFrequency(phrase);
	}
}