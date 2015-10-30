/*******************************************************************************
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.core.rftagger;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.logging.LogFactory;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProviderFactory;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.PlatformDetector;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.resources.RuntimeProvider;

public class RfTagger
    extends JCasAnnotator_ImplBase
{

    /**
     * Use this language instead of the document language to resolve the model.
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
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    protected String modelLocation;

    /**
     * The character encoding used by the model.
     */
    public static final String PARAM_MODEL_ENCODING = ComponentParameters.PARAM_MODEL_ENCODING;
    @ConfigurationParameter(name = PARAM_MODEL_ENCODING, mandatory = false)
    protected String modelEncoding;

    /**
     * Load the part-of-speech tag to UIMA type mapping from this location instead of locating the
     * mapping automatically.
     */
    public static final String PARAM_POS_MAPPING_LOCATION = ComponentParameters.PARAM_POS_MAPPING_LOCATION;
    @ConfigurationParameter(name = PARAM_POS_MAPPING_LOCATION, mandatory = false)
    protected String posMappingLocation;

    private MappingProvider mappingProvider;
    private RuntimeProvider rfTaggerExecutables;
    private ModelProviderBase<File> modelProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        loadExecutable();
        loadModel();
        loadMappingProvider();
    }

    private void loadMappingProvider()
    {
        mappingProvider = MappingProviderFactory.createPosMappingProvider(posMappingLocation,
                language, modelProvider);        
    }

    private void loadModel() throws ResourceInitializationException
    {
        modelProvider = new ModelProviderBase<File>()
                {
                    {
                        setContextObject(RfTagger.this);

                        setDefault(ARTIFACT_ID, "${groupId}.rftagger-model-${language}-${variant}");
                        setDefault(LOCATION,
                                "classpath:/${package}/lib/tagger-${language}-${variant}.properties");

                        setOverride(LOCATION, modelLocation);
                        setOverride(LANGUAGE, language);
                        setOverride(VARIANT, variant);
                    }

                    @Override
                    protected File produceResource(URL aUrl)
                        throws IOException
                    {
                        File folder = ResourceUtils.getClasspathAsFolder(aUrl.toString(), true);
                        return folder;
                    }
                };
                try {
                    modelProvider.configure();
                }
                catch (IOException e) {
                    throw new ResourceInitializationException(e);
                }        
    }

    private void loadExecutable()
    {
        PlatformDetector pd = new PlatformDetector();
        String platform = pd.getPlatformId();
        LogFactory.getLog(getClass()).info("Load binary for platform: [" + platform + "]");

        rfTaggerExecutables = new RuntimeProvider(
                "classpath:/de/tudarmstadt/ukp/dkpro/core/rftagger/");
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        try {
            System.out.println(rfTaggerExecutables.getFile("rft-annotate"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(modelProvider.getResource().getAbsolutePath());
        int a=0; 
        a++;
        
    }

}
