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

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Stemmer using Morpha.
 * 
 * @author Richard Eckart de Castilho
 */
@TypeCapability(
        inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                    "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                    "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS" }, 
        outputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma" })
public class MorphaLemmatizer
    extends JCasAnnotator_ImplBase
{
    /**
     * Pass part-of-speech information on to Morpha. Since we currently do not know in which format
     * the part-of-speech tags are expected by Morpha, we just pass on the actual pos tag value
     * we get from the token. This may produce worse results than not passing on pos tags at all,
     * so this is disabled by default.
     */
    public static final String PARAM_READ_POS = ComponentParameters.PARAM_READ_POS;
    @ConfigurationParameter(name=PARAM_READ_POS, mandatory=true, defaultValue="false")
    private boolean readPos;
    
    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        // Iterate over all sentences
        for (Sentence sentence : select(aJCas, Sentence.class)) {
            List<Token> tokens = selectCovered(aJCas, Token.class, sentence);

            for (Token t : tokens) {
                Lemma l = new Lemma(aJCas, t.getBegin(), t.getEnd());

                if (readPos && (t.getPos() != null)) {
                    l.setValue(edu.washington.cs.knowitall.morpha.MorphaStemmer.stemToken(
                            t.getCoveredText(), t.getPos().getPosValue()));
                }
                else {
                    l.setValue(edu.washington.cs.knowitall.morpha.MorphaStemmer.stemToken(t
                            .getCoveredText()));
                }
                l.addToIndexes();

                t.setLemma(l);
            }
        }
    }
}
