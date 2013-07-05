/*******************************************************************************
 * Copyright 2010
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp;

import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import edu.stanford.nlp.ling.WordTag;
import edu.stanford.nlp.process.Morphology;

/**
 * Stanford Lemmatizer component.
 */
public class StanfordLemmatizer
	extends JCasAnnotator_ImplBase
{
	private Morphology morphology;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		morphology = new Morphology();
	}

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		for (Token t : select(aJCas, Token.class)) {
			// Only verbs are lemmatized, the other words are simply stemmed. This corresponds
			// roughly to what is happening in MorphaAnnotator.
			String token = t.getCoveredText();
			String lemma;
			if (t.getPos() instanceof V) {
				lemma = morphology.lemmatize(new WordTag(token, t.getPos().getPosValue()))
						.lemma();
			}
			else {
				lemma = morphology.stem(token);
			}
			Lemma l = new Lemma(aJCas, t.getBegin(), t.getEnd());
			l.setValue(lemma);
			l.addToIndexes();
			t.setLemma(l);
		}
	}
}
