package de.tudarmstadt.ukp.dkpro.core.decompounding.uima.resource;

import java.util.Map;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.uimafit.component.Resource_ImplBase;
import org.uimafit.descriptor.ExternalResource;

import de.tudarmstadt.ukp.dkpro.core.decompounding.ranking.Ranker;

public abstract class RankerResource
    extends Resource_ImplBase
    implements Ranker
{

    // Finder resource
    public static final String PARAM_FINDER_RESOURCE = "finderResource";
    @ExternalResource(key = PARAM_FINDER_RESOURCE)
    private SharedFinder finderResource;

    protected Ranker ranker;

    @SuppressWarnings({ "rawtypes" })
    @Override
    public boolean initialize(ResourceSpecifier aSpecifier,
            Map aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }
        return true;
    }

    @Override
    public void afterResourcesInitialized(){
        ranker.setFinder(finderResource.getFinder());
    }

}
