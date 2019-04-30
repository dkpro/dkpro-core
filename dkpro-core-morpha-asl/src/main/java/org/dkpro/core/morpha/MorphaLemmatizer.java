/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.morpha;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.LanguageCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.parameter.ComponentParameters;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Lemmatize based on a finite-state machine. Uses the <a href="https://github.com/knowitall/morpha">
 * Java port</a> of <a href="http://www.informatics.sussex.ac.uk/research/groups/nlp/carroll/morph.html">Morpha</a>.
 *
 * <p>References:</p>
 * <ul>
 * <li>Minnen, G., J. Carroll and D. Pearce (2001). Applied morphological 
 * processing of English, Natural Language Engineering, 7(3). 207-223.</li>
 * </ul>
 */
@Component(OperationType.LEMMATIZER)
@ResourceMetaData(name = "Morpha Lemmatizer")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@LanguageCapability("en")
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
    @ConfigurationParameter(name = PARAM_READ_POS, mandatory = true, defaultValue = "false")
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

                String lemmaString;
                if (readPos && (t.getPos() != null)) {
                    lemmaString = edu.washington.cs.knowitall.morpha.MorphaStemmer.stemToken(
                            t.getText(), t.getPos().getPosValue());
                }
                else {
                    lemmaString = edu.washington.cs.knowitall.morpha.MorphaStemmer
                            .stemToken(t.getText());
                }
                if (lemmaString == null) {
                    lemmaString = t.getText();
                }
                l.setValue(lemmaString);

                l.addToIndexes();

                t.setLemma(l);
            }
        }
    }
}
