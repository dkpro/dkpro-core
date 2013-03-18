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
package de.tudarmstadt.ukp.dkpro.core.posfilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Level;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.CasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathFactory;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;

/**
 * Removes all tokens/lemmas/stems (depending on the "Mode" setting) that do not match the given
 * parts of speech.
 *
 * @author Torsten Zesch
 */
public class PosFilter
	extends JCasAnnotator_ImplBase
{
	/**
	 * The fully qualified name of the type that should be filtered.
	 */
	public static final String PARAM_TYPE_TO_REMOVE = "TypeToRemove";
	@ConfigurationParameter(name = PARAM_TYPE_TO_REMOVE, mandatory = true)
	private String typeToRemove;

	/**
	 * Keep/remove adjectives (true: keep, false: remove)
	 */
	public static final String PARAM_ADJ = "Adjectives";
	@ConfigurationParameter(name = PARAM_ADJ, mandatory = true, defaultValue = "false")
	private boolean adj;

	/**
	 * Keep/remove adverbs (true: keep, false: remove)
	 */
	public static final String PARAM_ADV = "Adverbs";
	@ConfigurationParameter(name = PARAM_ADV, mandatory = true, defaultValue = "false")
	private boolean adv;

	/**
	 * Keep/remove articles (true: keep, false: remove)
	 */
	public static final String PARAM_ART = "Articles";
	@ConfigurationParameter(name = PARAM_ART, mandatory = true, defaultValue = "false")
	private boolean art;

	/**
	 * Keep/remove cardinal numbers (true: keep, false: remove)
	 */
	public static final String PARAM_CARD = "Cardinals";
	@ConfigurationParameter(name = PARAM_CARD, mandatory = true, defaultValue = "false")
	private boolean card;

	/**
	 * Keep/remove conjunctions (true: keep, false: remove)
	 */
	public static final String PARAM_CONJ = "Conjunctions";
	@ConfigurationParameter(name = PARAM_CONJ, mandatory = true, defaultValue = "false")
	private boolean conj;

	/**
	 * Keep/remove nouns (true: keep, false: remove)
	 */
	public static final String PARAM_N = "Nouns";
	@ConfigurationParameter(name = PARAM_N, mandatory = true, defaultValue = "false")
	private boolean n;

	/**
	 * Keep/remove "others" (true: keep, false: remove)
	 */
	public static final String PARAM_O = "Others";
	@ConfigurationParameter(name = PARAM_O, mandatory = true, defaultValue = "false")
	private boolean o;

	/**
	 * Keep/remove prepositions (true: keep, false: remove)
	 */
	public static final String PARAM_PP = "Prepositions";
	@ConfigurationParameter(name = PARAM_PP, mandatory = true, defaultValue = "false")
	private boolean pp;

	/**
	 * Keep/remove pronouns (true: keep, false: remove)
	 */
	public static final String PARAM_PR = "Pronouns";
	@ConfigurationParameter(name = PARAM_PR, mandatory = true, defaultValue = "false")
	private boolean pr;

	/**
	 * Keep/remove punctuation (true: keep, false: remove)
	 */
	public static final String PARAM_PUNC = "Punctuation";
	@ConfigurationParameter(name = PARAM_PUNC, mandatory = true, defaultValue = "false")
	private boolean punc;

	/**
	 * Keep/remove verbs (true: keep, false: remove)
	 */
	public static final String PARAM_V = "Verbs";
	@ConfigurationParameter(name = PARAM_V, mandatory = true, defaultValue = "false")
	private boolean v;

	private JCas jcas;

	@Override
	public void process(JCas jcas)
		throws AnalysisEngineProcessException
	{
		this.jcas = jcas;

		getContext().getLogger().log(Level.CONFIG, "Entering " + this.getClass().getSimpleName());

		Type posType = jcas.getCas().getTypeSystem().getType(POS.class.getCanonicalName());

		if (posType == null) {
			throw new AnalysisEngineProcessException(new Throwable(
					"Could not get type for feature path: " + typeToRemove));
		}

		List<AnnotationFS> toRemove = new ArrayList<AnnotationFS>();
		try {
			for (Entry<AnnotationFS, String> entry : FeaturePathFactory.select(jcas.getCas(),
					typeToRemove)) {
				AnnotationFS annotation = entry.getKey();
				AnnotationFS pos = getPos(posType, annotation);
				if (pos == null) {
					continue;
				}

				String posString = pos.getType().getShortName();
				if (posString.equals("ADJ") && !adj) {
					toRemove.add(annotation);
					continue;
				}
				if (posString.equals("ADV") && !adv) {
					toRemove.add(annotation);
					continue;
				}
				if (posString.equals("ART") && !art) {
					toRemove.add(annotation);
					continue;
				}
				if (posString.equals("CARD") && !card) {
					toRemove.add(annotation);
					continue;
				}
				if (posString.equals("CONJ") && !conj) {
					toRemove.add(annotation);
					continue;
				}
				if ((posString.equals("N") || posString.equals("NN") || posString.equals("NP"))
						&& !n) {
					toRemove.add(annotation);
					continue;
				}
				if (posString.equals("O") && !o) {
					toRemove.add(annotation);
					continue;
				}
				if (posString.equals("PP") && !pp) {
					toRemove.add(annotation);
					continue;
				}
				if (posString.equals("PR") && !pr) {
					toRemove.add(annotation);
					continue;
				}
				if (posString.equals("PUNC") && !punc) {
					toRemove.add(annotation);
					continue;
				}
				if (posString.equals("V") && !v) {
					toRemove.add(annotation);
					continue;
				}
			}
		}
		catch (FeaturePathException e) {
			throw new AnalysisEngineProcessException(e);
		}

		for (AnnotationFS fs : toRemove) {
			jcas.getCas().removeFsFromIndexes(fs);
		}
	}

	/**
	 * Returns the POS annotation aligned with another annotation
	 *
	 * @param annotation
	 *            An annotation.
	 * @return The POS annotation aligned with another annotation.
	 */
	private AnnotationFS getPos(Type type, AnnotationFS fs)
	{
		List<AnnotationFS> posAnnotations = CasUtil.selectCovered(jcas.getCas(), type, fs);
		if (posAnnotations.size() != 1) {
			getContext().getLogger()
					.log(Level.WARNING,
							"Could not find matching POS annotation for annotation: "
									+ fs.getCoveredText());
			return null;
		}

		return posAnnotations.get(0);
	}
}
