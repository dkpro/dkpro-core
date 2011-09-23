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

import org.uimafit.component.Resource_ImplBase;

import de.tudarmstadt.ukp.dkpro.core.frequency.FrequencyCountProvider;


public abstract class FrequencyCountResourceBase
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

    @Override
    public long getNrOfNgrams(int n)
        throws Exception
    {
        return provider.getNrOfNgrams(n);
    }    

}