/*
 * Copyright 2017
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
 */
package de.tudarmstadt.ukp.dkpro.core.langdetect;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Langdetect language identifier based on character n-grams.
 * 
 * Due to the way LangDetect is implemented, this component does <b>not</b> support being
 * instantiated multiple times with different model locations. Only a single model location
 * can be active at a time over <b>all</b> instances of this component. 
 */
@Component(OperationType.LANGUAGE_IDENTIFIER)
@ResourceMetaData(name = "LangDetect")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
public class LangDetectLanguageIdentifier
    extends JCasAnnotator_ImplBase
{
    /**
     * Variant of a model the model. Used to address a specific model if here are multiple models
     * for one language.
     */
    public static final String PARAM_VARIANT = ComponentParameters.PARAM_VARIANT;
    @ConfigurationParameter(name = PARAM_VARIANT, mandatory = false)
    protected String variant;

    /**
     * URI of the model artifact. This can be used to override the default model resolving 
     * mechanism and directly address a particular model.
     * 
     * <p>The URI format is {@code mvn:${groupId}:${artifactId}:${version}}. Remember to set
     * the variant parameter to match the artifact. If the artifact contains the model in
     * a non-default location, you  also have to specify the model location parameter, e.g.
     * {@code classpath:/model/path/in/artifact/model.bin}.</p>
     */
    public static final String PARAM_MODEL_ARTIFACT_URI = 
            ComponentParameters.PARAM_MODEL_ARTIFACT_URI;
    @ConfigurationParameter(name = PARAM_MODEL_ARTIFACT_URI, mandatory = false)
    protected String modelArtifactUri;
    
    /**
     * Location from which the model is read.
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    protected String modelLocation;
    private CasConfigurableProviderBase<File> modelProvider;

    /**
     * The random seed.
     */
    public static final String PARAM_SEED = "seed";
    @ConfigurationParameter(name = PARAM_SEED, mandatory = false)
    private Long seed;
    
    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        modelProvider = new ModelProviderBase<File>()
        {
            {
                setContextObject(LangDetectLanguageIdentifier.this);

                setDefault(ARTIFACT_ID, "${groupId}.langdetect-model-${language}-${variant}");
                setDefault(LOCATION,
                        "classpath:/${package}/lib/languageidentifier-${language}-${variant}.properties");
                setDefault(VARIANT, "wikipedia");

                setOverride(LOCATION, modelLocation);
                setOverride(LANGUAGE, "any");
                setOverride(VARIANT, variant);
            }

            @Override
            protected File produceResource(URL aUrl)
                throws IOException
            {
                try {
                    DetectorFactory.clear();
                    if (seed != null) {
                        DetectorFactory.setSeed(seed);
                    }
                    File profileFolder = ResourceUtils.getClasspathAsFolder(aUrl.toString(), true);
                    DetectorFactory.loadProfile(profileFolder);
                    return profileFolder;
                }
                catch (LangDetectException e) {
                    throw new IOException(e);
                }
            }
        };
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        modelProvider.configure(aJCas.getCas());
        modelProvider.getResource();

        String documentText = aJCas.getDocumentText();
        String language = detectLanguage(documentText);
        aJCas.setDocumentLanguage(language);
    }

    private String detectLanguage(String aDocumentText)
        throws AnalysisEngineProcessException
    {
        String language = "x-unspecified";
        try {
            Detector detector = DetectorFactory.create();
            detector.append(aDocumentText);
            language = detector.detect();
        }
        catch (LangDetectException e) {
            // "no features in text" might occur if a message composes for instance of a single
            // numeric value
            // we silently ignore this particular error message, but throw all other
            if (!isFeatureException(e)) {
                throw new AnalysisEngineProcessException(e);
            }
        }
        return language;
    }

    private boolean isFeatureException(LangDetectException e)
    {
        return e.getMessage().equals("no features in text");
    }
}
