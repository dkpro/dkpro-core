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
package de.tudarmstadt.ukp.dkpro.core.castransformation;

import de.tudarmstadt.ukp.dkpro.core.api.transform.alignment.AlignedString;
import de.tudarmstadt.ukp.dkpro.core.api.transform.alignment.ImmutableInterval;
import de.tudarmstadt.ukp.dkpro.core.api.transform.alignment.Interval;
import de.tudarmstadt.ukp.dkpro.core.api.transform.internal.CasCopier;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.*;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.LinkedList;

/**
 * After processing a file with the {@code ApplyChangesAnnotator} this annotator
 * can be used to map the annotations created in the cleaned view back to the
 * original view.
 *
 * @see ApplyChangesAnnotator
 */
public
class Backmapper
extends JCasAnnotator_ImplBase
{
	/**
	 * Chain of views for backmapping. This should be the reverse of the chain of views that the
	 * {@code ApplyChangesAnnotator} has used.
	 *
	 * For example, if view A has been mapped to B using {@code ApplyChangesAnnotator}, then this
	 * parameter should be set using an array containing [B, A].
	 */
	public static final String PARAM_CHAIN = "Chain";

	@ConfigurationParameter(name = PARAM_CHAIN, mandatory = false, defaultValue = {ApplyChangesAnnotator.VIEW_SOURCE,
			ApplyChangesAnnotator.VIEW_TARGET})
	protected LinkedList<String> sofaChain = new LinkedList<>();

	@Override
	public
	void process(
			final JCas aJCas)
	throws AnalysisEngineProcessException
	{
		try {
			// Copy the annotations
			CasCopier cc = new CasCopier(aJCas.getCas(), aJCas.getCas());

			// Now we can copy the complete CAS while mapping back the offsets.
			// We first use the CAS copier and then update the offsets.
			getLogger().info("Copying annotations from [" + sofaChain.getFirst() + "] to ["
							+ sofaChain.getLast() + "]");
			cc.copyCasView(aJCas.getCas().getView(sofaChain.getFirst()), sofaChain.getLast(), false);

			// Get the final target view
			JCas targetView = aJCas.getView(sofaChain.getLast());

			LinkedList<String> workChain = new LinkedList<>(sofaChain);
			String target = workChain.poll();
			String source = null;

			do {
				source = target;
				target = workChain.poll();

				// Ok, so now we update the offsets.
				String realSource = aJCas.getCas().getView(source).getViewName();
				String realTarget = aJCas.getCas().getView(target).getViewName();
				updateOffsets(targetView, realSource, realTarget, cc);
			}
			while (!workChain.isEmpty());
		}
		catch (UIMAException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	protected
	void updateOffsets(
			final JCas targetView,
			String from,
			String to,
			CasCopier cc)
	throws CASException, AnalysisEngineProcessException
	{
		CAS baseCas = targetView.getCasImpl().getBaseCAS();

		// Try to get the AlignedString for the current JCas.
		AlignmentStorage asstore = AlignmentStorage.getInstance();
		AlignedString as = asstore.get(baseCas, to, from);

		// If there is none we have to fail.
		if (as == null) {
			throw new AnalysisEngineProcessException(new IllegalStateException(
					"No mapping found from ["+from+"] to ["+to+"] on ["+baseCas.hashCode()+"]"));
		}

		FSIndex<Annotation> idx = targetView.getAnnotationIndex().withSnapshotIterators();
		FSIterator<Annotation> it = idx.iterator();
		TypeSystem typeSystem = targetView.getTypeSystem();
		Type annotationType = typeSystem.getType(CAS.TYPE_NAME_ANNOTATION);
		while (it.isValid()) {
			FeatureStructure fs = it.get();

			// Index "AnnotationIndex" is over type "uima.tcas.Annotation".
			if (!typeSystem.subsumes(annotationType, fs.getType())) {
				it.moveToNext();
				continue;
			}

			// We only update annotations that were copied, nothing that
			// was already there.
			if (!cc.isCopied(fs)) {
				it.moveToNext();
				continue;
			}

			// Now we update the offsets
			Annotation a = (Annotation) fs;
			Interval resolved = as.resolve(new ImmutableInterval(a.getBegin(), a.getEnd()));
			a.setBegin(resolved.getStart());
			a.setEnd(resolved.getEnd());
			it.moveToNext();
		}
	}
}
