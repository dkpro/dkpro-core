/**
 * Copyright 2013
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.tudarmstadt.ukp.dkpro.core.sfst;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertEquals;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

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
