/*******************************************************************************
 * Copyright 2012
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.sfst;

import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createAggregate;
import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.Morpheme;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;


public class MorphologyAnnotatorTest

{

    @Test
    public void turkishMorhologyAnnotatorTest()
        throws Exception
    {

        AnalysisEngine ae = getEngine();
        JCas jcas = ae.newJCas();
        jcas.setDocumentLanguage("tr");
        jcas.setDocumentText("Doktor hastanede çalışıyorum.");
        ae.process(jcas);

        String[] lemmas = new String[] { "Doktor", "hastane", "çalış", "."  };

        String[] posOriginal = new String[] { "notAvailable", "n", "v", "notAvailable" };

        AssertAnnotations.assertPOS(null, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
        assertEquals(4, JCasUtil.select(jcas, Morpheme.class).size());
    }
    
    @Test
    public void germanMorhologyAnnotatorTest()
        throws Exception
    {
        AnalysisEngine ae = getEngine();
        JCas jcas = ae.newJCas();
        jcas.setDocumentLanguage("de");
        jcas.setDocumentText("Der Arzt arbeitet im Krankenhaus.");
        ae.process(jcas);
        
        String[] lemmas = new String[] { };

        String[] posOriginal = new String[] { "notAvailable", "notAvailable", "notAvailable",
                "notAvailable", "notAvailable", "notAvailable" };

        AssertAnnotations.assertPOS(null, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
        assertEquals(6, JCasUtil.select(jcas, Morpheme.class).size());
    }
    
    private AnalysisEngine getEngine() throws ResourceInitializationException {
        AnalysisEngineDescription segmenter = createPrimitiveDescription(
                BreakIteratorSegmenter.class
        );

        AnalysisEngineDescription morphology = createPrimitiveDescription(
                SfstAnnotator.class
        );

        AnalysisEngineDescription aggregate = createAggregateDescription(
                segmenter,
                morphology
        );

        return createAggregate(aggregate);
    }
}
