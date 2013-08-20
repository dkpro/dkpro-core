/*******************************************************************************
 * Copyright 2013
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
package de.tudarmstadt.ukp.dkpro.core.dictionaryannotator.semantictagging;

import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

@TypeCapability(
		inputs = {"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
				"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
				"de.tudarmstadt.ukp.dkpro.core.lexmorph.type.POS"},
		outputs = {
				"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.NamedEntity"})


/**
 * 
 * This Analysis Engine annotates 
 * English common nouns with semantic field information from WordNet.
 * The annotation is stored in the NamedEntity annotation type.
 *     
 * @author Judith Eckle-Kohler
 * 
 */
public class NounSemanticFieldAnnotator extends JCasAnnotator_ImplBase
{
	
	public static final String PARAM_NOUN_SEMANTIC_FIELD_RESOURCE = "nounSemanticFieldResource";
	@ExternalResource(key = PARAM_NOUN_SEMANTIC_FIELD_RESOURCE)	
	private SemanticTagResource nounSemanticFieldResource;
	
	
	@Override
	public void initialize(final UimaContext context)
			throws ResourceInitializationException
	{
		super.initialize(context);
	}

	
	@Override
	public void process(JCas aJCas)
			throws AnalysisEngineProcessException
	{
		// Note: it is not clear yet, if NN.class or N.class is the POS type which should be used here
		// the 12 universal POS tags include only N
		for (NN nn : select(aJCas, NN.class)) {
			for (Token token : JCasUtil.selectCovered(aJCas, Token.class, nn)) {
				try {
					String semanticField = nounSemanticFieldResource.getSemanticTag(token);
					NamedEntity semanticFieldAnnotation = new NamedEntity(aJCas, token.getBegin(), token.getEnd());
					semanticFieldAnnotation.setValue(semanticField);
					semanticFieldAnnotation.addToIndexes();
				} catch (Exception e) {
					throw new AnalysisEngineProcessException(e);
				}

			}
			
		}				
	}


}
