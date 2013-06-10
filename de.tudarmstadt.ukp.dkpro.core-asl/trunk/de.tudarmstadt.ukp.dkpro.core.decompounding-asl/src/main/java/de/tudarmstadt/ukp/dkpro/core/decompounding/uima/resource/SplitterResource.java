package de.tudarmstadt.ukp.dkpro.core.decompounding.uima.resource;

import java.util.Map;

import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.uimafit.component.Resource_ImplBase;
import org.uimafit.descriptor.ExternalResource;

import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.SplitterAlgorithm;

public abstract class SplitterResource
    extends Resource_ImplBase
    implements SplitterAlgorithm
{

    // Dictionary resource
    public static final String PARAM_DICT_RESOURCE = "dictionaryResource";
    @ExternalResource(key = PARAM_DICT_RESOURCE)
    private SharedDictionary dictResource;

    // Linking morphemes resource
    public static final String PARAM_MORPHEME_RESOURCE = "linkingMorphemeResource";
    @ExternalResource(key = PARAM_MORPHEME_RESOURCE)
    private SharedLinkingMorphemes morphemesResource;

    protected SplitterAlgorithm splitter;

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }
        return true;
    }

    @Override
    public void afterResourcesInitialized(){
        splitter.setDictionary(dictResource.getDictionary());
        splitter.setLinkingMorphemes(morphemesResource.getLinkingMorphemes());
    }

}
