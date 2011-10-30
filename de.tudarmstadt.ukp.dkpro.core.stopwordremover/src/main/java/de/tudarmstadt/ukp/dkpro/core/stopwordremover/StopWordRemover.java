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
package de.tudarmstadt.ukp.dkpro.core.stopwordremover;

import static de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils.resolveLocation;
import static org.apache.uima.util.Level.FINE;
import static org.apache.uima.util.Level.INFO;
import static org.uimafit.util.CasUtil.select;
import static org.uimafit.util.JCasUtil.getView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Logger;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;

import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathInfo;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.StopWord;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Remove all of the specified types from the CAS if their covered text is in the stop word
 * dictionary. Also remove any other of the specified types that is covered by a matching instance.
 *
 * @author Richard Eckart de Castilho
 */
public class StopWordRemover
	extends JCasAnnotator_ImplBase
{
	// VIEW NAMES
	private static final String TOPIC_VIEW = "topic";
	private static final String DOC_VIEW = "doc";

	/**
	 * A list of URLs from which to load the stop word lists. If an URL is prefixed with a language
	 * code in square brackets, the stop word list is only used for documents in that language.
	 * Using no prefix or the prefix "[*]" causes the list to be used for every document.
	 * Example: "[de]classpath:/stopwords/en_articles.txt"
	 */
	public static final String PARAM_STOP_WORD_LIST_FILE_NAMES = ComponentParameters.PARAM_MODEL_LOCATION;
	@ConfigurationParameter(name = PARAM_STOP_WORD_LIST_FILE_NAMES, mandatory = true)
	private Set<String> swFileNames;

	/**
	 * Feature paths for annotations that should be matched/removed. The default is
	 *
	 * <pre>
	 * StopWord.class.getName()
	 * Token.class.getName()
	 * Lemma.class.getName()+"/value"
	 * </pre>
	 */
	public static final String PARAM_PATHS = "Paths";
	@ConfigurationParameter(name = PARAM_PATHS, mandatory = false)
	private Set<String> paths;

	/**
	 * Anything annotated with this type will be removed even if it does not match any word in the
	 * lists.
	 */
	public static final String PARAM_STOP_WORD_TYPE = "StopWordType";
	@ConfigurationParameter(name = PARAM_STOP_WORD_TYPE, mandatory = false)
	private String stopWordType;

	private Map<String, StopWordSet> stopWordSets;

	@Override
	public void initialize(UimaContext context)
		throws ResourceInitializationException
	{
		super.initialize(context);

		// Set default paths. This cannot be done in the annotation because we cannot call
		// methods there.
		if (paths == null || paths.size() == 0) {
			paths = new HashSet<String>();
			paths.add(StopWord.class.getName());
			paths.add(Token.class.getName());
			paths.add(Lemma.class.getName()+"/value");
		}

		// Set default stop word type. This cannot be done in the annotation because we cannot call
		// methods there.
		if (stopWordType == null) {
			stopWordType = StopWord.class.getName();
		}

		try {
			stopWordSets = new HashMap<String, StopWordSet>();
			for (String swFileName : swFileNames) {
				String fileLocale = "*";
				// Check if a locale is defined for the file
				if (swFileName.startsWith("[")) {
					fileLocale = swFileName.substring(1, swFileName.indexOf(']'));
					swFileName = swFileName.substring(swFileName.indexOf(']')+1);
				}

				// Fetch the set for the specified locale
				StopWordSet set = stopWordSets.get(fileLocale);
				if (set == null) {
					set = new StopWordSet();
					stopWordSets.put(fileLocale, set);
				}

				// Load the set
				URL source = resolveLocation(swFileName, this, context);
				set.load(source.openStream());

				context.getLogger().log(INFO,
						"Loaded stopwords for locale [" + fileLocale + "] from [" + source + "]");
			}
		}
		catch (IOException e1) {
			throw new ResourceInitializationException(e1);
		}
	}

	@Override
	public void process(JCas jcas)
		throws AnalysisEngineProcessException
	{
		JCas doc = getView(jcas, DOC_VIEW, null);
		JCas topic = getView(jcas, TOPIC_VIEW, null);

		try {
			if (doc != null) {
				check(doc);
			}

			if (topic != null) {
				check(topic);
			}

			if (topic == null && doc == null) {
				check(jcas);
			}
		}
		catch (FeaturePathException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	private void check(JCas aJCas)
		throws FeaturePathException
	{
		Logger log = getContext().getLogger();

		Locale casLocale = new Locale(aJCas.getDocumentLanguage());
		StopWordSet anyLocaleSet = stopWordSets.get("*");
		StopWordSet casLocaleSet = stopWordSets.get(aJCas.getDocumentLanguage());

		// Now really to the removal part
		FeaturePathInfo fp = new FeaturePathInfo();
		for (String path : paths) {
			// Create a sorted list of annotations that we can quickly search on
			AnnotationFS[] candidates = getCandidates(aJCas);

			// Initialize list of annotations to remove
			List<AnnotationFS> toRemove = new ArrayList<AnnotationFS>();

			// Separate Typename and featurepath
			String[] segments = path.split("/", 2);

			String typeName = segments[0];
			boolean isStopWordType = stopWordType.equals(typeName);
			Type t = aJCas.getTypeSystem().getType(typeName);
			if (t == null) {
				throw new IllegalStateException("Type [" + typeName + "] not found in type system");
			}

			// initialize the FeaturePathInfo with the corresponding part
			if (segments.length > 1) {
				fp.initialize(segments[1]);
			}
			else {
				fp.initialize("");
			}

			int safeStart = 0;
			Iterator<Annotation> i = aJCas.getAnnotationIndex(t).iterator();
			while (i.hasNext()) {
				Annotation anno = i.next();

				// Move the start of the containment scanning range ahead if possible
				while ((safeStart + 1) < candidates.length
						&& candidates[safeStart + 1].getEnd() < anno.getBegin()) {
					safeStart++;
				}

				String candidate = fp.getValue(anno).toLowerCase(casLocale);
				if (isStopWordType || ((anyLocaleSet != null) && anyLocaleSet.contains(candidate))
						|| ((casLocaleSet != null) && casLocaleSet.contains(candidate))) {
					// Remove the annotation that matched the stop word
					toRemove.add(anno);
					if (log.isLoggable(FINE)) {
						log.log(FINE, "Removing ["
								+ typeName.substring(typeName.lastIndexOf('.') + 1)
								+ "] annotated as stop word [" + anno.getCoveredText() + "]@"
								+ anno.getBegin() + ".." + anno.getEnd());
					}

					// Scan all potential annotations that may be covered the current
					// annotation and remove them as well
					int n = safeStart;
					while (n < candidates.length && candidates[n].getBegin() < anno.getEnd()) {
						if ((anno.getBegin() <= candidates[n].getBegin())
								&& (candidates[n].getEnd() <= anno.getEnd())) {
							if (log.isLoggable(FINE)) {
								log.log(FINE, "Removing as well ["
										+ candidates[n].getClass().getSimpleName()
										+ "] annotated as stop word ["
										+ candidates[n].getCoveredText() + "]@"
										+ candidates[n].getBegin() + ".." + candidates[n].getEnd());
							}
							toRemove.add(candidates[n]);
						}
						n++;
					}
				}
			}

			// Remove from the CAS
			for (AnnotationFS anno : toRemove) {
				aJCas.removeFsFromIndexes(anno);
			}
		}
	}

	private AnnotationFS[] getCandidates(JCas aJCas)
	{
		// Make a list of all the annotations that can be matched by the given paths. If any one
		// of the paths match, we want to remove instances of all others being covered by the
		// match as well.
		List<AnnotationFS> candidateList = new ArrayList<AnnotationFS>();
		for (String path : paths) {
			String[] segments = path.split("/", 2);
			String typeName = segments[0];
			Type t = aJCas.getTypeSystem().getType(typeName);
			if (t == null) {
				throw new IllegalStateException("Type [" + typeName + "] not found in type system");
			}

			for (AnnotationFS fs : select(aJCas.getCas(), t)) {
				candidateList.add(fs);
			}
		}
		AnnotationFS[] candidates = candidateList.toArray(new AnnotationFS[candidateList.size()]);
		Arrays.sort(candidates, new BeginEndComparator());
		return candidates;

	}

	static class BeginEndComparator implements Comparator<AnnotationFS>
	{
		@Override
		public int compare(AnnotationFS aO1, AnnotationFS aO2)
		{
			if (aO1.getBegin() == aO2.getBegin()) {
				return aO1.getEnd() - aO2.getEnd();
			}
			else {
				return aO1.getBegin() - aO2.getBegin();
			}
		}
	}
}
