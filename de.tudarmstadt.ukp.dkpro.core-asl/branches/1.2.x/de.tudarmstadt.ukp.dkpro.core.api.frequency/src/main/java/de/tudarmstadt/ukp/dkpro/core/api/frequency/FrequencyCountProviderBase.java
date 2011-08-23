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

import de.tudarmstadt.ukp.dkpro.teaching.frequency.FrequencyCountProvider;

public abstract class FrequencyCountProviderBase
    extends Resource_ImplBase
    implements FrequencyCountProvider
{

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
}