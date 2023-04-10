/*
 * Copyright 2007-2023
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
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.core.matetools;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.dkpro.core.testing.AssertAnnotations.assertSemPred;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.matetools.MateLemmatizer;
import org.dkpro.core.matetools.MateMorphTagger;
import org.dkpro.core.matetools.MateParser;
import org.dkpro.core.matetools.MatePosTagger;
import org.dkpro.core.matetools.MateSemanticRoleLabeler;
import org.dkpro.core.testing.AssumeResource;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.TestRunner;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemPred;

public class MateSemanticRoleLabelerTest
{
    @Test
    public void testEnglish()
        throws Exception
    {
        JCas jcas = runTest(
                "en",
                "The economy 's temperature will be taken from several vantage points this week , with readings on trade , output , housing and inflation .");

        String[] predicates = { 
                "readings (reading.01): [(A1:on)]",
                "taken (take.01): [(A1:temperature)(A2:from)(AM-ADV:with)(AM-MOD:will)(AM-TMP:week)]",
                "temperature (temperature.01): [(A1:economy)]" };

        assertSemPred(predicates, select(jcas, SemPred.class));
    }

    @Test
    public void testGerman()
        throws Exception
    {
        JCas jcas = runTest(
                "de",
                "Wir brauchen ein sehr kompliziertes Beispiel , welches möglichst viele Konstituenten und Dependenzen beinhaltet .");

        String[] predicates = { "brauchen (brauchen.1): [(A0:Wir)(A3:Beispiel)]" };

        assertSemPred(predicates, select(jcas, SemPred.class));
    }

    private JCas runTest(String aLanguage, String aText)
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() >= 2000000000);

        AssumeResource.assumeResource(MateSemanticRoleLabeler.class, "srl", aLanguage, null);
        
        AnalysisEngineDescription aggregate;

        if (aLanguage == "en") {
            aggregate = createEngineDescription(createEngineDescription(MatePosTagger.class),
                    createEngineDescription(MateLemmatizer.class),
                    createEngineDescription(MateParser.class),
                    createEngineDescription(MateSemanticRoleLabeler.class));
        }
        else if (aLanguage == "de") {
            aggregate = createEngineDescription(createEngineDescription(MatePosTagger.class),
                    createEngineDescription(MateLemmatizer.class),
                    createEngineDescription(MateMorphTagger.class),
                    createEngineDescription(MateParser.class),
                    createEngineDescription(MateSemanticRoleLabeler.class));
        }
        else {
            throw new Exception("unkown language " + aLanguage);
        }

        return TestRunner.runTest(aggregate, aLanguage, aText);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
