/**
 * Copyright 2007-2014
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
package de.tudarmstadt.ukp.dkpro.core.matetools;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticArgument;
import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticPredicate;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class MateSemanticRoleLabelerTest
{
    @Test
    public void testEnglish()
        throws Exception
    {
        JCas jcas = runTest(
                "en",
                "The economy 's temperature will be taken from several vantage points this week , with readings on trade , output , housing and inflation .");

        String[] predicates = new String[] { "readings (reading.01): [[(A1:on)]]",
                "taken (take.01): [[(A2:from), (AM-ADV:with), (AM-MOD:will), (AM-TMP:week)]]",
                "temperature (temperature.01): [[(A1:economy)]]" };

        assertSemanticPredicates(predicates, select(jcas, SemanticPredicate.class));

    }

    @Test
    public void testGerman()
        throws Exception
    {
        JCas jcas = runTest(
                "de",
                "Wir brauchen ein sehr kompliziertes Beispiel , welches möglichst viele Konstituenten und Dependenzen beinhaltet .");

        String[] predicates = new String[] { "brauchen (brauchen.1): [[(A0:Wir), (A3:Beispiel)]]" };

        assertSemanticPredicates(predicates, select(jcas, SemanticPredicate.class));

    }

    private JCas runTest(String aLanguage, String aText)
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() >= 2000000000);

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

    private void assertSemanticPredicates(String[] aExpected, Collection<SemanticPredicate> aActual)
    {
        List<String> expected = new ArrayList<String>(asList(aExpected));
        List<String> actual = new ArrayList<String>();

        for (SemanticPredicate p : aActual) {
            StringBuilder sb = new StringBuilder();
            sb.append(p.getCoveredText()).append(" (").append(p.getCategory()).append("): [");

            List<String> arguments = new ArrayList<String>();
            for (SemanticArgument a : select(p.getArguments(), SemanticArgument.class)) {
                arguments.add(String.format("(%s:%s)", a.getRole(), a.getCoveredText()));
            }

            Collections.sort(arguments);

            sb.append(arguments.toString());
            sb.append(']');
            actual.add(sb.toString());
        }

        Collections.sort(actual);

        System.out.printf("%-20s - Expected: %s%n", "Semantic predicates",
                AssertAnnotations.asCopyableString(expected, false));
        System.out.printf("%-20s - Actual  : %s%n", "Semantic predicates",
                AssertAnnotations.asCopyableString(actual, false));

        assertEquals(AssertAnnotations.asCopyableString(expected, true),
                AssertAnnotations.asCopyableString(actual, true));
    }
}
