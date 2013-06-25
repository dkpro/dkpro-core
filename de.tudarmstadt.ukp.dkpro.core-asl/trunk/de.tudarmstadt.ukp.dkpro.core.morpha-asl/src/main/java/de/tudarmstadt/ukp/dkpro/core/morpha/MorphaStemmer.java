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
package de.tudarmstadt.ukp.dkpro.core.morpha;

import static org.uimafit.util.JCasUtil.select;
import static org.uimafit.util.JCasUtil.selectCovered;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.TypeCapability;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Lemmatizer using Morpha.
 * 
 * @author Richard Eckart de Castilho
 */
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" }, outputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma" })
public class MorphaStemmer
    extends JCasAnnotator_ImplBase
{
    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        // Iterate over all sentences
        for (Sentence sentence : select(aJCas, Sentence.class)) {
            List<Token> tokens = selectCovered(aJCas, Token.class, sentence);

            for (Token t : tokens) {
                Stem l = new Stem(aJCas, t.getBegin(), t.getEnd());
                
                if (t.getPos() != null) {
                    l.setValue(edu.washington.cs.knowitall.morpha.MorphaStemmer.stemToken(t
                            .getCoveredText(), t.getPos().getPosValue()));
                }
                else {
                    l.setValue(edu.washington.cs.knowitall.morpha.MorphaStemmer.stemToken(t
                            .getCoveredText()));
                }
                l.addToIndexes();

                t.setStem(l);
            }
        }
    }
}
