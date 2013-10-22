package de.tudarmstadt.ukp.dkpro.core.umlautnormalizer;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.provider.FrequencyCountProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.frequency.Web1TFileAccessProvider;

public abstract class FrequencyNormalizer_ImplBase
    extends Normalizer_ImplBase
{

    public static final String PARAM_FREQUENCY_MODEL = "frequencyModel";
    @ConfigurationParameter(name = PARAM_FREQUENCY_MODEL, mandatory = true, defaultValue="classpath*:/de/tudarmstadt/ukp/dkpro/core/umlautnormalizer/lib/normalizer/de/default")
    private String frequencyModel;
    
    protected FrequencyCountProvider provider;


    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        try {
            File modelFolder = ResourceUtils
                    .getClasspathAsFolder(
                            frequencyModel,
                            true);
            provider = new Web1TFileAccessProvider(modelFolder, 1, 1);
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

}