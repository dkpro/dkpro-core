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

import static org.apache.uima.util.Level.SEVERE;

import java.io.IOException;
import java.util.Iterator;

import org.apache.uima.fit.component.Resource_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;

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
        throws IOException
    {
        checkProvider();
        return provider.getFrequency(phrase);
    }

    @Override
    public double getProbability(String phrase)
        throws IOException
    {
        checkProvider();
        return provider.getProbability(phrase);
    }

    @Override
    public double getLogProbability(String phrase)
        throws IOException
    {
        checkProvider();
        return provider.getLogProbability(phrase);
    }
    
    @Override
    public long getNrOfTokens()
        throws IOException
    {
        checkProvider();
        return provider.getNrOfTokens();
    }    

    @Override
    public long getNrOfNgrams(int n)
        throws IOException
    {
        checkProvider();
        return provider.getNrOfNgrams(n);
    }
    
    @Override
    public long getNrOfDistinctNgrams(int n)
        throws IOException
    {
        checkProvider();
        return provider.getNrOfDistinctNgrams(n);
    }
    
    @Override
    public Iterator<String> getNgramIterator(int n)
        throws IOException
    {
        checkProvider();
        return provider.getNgramIterator(n);
    }

    @Override
    public String getLanguage()
        throws IOException
    {
        checkProvider();
        return provider.getLanguage();
    }

    @Override
    public String getID()
    {
        checkProvider();
        return provider.getID();
    }
    
    protected abstract void initializeProvider() throws Exception;
    
    private void checkProvider(){
        if(provider == null){
            try{
                initializeProvider();
            }
            catch (Exception e) {
                getLogger().log(SEVERE, "RuntimeException caught when initializing frequency count resource.");
                getLogger().log(SEVERE, e.getLocalizedMessage());
                getLogger().log(SEVERE, e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }
    
}