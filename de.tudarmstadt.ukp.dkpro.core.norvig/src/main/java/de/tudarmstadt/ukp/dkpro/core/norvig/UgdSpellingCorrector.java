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
package de.tudarmstadt.ukp.dkpro.core.norvig;

import static org.uimafit.util.JCasUtil.iterate;
import static org.uimafit.util.JCasUtil.selectCovered;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SpellingAnomaly;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation;

/**
 * Creates {@link SofaChangeAnnotation}s containing corrections for previously
 * identified spelling errors.
 *
 * @author <a href="eckartde@tk.informatik.tu-darmstadt.de">Richard Eckart de Castilho</a>
 * @author delphine
 */
public
class UgdSpellingCorrector
extends JCasAnnotator_ImplBase
{
	public static final String PARAM_MODEL_FILE = "ModelFile";

	private NorvigSpellingCorrector spellingCorrector;

	@Override
	public
	void initialize(
			UimaContext context)
	throws ResourceInitializationException
	{
		super.initialize(context);
		try {
			String trainingFile = (String) context
					.getConfigParameterValue(PARAM_MODEL_FILE);

			spellingCorrector = new NorvigSpellingCorrector();
			spellingCorrector.train(getContext().getResourceURL(trainingFile), "UTF-8");
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public
	void process(
			JCas jcas)
	throws AnalysisEngineProcessException
	{
		for (Token t : iterate(jcas, Token.class)) {
			String token = t.getCoveredText();

			// If there is no spelling error in this token, then we do not
			// have to correct it.
			if (selectCovered(SpellingAnomaly.class, t).size() == 0) {
				continue; // No mistake here
			}

			String correction = spellingCorrector.correct(token);

			if (!correction.equals(token)) {
				// Create change annotation
				SofaChangeAnnotation change = new SofaChangeAnnotation(
						jcas, t.getBegin(), t.getEnd());
				change.setValue(correction);
				change.setReason("spelling error");
				change.setOperation("replace");
				change.addToIndexes();
			}
		}
	}
}
