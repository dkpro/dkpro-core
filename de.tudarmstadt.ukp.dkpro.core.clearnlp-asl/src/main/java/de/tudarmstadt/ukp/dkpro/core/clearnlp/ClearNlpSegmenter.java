/*******************************************************************************
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.clearnlp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import com.clearnlp.segmentation.AbstractSegmenter;
import com.clearnlp.segmentation.EnglishSegmenter;
import com.clearnlp.tokenization.EnglishTokenizer;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CasConfigurableProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ModelProviderBase;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterBase;

/**
 * Tokenizer using Clear NLP.
 *
 */
@TypeCapability(
	    outputs = {
	        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
	        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public class ClearNlpSegmenter
	extends SegmenterBase
{
    /**
     * Use this language instead of the document language to resolve the model.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false, defaultValue="en")
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

    private CasConfigurableProviderBase<AbstractSegmenter> modelProvider;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        modelProvider = new ModelProviderBase<AbstractSegmenter>(this, "clearnlp", "dictionary")
        {
            {
                setContextObject(ClearNlpSegmenter.this);

                setDefault(ARTIFACT_ID, "${groupId}.clearnlp-model-dictionary-${language}-${variant}");
                setDefault(LOCATION,
                        "classpath:/${package}/lib/dictionary-${language}-${variant}.properties");
                setDefault(VARIANT, "default");

                setOverride(LOCATION, modelLocation);
                setOverride(LANGUAGE, language);
                setOverride(VARIANT, variant);
            }

            @Override
            protected AbstractSegmenter produceResource(InputStream aStream)
                throws Exception
            {
                String language = getAggregatedProperties().getProperty(LANGUAGE);
                AbstractSegmenter segmenter;
                if(language.equals("en")){
                    segmenter = new EnglishSegmenter(new EnglishTokenizer(aStream));
                }else{
                    throw new ResourceInitializationException(new
                            Throwable("ClearNLP segmenter supports only English"));
                }
                return segmenter;
            }

        };
    }

	@Override
	protected void process(JCas aJCas, String aText, int aZoneBegin)
		throws AnalysisEngineProcessException
	{
	    modelProvider.configure(aJCas.getCas());
	    AbstractSegmenter segmenter = modelProvider.getResource();

		List<List<String>> sentences = segmenter.getSentences(new BufferedReader(new StringReader(aText)));

		int sBegin = 0;
		int sEnd = 0;
		int tBegin = 0;
		int tEnd = 0;

		for (List<String> sentence : sentences) {
			sBegin = -1;

			for (String token : sentence) {
				tBegin = aText.indexOf(token, tEnd);
				tEnd = tBegin + token.length();

				if (sBegin == -1) {
					sBegin = tBegin;
				}

				createToken(aJCas, aZoneBegin + tBegin, aZoneBegin + tEnd);
			}
			sEnd = tEnd;

			createSentence(aJCas, aZoneBegin + sBegin, aZoneBegin + sEnd);
		}
	}
}
