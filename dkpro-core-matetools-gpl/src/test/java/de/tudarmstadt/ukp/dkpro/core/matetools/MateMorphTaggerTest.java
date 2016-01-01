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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.core.matetools;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.Morpheme;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class MateMorphTaggerTest
{
    @Test
    public void testGerman()
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() >= 1000000000);

        JCas jcas = runTest("de", "Wir brauchen ein sehr kompliziertes Beispiel , welches "
                + "möglichst viele Konstituenten und Dependenzen beinhaltet .");

        String[] lemmas = { "wir", "brauchen", "ein", "sehr", "kompliziert",
                "Beispiel", "--", "welcher", "möglichst", "vieler", "Konstituent", "und",
                "Dependenz", "beinhalten", "--" };

        String[] morphTagsExpected = { 
                "[  0,  3]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - Wir (nom|pl|*|1)",
                "[  4, 12]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - brauchen (pl|1|pres|ind)",
                "[ 13, 16]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - ein (acc|sg|neut)",
                "[ 17, 21]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - sehr (_)",
                "[ 22, 35]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - kompliziertes (nom|sg|neut|pos)",
                "[ 36, 44]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - Beispiel (nom|sg|neut)",
                "[ 45, 46]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - , (_)",
                "[ 47, 54]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - welches (nom|sg|neut)",
                "[ 55, 64]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - möglichst (_)",
                "[ 65, 70]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - viele (nom|pl|*)",
                "[ 71, 84]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - Konstituenten (gen|pl|masc)",
                "[ 85, 88]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - und (_)",
                "[ 89,100]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - Dependenzen (gen|pl|masc)",
                "[101,111]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - beinhaltet (sg|3|pres|ind)",
                "[112,113]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - . (_)" };

        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
        AssertAnnotations.assertMorph(morphTagsExpected, select(jcas, MorphologicalFeatures.class));
    }

    @Test
    public void testFrench()
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() >= 1000000000);

        JCas jcas = runTest("fr", "Nous avons besoin d'une phrase par exemple très "
                + "compliqué, qui contient des constituants que de nombreuses dépendances et que "
                + "possible .");

        String[] lemmas = new String[] { "il", "avoir", "besoin", "d'une", "phrase", "par",
                "exemple", "très", "compliqué,", "qui", "contenir", "de", "constituant", "que",
                "de", "nombreux", "dépendance", "et", "que", "possible", "." };

        String[] morphTagsExpected = { "nom|pl|masc|pos", "acc|sg|neut", "nom|pl|neut",
                "nom|pl|neut", "_", "acc|sg|neut", "_", "nom|pl|neut", "nom|pl|neut", "*|*|*",
                "nom|pl|neut", "nom|pl|neut", "dat|sg|masc", "acc|pl|*", "gen|sg|masc",
                "nom|pl|neut", "acc|sg|fem|sup", "nom|pl|neut", "nom|pl|neut", "nom|pl|neut" };

        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
        AssertAnnotations.assertMorpheme(morphTagsExpected, select(jcas, Morpheme.class));
    }

    @Test
    public void testSpanish()
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() >= 1000000000);

        JCas jcas = runTest("es", "Necesitamos una oración de ejemplo muy complicado , que "
                + "contiene la mayor cantidad de componentes y dependencias como sea posible .");

        String[] lemmas = new String[] { "necesitar", "uno", "oración", "de", "ejemplo", "mucho",
                "complicado", ",", "que", "contener", "el", "mayor", "cantidad", "de",
                "componente", "y", "dependencia", "como", "ser", "posible", "."};

        String[] morphTagsExpected = {
                "postype=main|gen=c|num=p|person=1|mood=indicative|tense=present",
                "postype=indefinite|gen=f|num=s", "postype=common|gen=f|num=s",
                "postype=preposition|gen=c|num=c", "postype=common|gen=m|num=s", "_",
                "postype=qualificative|gen=m|num=s|posfunction=participle", "punct=comma",
                "postype=relative|gen=c|num=c",
                "postype=main|gen=c|num=s|person=3|mood=indicative|tense=present",
                "postype=article|gen=f|num=s", "postype=qualificative|gen=c|num=s",
                "postype=common|gen=f|num=s", "postype=preposition|gen=c|num=c",
                "postype=common|gen=m|num=p", "postype=coordinating", "postype=common|gen=f|num=p",
                "postype=subordinating",
                "postype=semiauxiliary|gen=c|num=s|person=3|mood=subjunctive|tense=present",
                "postype=qualificative|gen=c|num=s", "punct=period"};

        AssertAnnotations.assertLemma(lemmas, select(jcas, Lemma.class));
        AssertAnnotations.assertMorpheme(morphTagsExpected, select(jcas, Morpheme.class));
    }

    private JCas runTest(String aLanguage, String aText)
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() >= 2000000000);

        AnalysisEngineDescription lemma = createEngineDescription(MateLemmatizer.class);
        AnalysisEngineDescription morphTag = createEngineDescription(MateMorphTagger.class);

        AnalysisEngineDescription aggregate = createEngineDescription(lemma, morphTag);

        return TestRunner.runTest(aggregate, aLanguage, aText);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
