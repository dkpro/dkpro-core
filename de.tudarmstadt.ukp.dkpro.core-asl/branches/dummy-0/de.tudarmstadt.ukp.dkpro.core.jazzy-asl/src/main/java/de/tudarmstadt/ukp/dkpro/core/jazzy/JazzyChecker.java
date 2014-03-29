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
import static org.apache.uima.fit.util.JCasUtil.select;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.engine.Word;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SpellingAnomaly;
import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SuggestedAction;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * This annotator uses Jazzy for the decision whether a word is spelled correctly or not.
 */

@TypeCapability(
        inputs={
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token"},
        outputs={
                "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SpellingAnomaly",
                "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SuggestedAction"})

public class JazzyChecker
	extends JCasAnnotator_ImplBase
{
	/**
	 * Location from which the model is read. The model file is a simple word-list with one word
	 * per line.
	 */
	public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
	@ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = true)
	private String dictPath;

	/**
	 * The character encoding used by the model.
	 */
	public static final String PARAM_MODEL_ENCODING = ComponentParameters.PARAM_MODEL_ENCODING;
	@ConfigurationParameter(name = PARAM_MODEL_ENCODING, mandatory = true, defaultValue = "UTF-8")
	private String dictEncoding;

    /**
     * Determines the maximum edit distance (as an int value) that a suggestion for a spelling error may have.
     * E.g. if set to one suggestions are limited to words within edit distance 1 to the original word.
     */
    public static final String PARAM_SCORE_THRESHOLD = "ScoreThreshold";
    @ConfigurationParameter(name = PARAM_SCORE_THRESHOLD, mandatory = true, defaultValue = "1")
    private int scoreThreshold;

    private SpellDictionary dict;

	@Override
	public void initialize(final UimaContext context)
		throws ResourceInitializationException
	{
		super.initialize(context);
		InputStream is = null;
		try {
			URL url = ResourceUtils.resolveLocation(dictPath, this, context);
			this.getLogger().debug("Loading dictionary from " + url);
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
	public void process(final JCas jcas)
		throws AnalysisEngineProcessException
	{
		for (Token t : select(jcas, Token.class)) {
			String tokenText = t.getCoveredText();
			if (tokenText.matches("[\\.\\?\\!]")) {
				continue;
			}
			if (!dict.isCorrect(tokenText)) {
				SpellingAnomaly anomaly = new SpellingAnomaly(jcas, t.getBegin(), t.getEnd());

				// only try to correct single character tokens if they are letters
				if (tokenText.length() == 1 && !Character.isLetter(tokenText.charAt(0))) {
				    continue;
				}

				@SuppressWarnings("unchecked")
				List<Word> suggestions = dict.getSuggestions(tokenText, scoreThreshold);

				SuggestionCostTuples tuples = new SuggestionCostTuples();
				for (Word suggestion : suggestions) {
				    String suggestionString = suggestion.getWord();
				    int cost = suggestion.getCost();

				    if (suggestionString != null) {
	                    tuples.addTuple(suggestionString, cost);
				    }
				}

				if (tuples.size() > 0) {
    				FSArray actions = new FSArray(jcas, tuples.size());
    				int i=0;
    				for (SuggestionCostTuple tuple : tuples) {
                        SuggestedAction action = new SuggestedAction(jcas);
                        action.setReplacement(tuple.getSuggestion());
                        action.setCertainty(tuple.getNormalizedCost(tuples.getMaxCost()));

                        actions.set(i, action);
                        i++;
    				}
    			    anomaly.setSuggestions(actions);
                    anomaly.addToIndexes();
				}
			}
		}
	}

    class SuggestionCostTuples implements Iterable<SuggestionCostTuple> {
	    private final List<SuggestionCostTuple> tuples;
	    private int maxCost;

	    public SuggestionCostTuples()
        {
	        tuples = new ArrayList<SuggestionCostTuple>();
	        maxCost = 0;
        }

	    public void addTuple(String suggestion, int cost) {
	        tuples.add(new SuggestionCostTuple(suggestion, cost));

	        if (cost > maxCost) {
	            maxCost = cost;
	        }
	    }

	    public int getMaxCost() {
	        return maxCost;
	    }

	    public int size() {
	        return tuples.size();
	    }

        @Override
        public Iterator<SuggestionCostTuple> iterator()
        {
            return tuples.iterator();
        }
	}

    class SuggestionCostTuple {
        private final String suggestion;
        private final Integer cost;

        public SuggestionCostTuple(String suggestion, Integer cost)
        {
            this.suggestion = suggestion;
            this.cost = cost;
        }

        public String getSuggestion()
        {
            return suggestion;
        }

        public Integer getCost()
        {
            return cost;
        }

        public float getNormalizedCost(int maxCost)
        {
            if (maxCost > 0) {
                return (float) cost / maxCost;
            }
            else {
                return 0f;
            }
        }
    }
}