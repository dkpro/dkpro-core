package de.tudarmstadt.ukp.dkpro.core.api.frequency;

import java.util.Map;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.uimafit.component.Resource_ImplBase;

import de.tudarmstadt.ukp.dkpro.teaching.frequency.FrequencyCountProvider;

public final class Web1TFrequencyCountProvider extends Resource_ImplBase implements FrequencyCountProvider
{

    private FrequencyCountProvider provider;
    
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