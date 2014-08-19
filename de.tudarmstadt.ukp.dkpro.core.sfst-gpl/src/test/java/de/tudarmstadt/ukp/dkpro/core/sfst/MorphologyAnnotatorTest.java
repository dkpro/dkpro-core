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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.*;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.Morpheme;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
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

        String[] morphemes = new String[] { "Doktor", "hastane<n><loc>", "çalış<v><t_cont><1s>", "." };
        
        assertPOS(null, posOriginal, select(jcas, POS.class));
        assertLemma(lemmas, select(jcas, Lemma.class));
        assertMorpheme(morphemes, select(jcas, Morpheme.class));
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

        String[] morphemes = new String[] { "<CAP>die<+ART><Def><Fem><Gen><Sg>",
                "Arzt<+NN><Masc><Nom><Sg>", "arbeiten<+V><Imp><Pl>",
                "im<+PREP/ART><Masc><Dat><Sg>", "kranken<V><NN><SUFF>Haus<+NN><Neut><Nom><Sg>",
                ".<+IP><Norm>" };

        assertPOS(null, posOriginal, select(jcas, POS.class));
        assertLemma(lemmas, select(jcas, Lemma.class));
        assertMorpheme(morphemes, select(jcas, Morpheme.class));
    }

    @Test
    public void testItalian()
        throws Exception
    {
        AnalysisEngine ae = getEngine();
        JCas jcas = ae.newJCas();
        jcas.setDocumentLanguage("it");
        jcas.setDocumentText("Il medico che lavora in ospedale.");
        ae.process(jcas);

        String[] lemmas = new String[] { };

        String[] posOriginal = new String[] { "notAvailable", "notAvailable", "notAvailable",
                "notAvailable", "notAvailable", "notAvailable", "notAvailable" };

        String[] morphemes = new String[] { "Il", "medico<ADJ><pos><m><s>", "che<CON>",
                "lavorare<VER><impr><pres><2><s>", "in<PRE>", "ospedale<NOUN><M><s>", ".<SENT>" };

        assertPOS(null, posOriginal, select(jcas, POS.class));
        assertLemma(lemmas, select(jcas, Lemma.class));
        assertMorpheme(morphemes, select(jcas, Morpheme.class));
    }

    private AnalysisEngine getEngine() throws ResourceInitializationException {
        AnalysisEngineDescription segmenter = createEngineDescription(
                BreakIteratorSegmenter.class
        );

        AnalysisEngineDescription morphology = createEngineDescription(
                SfstAnnotator.class
        );

        AnalysisEngineDescription aggregate = createEngineDescription(
                segmenter,
                morphology
        );

        return createEngine(aggregate);
    }
}
