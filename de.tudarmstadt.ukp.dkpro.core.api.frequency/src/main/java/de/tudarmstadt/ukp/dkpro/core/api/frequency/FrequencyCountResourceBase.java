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

import org.uimafit.component.Resource_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.provider.FrequencyCountProvider;

public abstract class FrequencyCountResourceBase
    extends Resource_ImplBase
    implements FrequencyCountProvider
{
    
    /**
     * Scales down the frequencies by the given factor.
     */
    public static final String PARAM_SCALE_DOWN_FACTOR = "ScaleDownFactor";
    @ConfigurationParameter(name = PARAM_SCALE_DOWN_FACTOR, mandatory = true, defaultValue = "1")
    protected String scaleDownFactor;

    protected FrequencyCountProvider provider;

    @Override
    public long getFrequency(String phrase)
        throws Exception
    {
        return provider.getFrequency(phrase);
    }

    @Override
    public double getProbability(String phrase)
        throws Exception
    {
        return provider.getProbability(phrase);
    }

    @Override
    public double getLogProbability(String phrase)
        throws Exception
    {
        return provider.getLogProbability(phrase);
    }
    
    @Override
    public long getNrOfTokens()
        throws Exception
    {
        return provider.getNrOfTokens();
    }    

    @Override
    public long getNrOfNgrams(int n)
        throws Exception
    {
        return provider.getNrOfNgrams(n);
    }
    
    @Override
    public long getNrOfDistinctNgrams(int n)
        throws Exception
    {
        return provider.getNrOfDistinctNgrams(n);
    }
}