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
package de.tudarmstadt.ukp.dkpro.core.frequency.resources;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.ExternalResourceFactory.createExternalResourceDescription;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.junit.Test;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ExternalResource;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.FrequencyCountResourceBase;

public class BerkeleyLMFrequencyCountProviderTest
{
    public static class Annotator extends JCasAnnotator_ImplBase {
        final static String MODEL_KEY = "FrequencyProvider";
        @ExternalResource(key = MODEL_KEY)
        private FrequencyCountResourceBase model;

        @Override
        public void process(JCas aJCas)
            throws AnalysisEngineProcessException
        {
            System.out.println(model.getClass().getName());
        }
    }

    @Test
    public void configureAggregatedExample() throws Exception {
        AnalysisEngineDescription desc = createPrimitiveDescription(Annotator.class,
        		Annotator.MODEL_KEY, createExternalResourceDescription(
        				BerkeleyLMFrequencyCountProvider.class,
                        BerkeleyLMFrequencyCountProvider.PARAM_BINARY, "src/test/resources/test.ser"));
        
        // Check the external resource was injected
        AnalysisEngine ae = createPrimitive(desc);
        ae.process(ae.newJCas());
    }
}
