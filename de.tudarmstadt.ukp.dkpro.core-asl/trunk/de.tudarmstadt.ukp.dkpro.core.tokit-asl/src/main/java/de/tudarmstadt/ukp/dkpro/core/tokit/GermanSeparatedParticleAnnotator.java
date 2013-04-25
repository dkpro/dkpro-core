/*******************************************************************************
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.tokit;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.TypeCapability;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Annotator to be used for post-processing of German corpora that have been lemmatized and POS-tagged with the
 * TreeTagger, based on the STTS tagset.
 *
 * This Annotator deals with German particle verbs. Particle verbs consist of a particle and a stem, e.g. anfangen = an+fangen
 * There are many usages of German particle verbs where the stem and the particle are separated, e.g., Wir fangen gleich an.
 * The TreeTagger lemmatizes the verb stem as "fangen" and the separated particle as "an",
 * the proper verblemma "anfangen" is thus not available as an annotation.
 * The GermanSeparatedParticleAnnotator replaces the lemma of the stem of particle-verbs (e.g., fangen) by the proper verb lemma
 * (e.g. anfangen) and leaves the lemma of the separated particle unchanged.
 *
 * @author Judith Eckle-Kohler
 *
 */
@TypeCapability(
        inputs={
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma"},
        outputs={
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma"})

public class GermanSeparatedParticleAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {

		for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
			List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class, sentence);
			for (int i = 0; i < tokens.size(); i++) {
				Token token = tokens.get(i);
				if (token.getPos() != null) {
					if (token.getPos().getPosValue().matches("PTKVZ.*")) {
					// go back and find the next finite verb
						String particle = token.getCoveredText();
						String verblemma = "";

						int j = i-1;
						while (j >= 0){
							Token t = tokens.get(j);
							if (t.getLemma() != null && t.getPos() != null) {
								if (t.getPos().getPosValue().matches("V.*FIN")) {
									verblemma = t.getLemma().getValue();
									Lemma l = t.getLemma();
									l.setValue(particle +verblemma);
									break;
									//l.addToIndexes(); // do not add to indexes: creates Lemma twice
								}
							}
							j--;
						}

					}
				}

			} // for all tokens in the sentence

		} // for all sentences

	} // process


} // class
