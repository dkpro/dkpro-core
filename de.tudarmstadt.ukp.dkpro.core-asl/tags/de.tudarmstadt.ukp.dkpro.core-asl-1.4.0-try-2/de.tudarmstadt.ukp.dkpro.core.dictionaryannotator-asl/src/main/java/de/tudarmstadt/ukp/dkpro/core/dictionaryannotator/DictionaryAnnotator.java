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

import static org.uimafit.util.JCasUtil.select;
import static org.uimafit.util.JCasUtil.selectCovered;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.NGram;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Takes a plain text file with phrases as input and annotates
 * the phrases in the CAS file. The annotation type defaults to
 * {@link NGram}, but can be changed.
 *
 * The format of the phrase file is one phrase per line, tokens are
 * separated by space:
 *
 * <pre>
 * this is a phrase
 * another phrase
 * </pre>
 *
 * @author Johannes Hoffart
 *
 */
public class DictionaryAnnotator
	extends JCasAnnotator_ImplBase
{
	/**
	 * The file must contain one phrase per line - phrases will be split at " "
	 */
	public static final String PARAM_PHRASE_FILE = ComponentParameters.PARAM_MODEL_LOCATION;
	@ConfigurationParameter(name = PARAM_PHRASE_FILE, mandatory = true, defaultValue = "phrases.txt")
	private String phraseFile;

	public static final String PARAM_ANNOTATION_TYPE = "annotationType";
	@ConfigurationParameter(name = PARAM_ANNOTATION_TYPE, mandatory = false)
	private String annotationType;

	public static final String PARAM_VALUE_FEATURE = "valueFeature";
	@ConfigurationParameter(name = PARAM_VALUE_FEATURE, mandatory = false, defaultValue="value")
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

		try {
			BufferedReader reader = new BufferedReader(new FileReader(phraseFile));

			String inputLine;
			while ((inputLine = reader.readLine()) != null) {
				String[] phraseSplit = inputLine.split(" ");
				phrases.addPhrase(phraseSplit);
			}

			reader.close();
		}
		catch (FileNotFoundException e) {
			throw new ResourceInitializationException(e);
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void process(JCas jcas)
		throws AnalysisEngineProcessException
	{
		Type type = jcas.getTypeSystem().getType(annotationType);
		Feature f = null;
		if ((valueFeature != null) && (value != null)) {
			f = type.getFeatureByBaseName(valueFeature);
		}

		for (Sentence currSentence : select(jcas, Sentence.class)) {
			ArrayList<Token> tokens = new ArrayList<Token>(selectCovered(Token.class, currSentence));

			for (int i = 0; i < tokens.size(); i++) {
				List<Token> tokensToSentenceEnd = tokens.subList(i, tokens.size()-1);
				String[] sentenceToEnd = new String[tokens.size()];

				for (int j=0;j<tokensToSentenceEnd.size();j++) {
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
