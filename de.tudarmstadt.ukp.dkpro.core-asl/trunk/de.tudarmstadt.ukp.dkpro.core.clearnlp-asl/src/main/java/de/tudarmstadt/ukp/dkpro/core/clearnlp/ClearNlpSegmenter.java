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
import java.io.StringReader;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;

import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.segmentation.AbstractSegmenter;
import com.clearnlp.tokenization.AbstractTokenizer;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.SegmenterBase;

/**
 * Tokenizer using Clear NLP.
 *
 * @author Richard Eckart de Castilho
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

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{

		super.process(aJCas);
	}

	@Override
	protected void process(JCas aJCas, String aText, int aZoneBegin)
		throws AnalysisEngineProcessException
	{
	    AbstractTokenizer tokenizer = NLPGetter.getTokenizer(aJCas.getDocumentLanguage());
	    AbstractSegmenter segmenter = NLPGetter.getSegmenter(aJCas.getDocumentLanguage(), tokenizer);

		List<List<String>> sentences = segmenter.getSentences(new BufferedReader(new StringReader(aText)));

		int sBegin = 0;
		int sEnd = 0;
		int tBegin = 0;
		int tEnd = 0;

		for (List<String> sentence : sentences) {
			sBegin = -1;

			for (String token : sentence) {
				tBegin = aText.indexOf(token, tBegin);
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
