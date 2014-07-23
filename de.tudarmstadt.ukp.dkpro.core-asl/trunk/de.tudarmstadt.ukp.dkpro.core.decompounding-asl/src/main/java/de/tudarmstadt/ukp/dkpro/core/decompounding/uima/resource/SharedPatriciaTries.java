package de.tudarmstadt.ukp.dkpro.core.decompounding.uima.resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.uima.fit.component.Resource_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.AsvToolboxSplitterAlgorithm;
import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.SplitterAlgorithm;

public class SharedPatriciaTries
    extends Resource_ImplBase
{

    /**
     * Use this language instead of the default language.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    protected String language;

    /**
     * Override the default variant used to locate the model.
     */
    public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
    protected String variant;

    /**
     * Load the model from this location instead of locating the model automatically.
     */
    public static final String PARAM_KOMP_VVIC_TREE_LOCATION = "kompVVicTreeLocation";
    @ConfigurationParameter(name = PARAM_KOMP_VVIC_TREE_LOCATION, mandatory = false)
    protected String kompVVicTreeLocation;

    /**
     * Load the model from this location instead of locating the model automatically.
     */
    public static final String PARAM_KOMP_VHIC_TREE_LOCATION = "kompVHicTreeLocation";
    @ConfigurationParameter(name = PARAM_KOMP_VHIC_TREE_LOCATION, mandatory = false)
    protected String kompVHicTreeLocation;

    /**
     * Load the model from this location instead of locating the model automatically.
     */
    public static final String PARAM_GRF_EXT_TREE_LOCATION = "grfExtTreeLocation";
    @ConfigurationParameter(name = PARAM_GRF_EXT_TREE_LOCATION, mandatory = false)
    protected String grfExtTreeLocation;

    private CasConfigurableProviderBase<File> kompVVicProvider;
    private CasConfigurableProviderBase<File> kompVHicProvider;
    private CasConfigurableProviderBase<File> grfExtlProvider;

    private SplitterAlgorithm splitter;

    @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
        throws ResourceInitializationException
    {
        if (!super.initialize(aSpecifier, aAdditionalParams)) {
            return false;
        }

        kompVVicProvider = new ModelProviderBase<File>()
        {
            {
                setContextObject(SharedPatriciaTries.this);

                setDefault(ARTIFACT_ID, "${groupId}.decompounding-model-splitter-${language}-"
                        + "${variant}");
                setDefault(LOCATION, "classpath:de/tudarmstadt/ukp/dkpro/core/decompounding/lib/"
                        + "splitter-${language}-${variant}.properties");
                setDefault(VARIANT, "kompVVic");
                setDefault(LANGUAGE, "de");

                setOverride(LOCATION, kompVVicTreeLocation);
                setOverride(LANGUAGE, language);
                setOverride(VARIANT, variant);
            }

            @Override
            protected File produceResource(URL aUrl) throws IOException
            {
                return ResourceUtils.getUrlAsFile(aUrl, true);
            }
        };

        kompVHicProvider = new ModelProviderBase<File>()
        {
            {
                setContextObject(SharedPatriciaTries.this);

                setDefault(ARTIFACT_ID, "${groupId}.decompounding-model-splitter-${language}-"
                        + "${variant}");
                setDefault(LOCATION, "classpath:de/tudarmstadt/ukp/dkpro/core/decompounding/lib/"
                        + "splitter-${language}-${variant}.properties");
                setDefault(VARIANT, "kompVHic");
                setDefault(LANGUAGE, "de");

                setOverride(LOCATION, kompVHicTreeLocation);
                setOverride(LANGUAGE, language);
                setOverride(VARIANT, variant);
            }

            @Override
            protected File produceResource(URL aUrl) throws IOException
            {
                return ResourceUtils.getUrlAsFile(aUrl, true);
            }
        };

        grfExtlProvider = new ModelProviderBase<File>()
        {
            {
                setContextObject(SharedPatriciaTries.this);

                setDefault(ARTIFACT_ID, "${groupId}.decompounding-model-splitter-${language}-"
                        + "${variant}");
                setDefault(LOCATION, "classpath:de/tudarmstadt/ukp/dkpro/core/decompounding/lib/"
                        + "splitter-${language}-${variant}.properties");
                setDefault(VARIANT, "grfExt");
                setDefault(LANGUAGE, "de");

                setOverride(LOCATION, grfExtTreeLocation);
                setOverride(LANGUAGE, language);
                setOverride(VARIANT, variant);
            }

            @Override
            protected File produceResource(URL aUrl) throws IOException
            {
                return ResourceUtils.getUrlAsFile(aUrl, true);
            }
        };

        return true;

    }

    public SplitterAlgorithm getSplitter()
        throws IOException, ResourceInitializationException
    {
        if (this.splitter == null) {
            kompVVicProvider.configure();
            kompVHicProvider.configure();
            grfExtlProvider.configure();
            this.splitter = new AsvToolboxSplitterAlgorithm(kompVVicProvider.getResource(),
                    kompVHicProvider.getResource(), grfExtlProvider.getResource());
        }
        return this.splitter;
    }

}
