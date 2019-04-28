/*
 * Copyright 2017
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
 */
package org.dkpro.core.norvig;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.parameter.ComponentParameters;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SpellingAnomaly;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation;
import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.Parameters;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Identifies spelling errors using Norvig's algorithm.
 */
@Component(OperationType.SPELLING_CHECKER)
@ResourceMetaData(name = "Simple Spelling Corrector")
@Parameters(
        exclude = { 
                NorvigSpellingCorrector.PARAM_MODEL_LOCATION  })
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token"},
        outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation"})
public class NorvigSpellingCorrector
    extends JCasAnnotator_ImplBase
{
    /**
     * Location from which the model is read. This is either a local path or a classpath location.
     * In the latter case, the model artifact (if any) is searched as well.
     */
    public static final String PARAM_MODEL_LOCATION = ComponentParameters.PARAM_MODEL_LOCATION;
    @ConfigurationParameter(name = PARAM_MODEL_LOCATION, mandatory = false)
    private String modelLocation;

    private NorvigSpellingAlgorithm spellingCorrector;

    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);
        try {
            spellingCorrector = new NorvigSpellingAlgorithm();
            spellingCorrector.train(getContext().getResourceURL(modelLocation), "UTF-8");
        }
        catch (Exception e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        for (Token t : select(jcas, Token.class)) {
            String token = t.getCoveredText();

            // If there is no spelling error in this token, then we do not
            // have to correct it.
            if (selectCovered(SpellingAnomaly.class, t).size() == 0) {
                continue; // No mistake here
            }

            String correction = spellingCorrector.correct(token);

            if (!correction.equals(token)) {
                // Create change annotation
                SofaChangeAnnotation change = new SofaChangeAnnotation(jcas, t.getBegin(),
                        t.getEnd());
                change.setValue(correction);
                change.setReason("spelling error");
                change.setOperation("replace");
                change.addToIndexes();
            }
        }
    }
}
