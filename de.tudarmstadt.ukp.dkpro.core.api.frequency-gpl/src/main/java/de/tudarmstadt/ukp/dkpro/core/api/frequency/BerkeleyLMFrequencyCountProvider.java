/*******************************************************************************
 * Copyright 2010
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.api.frequency;

import java.util.Map;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.frequency.BerkeleyLMProvider;
import de.tudarmstadt.ukp.dkpro.core.frequency.FrequencyCountProvider;


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
    
    @SuppressWarnings("unchecked")
    @Override
	public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
		throws ResourceInitializationException
	{
		if (!super.initialize(aSpecifier, aAdditionalParams)) {
			return false;
		}

		try {
            provider = new BerkeleyLMProvider(file);
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }

		return true;
	}
}