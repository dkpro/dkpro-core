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

import static java.util.Arrays.asList;

import java.util.LinkedList;

import org.apache.uima.UIMAException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;

import de.tudarmstadt.ukp.dkpro.core.castransformation.alignment.AlignedString;
import de.tudarmstadt.ukp.dkpro.core.castransformation.alignment.ImmutableInterval;
import de.tudarmstadt.ukp.dkpro.core.castransformation.alignment.Interval;

/**
 * After processing a file with the {@code ApplyChangesAnnotator} this annotator
 * can be used to map the annotations created in the cleaned view back to the
 * original view.
 *
 * @author Richard Eckart de Castilho
 * @see ApplyChangesAnnotator
 */
public
class Backmapper
extends JCasAnnotator_ImplBase
{
	public static final String PARAM_CHAIN = "Chain";

	protected LinkedList<String> sofaChain = new LinkedList<String>();

	@Override
	public
	void initialize(UimaContext aContext)
	throws ResourceInitializationException
	{
		super.initialize(aContext);

		String[] chain = (String[]) aContext.getConfigParameterValue(PARAM_CHAIN);
		if (chain != null) {
			sofaChain.addAll(asList(chain));
		}
		else {
			sofaChain.add("source");
			sofaChain.add("target");
		}
	}

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

			LinkedList<String> workChain = new LinkedList<String>(sofaChain);
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

		FSIndex<Annotation> idx = targetView.getAnnotationIndex();
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
