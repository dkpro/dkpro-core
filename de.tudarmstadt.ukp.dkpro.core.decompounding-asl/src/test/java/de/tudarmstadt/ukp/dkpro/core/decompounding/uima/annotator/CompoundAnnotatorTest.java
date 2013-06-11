/*******************************************************************************
 * Copyright 2010
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
 *******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.decompounding.uima.annotator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.testing.factory.TokenBuilder;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.CompoundPart;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.LinkingMorpheme;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Split;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.decompounding.utils.TestUtils;

public class CompoundAnnotatorTest
{

    @Test
    public void testWithDefaults()
        throws CASException, UIMAException
    {
        runAnnotator(TestUtils.getDefaultCompoundAnnotatorDescription());
    }

    private void runAnnotator(final AnalysisEngineDescription aed)
        throws CASException, UIMAException
    {
        // Create Analysis Engine
        final AnalysisEngine analysisEngine = AnalysisEngineFactory.createAggregate(aed);

        // Create cas with token
        final CAS cas = analysisEngine.newCAS();
        final TokenBuilder<Token, Annotation> builder = new TokenBuilder<Token, Annotation>(
                Token.class, Annotation.class);
        builder.buildTokens(cas.getJCas(), "Aktionsplan im Doppelprozessormaschine");
        analysisEngine.typeSystemInit(cas.getTypeSystem());
        analysisEngine.process(cas);

        final String[] splits = new String[] { "Aktion", "s", "plan", "Doppel",
                "prozessormaschine", "prozessor", "maschine" };
        final String[] compounds = new String[] { "Aktionsplan", "Doppelprozessormaschine" };
        final String[] compoundsParts = new String[] { "Aktion", "plan", "Doppel",
                "prozessormaschine", "prozessor", "maschine" };
        final String[] linkingMorphemes = new String[] { "s" };

        // Check if splits and morphemes are equal
        assertThat(getAnnotation(cas.getJCas(), Compound.class), is(compounds));
        assertThat(getAnnotation(cas.getJCas(), Split.class), is(splits));
        assertThat(getAnnotation(cas.getJCas(), CompoundPart.class), is(compoundsParts));
        assertThat(getAnnotation(cas.getJCas(), LinkingMorpheme.class), is(linkingMorphemes));
    }

    public static <T extends Annotation> String[] getAnnotation(final JCas aCas,
            final Class<T> aClass)
    {
        final List<String> result = new ArrayList<String>();
        for (T s : JCasUtil.select(aCas, aClass)) {
            result.add(s.getCoveredText());
        }

        return result.toArray(new String[] {});
    }

}
