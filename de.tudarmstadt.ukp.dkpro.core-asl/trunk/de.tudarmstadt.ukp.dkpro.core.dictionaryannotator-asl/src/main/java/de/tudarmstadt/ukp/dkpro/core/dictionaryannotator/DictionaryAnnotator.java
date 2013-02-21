/*******************************************************************************
 * Copyright 2010
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
package de.tudarmstadt.ukp.dkpro.core.dictionaryannotator;

import static org.uimafit.util.CasUtil.getType;
import static org.uimafit.util.JCasUtil.select;
import static org.uimafit.util.JCasUtil.selectCovered;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.TypeCapability;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.NGram;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Takes a plain text file with phrases as input and annotates the phrases in the CAS file. The
 * annotation type defaults to {@link NGram}, but can be changed.
 * 
 * The component requires that {@link Token}s and {@link Sentence}es are annotated in the CAS.
 * 
 * The format of the phrase file is one phrase per line, tokens are separated by space:
 * 
 * <pre>
 * this is a phrase
 * another phrase
 * </pre>
 * 
 * @author Johannes Hoffart
 * @author Richard Eckart de Castilho
 */
@TypeCapability(
	    inputs = { 
	        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
	        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence" })
public class DictionaryAnnotator
	extends JCasAnnotator_ImplBase
{
	/**
	 * The file must contain one phrase per line - phrases will be split at " "
	 */
	public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
	@ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = true)
	private String phraseFile;

	public static final String PARAM_MODEL_ENCODING = ComponentParameters.PARAM_MODEL_ENCODING;
	@ConfigurationParameter(name = PARAM_MODEL_ENCODING, mandatory = true, defaultValue="UTF-8")
	private String modelEncoding;

	/**
	 * The annotation to create on matching phases. If nothing is specified, this defaults to
	 * {@link NGram}.
	 */
	public static final String PARAM_ANNOTATION_TYPE = "annotationType";
	@ConfigurationParameter(name = PARAM_ANNOTATION_TYPE, mandatory = false)
	private String annotationType;

	public static final String PARAM_VALUE_FEATURE = "valueFeature";
	@ConfigurationParameter(name = PARAM_VALUE_FEATURE, mandatory = false, defaultValue = "value")
	private String valueFeature;

	public static final String PARAM_VALUE = "value";
	@ConfigurationParameter(name = PARAM_VALUE, mandatory = false)
	private String value;

	private PhraseTree phrases;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		if (annotationType == null) {
			annotationType = NGram.class.getName();
		}

		phrases = new PhraseTree();

		InputStream is = null;
		try {
			URL phraseFileUrl = ResourceUtils.resolveLocation(phraseFile, aContext);
			is = phraseFileUrl.openStream();
			for (String inputLine : IOUtils.readLines(is, modelEncoding)) {
				String[] phraseSplit = inputLine.split(" ");
				phrases.addPhrase(phraseSplit);
			}
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
		finally {
			IOUtils.closeQuietly(is);
		}
	}

	@Override
	public void process(JCas jcas)
		throws AnalysisEngineProcessException
	{
		Type type = getType(jcas.getCas(), annotationType);

		Feature f = null;
		if ((valueFeature != null) && (value != null)) {
			f = type.getFeatureByBaseName(valueFeature);
			if (f == null) {
				throw new IllegalArgumentException("Undeclared feature [" + valueFeature
						+ "] in type [" + annotationType + "]");
			}
		}

		for (Sentence currSentence : select(jcas, Sentence.class)) {
			ArrayList<Token> tokens = new ArrayList<Token>(selectCovered(Token.class, currSentence));

			for (int i = 0; i < tokens.size(); i++) {
				List<Token> tokensToSentenceEnd = tokens.subList(i, tokens.size() - 1);
				String[] sentenceToEnd = new String[tokens.size()];

				for (int j = 0; j < tokensToSentenceEnd.size(); j++) {
					sentenceToEnd[j] = tokensToSentenceEnd.get(j).getCoveredText();
				}

				String[] longestMatch = phrases.getLongestMatch(sentenceToEnd);

				if (longestMatch != null) {
					Token beginToken = tokens.get(i);
					Token endToken = tokens.get(i + longestMatch.length - 1);

					AnnotationFS newFound = jcas.getCas().createAnnotation(type,
							beginToken.getBegin(), endToken.getEnd());

					if (f != null) {
						newFound.setFeatureValueFromString(f, value);
					}

					jcas.getCas().addFsToIndexes(newFound);
				}
			}
		}
	}
}
