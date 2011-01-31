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
package de.tudarmstadt.ukp.dkpro.core.jazzy;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.util.Level.INFO;
import static org.uimafit.util.JCasUtil.iterate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.engine.Word;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SpellingAnomaly;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * This annotator uses Jazzy for the decision whether a word is spelled correctly or not.
 */
public class SpellChecker
	extends JCasAnnotator_ImplBase
{
	public static final String PARAM_DICT_PATH = "DictionaryPath";
	@ConfigurationParameter(name = PARAM_DICT_PATH, mandatory = true)
	private String dictPath;

	public static final String PARAM_DICT_ENCODING = "DictionaryEncoding";
	@ConfigurationParameter(name = PARAM_DICT_ENCODING, mandatory = true, defaultValue = "UTF-8")
	private String dictEncoding;

	private SpellDictionary dict;

	@Override
	public void initialize(final UimaContext context)
		throws ResourceInitializationException
	{
		super.initialize(context);
		InputStream is = null;
		try {
			URL url = ResourceUtils.resolveLocation(dictPath, this, context);
			context.getLogger().log(INFO, "Loading dictionary from " + url);
			is = url.openStream();
			dict = new SpellDictionaryHashMap(new InputStreamReader(is, dictEncoding));
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
		finally {
			closeQuietly(is);
		}
	}

	@Override
	public void process(final JCas cas)
		throws AnalysisEngineProcessException
	{
		for (Token t : iterate(cas, Token.class)) {
			String tokenText = t.getCoveredText();
			if (tokenText.matches("[\\.\\?\\!]")) {
				continue;
			}
			if (!dict.isCorrect(tokenText)) {
				SpellingAnomaly anomaly = new SpellingAnomaly(cas, t.getBegin(), t.getEnd());
				@SuppressWarnings("unchecked")
				List<Word> suggestions = dict.getSuggestions(tokenText, 1);
				if (suggestions.size() > 0) {
					anomaly.setSuggestion(suggestions.get(0).getWord());
				}
				anomaly.addToIndexes();
			}
		}
	}
}
