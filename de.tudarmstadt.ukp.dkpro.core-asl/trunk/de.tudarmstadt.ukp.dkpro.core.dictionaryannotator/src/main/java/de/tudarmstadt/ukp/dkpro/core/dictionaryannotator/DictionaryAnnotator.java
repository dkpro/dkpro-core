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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.JCasUtil;

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
	public static final String PARAM_PHRASE_FILE = "PhraseFile";
	public static final String PARAM_ANNOTATION_TYPE = "AnnotationType";

	/** The file must contain one phrase per line - phrases will be split at " " */
	@ConfigurationParameter(name = PARAM_PHRASE_FILE, mandatory = true, defaultValue = "phrases.txt")
	private String phraseFile;

	@ConfigurationParameter(name = PARAM_ANNOTATION_TYPE, mandatory = false)
	private String annotationType;

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

		AnnotationIndex sentenceIndex = jcas.getAnnotationIndex(Sentence.type);
		FSIterator sentenceIterator = sentenceIndex.iterator();

		while (sentenceIterator.hasNext()) {
			Sentence currSentence = (Sentence) sentenceIterator.next();

			ArrayList<Token> tokensInSentence = new ArrayList<Token>();

			for (Token currToken : JCasUtil.selectCovered(jcas,
					Token.class, currSentence)) {
				tokensInSentence.add(currToken);
			}

			for (int i = 0; i < tokensInSentence.size(); i++) {
				List<Token> tokensToSentenceEnd = tokensInSentence.subList(i, tokensInSentence.size()-1);
				String[] sentenceToEnd = new String[tokensInSentence.size()];

				for (int j=0;j<tokensToSentenceEnd.size();j++) {
					sentenceToEnd[j] = tokensToSentenceEnd.get(j).getCoveredText();
				}

				String[] longestMatch = phrases.getLongestMatch(sentenceToEnd);

				if (longestMatch != null) {
					Token beginToken = tokensInSentence.get(i);
					Token endToken = tokensInSentence.get(i + longestMatch.length - 1);

					AnnotationFS newFound = jcas.getCas().createAnnotation(type, beginToken.getBegin(), endToken.getEnd());
					jcas.getCas().addFsToIndexes(newFound);
				}
			}
		}
	}
}
