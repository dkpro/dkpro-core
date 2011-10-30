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

import static org.apache.uima.util.Level.INFO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.annolab.core.util.AlignedString;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation;

/**
 * Applies changes annotated using a {@link SofaChangeAnnotation}.
 *
 * @author Richard Eckart de Castilho
 * @since 1.1.0
 * @see Backmapper
 */
public class ApplyChangesAnnotator
	extends JCasAnnotator_ImplBase
{
	static private final String OP_INSERT = "insert";
	static private final String OP_REPLACE = "replace";
	static private final String OP_DELETE = "delete";

	protected String sourceSofaId = "source";
	protected String targetSofaId = "target";

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		try {
			JCas sourceView = aJCas.getView(sourceSofaId);
			JCas targetView = aJCas.createView(targetSofaId);
			DocumentMetaData.copy(sourceView, targetView);
			applyChanges(sourceView, targetView);
		}
		catch (CASException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	protected void applyChanges(JCas aSourceView, JCas aTargetView)
	{
		FSIndex<Annotation> idx = aSourceView.getAnnotationIndex(SofaChangeAnnotation.type);

		getContext().getLogger().log(INFO, "Found " + idx.size() + " changes");

		// Apply all the changes
		AlignedString as = new AlignedString(aSourceView.getDocumentText());

		// Collect all those edits that are going to be executed.
		//
		// | A | C1 C2 R
		// BBBBBB + - -
		// BBBBBBBBBB + + +
		// BBBBBBBBBBBBBBBBB + + +
		// BBBBBBB - + -
		// BBBBBBBBBBBBB - + -
		// BBBBBBBB - + -
		//
		if (idx.size() > 0) {
			List<SofaChangeAnnotation> edits = new ArrayList<SofaChangeAnnotation>();
			{
				// Get an iterator over all the change annotations. Per UIMA default
				// this iterator is sorted first by begin and then by end offsets.
				// We will make use of this fact here to skip change annotations that
				// are covered by others. The earliest longest change wins - this means
				// the one with the smallest begin offset and the largest end offset.
				FSIterator<Annotation> it = idx.iterator();

				SofaChangeAnnotation top = (SofaChangeAnnotation) it.get();
				edits.add(top);
				it.moveToNext();
				while (it.isValid()) {
					SofaChangeAnnotation b = (SofaChangeAnnotation) it.get();
					if (((top.getBegin() <= b.getBegin()) && // C1
							(top.getEnd() > b.getBegin()) // C2
							)
							|| ((top.getBegin() == b.getBegin()) && (top.getEnd() == b.getEnd()))) {
						// Found annotation covering current annotation. Skipping
						// current annotation.
					}
					else {
						top = b;
						edits.add(top);
					}
					it.moveToNext();
				}
			}

			// If we remove or add stuff all offsets right of the change location
			// will change and thus the offsets in the change annotation are no
			// longer valid. If we move from right to left it works better because
			// the left offsets remain stable.
			Collections.reverse(edits);
			for (SofaChangeAnnotation a : edits) {
				if (OP_INSERT.equals(a.getOperation())) {
					// getContext().getLogger().log(INFO,
					// "Performing insert: "+a.getBegin()+"-"+a.getEnd());
					as.insert(a.getBegin(), a.getValue());
				}
				if (OP_DELETE.equals(a.getOperation())) {
					// getContext().getLogger().log(INFO,
					// "Performing delete: "+a.getBegin()+"-"+a.getEnd());
					as.delete(a.getBegin(), a.getEnd());
				}
				if (OP_REPLACE.equals(a.getOperation())) {
					// getContext().getLogger().log(INFO,
					// "Performing replace: "+a.getBegin()+"-"+a.getEnd());
					as.replace(a.getBegin(), a.getEnd(), a.getValue());
				}
			}
		}

		// Set the text of the new Sofa
		aTargetView.setDocumentText(as.get());

		// Set document language
		aTargetView.setDocumentLanguage(aSourceView.getDocumentLanguage());

		// Optionally we may want to remember the AlignedString for the
		// Backmapper.
		AlignmentStorage.getInstance().put(aSourceView.getCasImpl().getBaseCAS(),
				aSourceView.getViewName(), aTargetView.getViewName(), as);
	}
}
