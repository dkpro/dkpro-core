/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.core.langdetect;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;

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
     * Location from which the model is read.
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    protected String modelLocation;
    private CasConfigurableProviderBase<File> modelProvider;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        modelProvider = new ModelProviderBase<File>()
        {
            {
                setContextObject(LangDetectLanguageIdentifier.this);

                setDefault(ARTIFACT_ID,
                        "${groupId}.langdetect-model-${language}-${variant}");
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
                    File profileFolder = ResourceUtils.getClasspathAsFolder(aUrl.toString(), false);
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

        try {
            // Reinitialize the profiles in the DetectorFactory if necessary
            modelProvider.getResource(); 
            
            // Create a new detector
            Detector detector = DetectorFactory.create();
            detector.append(aJCas.getDocumentText());
            
            String language = detector.detect();
            
            aJCas.setDocumentLanguage(language);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
