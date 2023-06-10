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
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.AssumeResource;
import org.dkpro.core.testing.TestRunner;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;

public class MateMorphTaggerTest
{
    @Test
    public void testGerman()
        throws Exception
    {
        assumeTrue(Runtime.getRuntime().maxMemory() >= 1000000000);

        JCas jcas = runTest("de", "Wir brauchen ein sehr kompliziertes Beispiel , welches "
                + "möglichst viele Konstituenten und Dependenzen beinhaltet .");

        String[] morphTagsExpected = { 
                "[  0,  3]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - Wir (case=nom|number=pl|gender=*|person=1)",
                "[  4, 12]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - brauchen (number=pl|person=1|tense=pres|mood=ind)",
                "[ 13, 16]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - ein (case=acc|number=sg|gender=neut)",
                "[ 17, 21]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - sehr (_)",
                "[ 22, 35]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - kompliziertes (case=acc|number=sg|gender=neut|degree=pos)",
                "[ 36, 44]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - Beispiel (case=acc|number=sg|gender=neut)",
                "[ 45, 46]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - , (_)",
                "[ 47, 54]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - welches (case=acc|number=sg|gender=neut)",
                "[ 55, 64]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - möglichst (_)",
                "[ 65, 70]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - viele (case=acc|number=pl|gender=*)",
                "[ 71, 84]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - Konstituenten (case=acc|number=pl|gender=*)",
                "[ 85, 88]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - und (_)",
                "[ 89,100]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - Dependenzen (case=acc|number=pl|gender=fem)",
                "[101,111]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - beinhaltet (number=sg|person=3|tense=pres|mood=ind)",
                "[112,113]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - . (_)"
        };

        AssertAnnotations.assertMorph(morphTagsExpected, select(jcas, MorphologicalFeatures.class));
    }

    @Test
    public void testFrench()
        throws Exception
    {
        assumeTrue(Runtime.getRuntime().maxMemory() >= 1000000000);

        JCas jcas = runTest("fr", "Nous avons besoin d'une phrase par exemple très "
                + "compliqué, qui contient des constituants que de nombreuses dépendances et que "
                + "possible .");

        String[] morphTagsExpected = { 
                "[  0,  4]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - Nous (g=m|n=p|p=1|s=suj)",
                "[  5, 10]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - avons (m=ind|n=p|p=1|t=pst)",
                "[ 11, 17]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - besoin (g=m|n=s|s=c)",
                "[ 18, 23]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - d'une (_)",
                "[ 24, 30]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - phrase (g=f|n=s|s=c)",
                "[ 31, 34]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - par (_)",
                "[ 35, 42]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - exemple (g=m|n=s|s=c)",
                "[ 43, 47]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - très (_)",
                "[ 48, 58]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - compliqué, (g=m|n=s|s=qual)",
                "[ 59, 62]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - qui (g=m|n=p|p=3|s=rel)",
                "[ 63, 71]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - contient (m=ind|n=s|p=3|t=pst)",
                "[ 72, 75]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - des (g=m|n=p|s=ind)",
                "[ 76, 88]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - constituants (g=m|n=p|s=c)",
                "[ 89, 92]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - que (g=m|n=p|p=3|s=rel)",
                "[ 93, 95]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - de (g=f|n=p|s=ind)",
                "[ 96,106]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - nombreuses (g=f|n=p|s=qual)",
                "[107,118]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - dépendances (g=f|n=p|s=c)",
                "[119,121]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - et (s=c)",
                "[122,125]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - que (s=s)",
                "[126,134]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - possible (g=m|n=s|s=qual)",
                "[135,136]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - . (s=s)"
        };

        AssertAnnotations.assertMorph(morphTagsExpected, select(jcas, MorphologicalFeatures.class));
    }

    @Test
    public void testSpanish()
        throws Exception
    {
        assumeTrue(Runtime.getRuntime().maxMemory() >= 1000000000);

        JCas jcas = runTest("es", "Necesitamos una oración de ejemplo muy complicado , que "
                + "contiene la mayor cantidad de componentes y dependencias como sea posible .");

        String[] morphTagsExpected = {
                "[  0, 11]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - Necesitamos (postype=main|gen=c|num=p|person=1|mood=indicative|tense=present)",
                "[ 12, 15]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - una (postype=indefinite|gen=f|num=s)",
                "[ 16, 23]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - oración (postype=common|gen=f|num=s)",
                "[ 24, 26]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - de (postype=preposition|gen=c|num=c)",
                "[ 27, 34]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - ejemplo (postype=common|gen=m|num=s)",
                "[ 35, 38]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - muy (_)",
                "[ 39, 49]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - complicado (postype=qualificative|gen=m|num=s|posfunction=participle)",
                "[ 50, 51]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - , (punct=comma)",
                "[ 52, 55]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - que (postype=relative|gen=c|num=c)",
                "[ 56, 64]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - contiene (postype=main|gen=c|num=s|person=3|mood=indicative|tense=present)",
                "[ 65, 67]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - la (postype=article|gen=f|num=s)",
                "[ 68, 73]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - mayor (postype=qualificative|gen=c|num=s)",
                "[ 74, 82]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - cantidad (postype=common|gen=f|num=s)",
                "[ 83, 85]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - de (postype=preposition|gen=c|num=c)",
                "[ 86, 97]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - componentes (postype=common|gen=m|num=p)",
                "[ 98, 99]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - y (postype=coordinating)",
                "[100,112]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - dependencias (postype=common|gen=f|num=p)",
                "[113,117]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - como (postype=subordinating)",
                "[118,121]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - sea (postype=semiauxiliary|gen=c|num=s|person=3|mood=subjunctive|tense=present)",
                "[122,129]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - posible (postype=qualificative|gen=c|num=s)",
                "[130,131]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - . (punct=period)"
        };

        AssertAnnotations.assertMorph(morphTagsExpected, select(jcas, MorphologicalFeatures.class));
    }

    private JCas runTest(String aLanguage, String aText)
        throws Exception
    {
        assumeTrue(Runtime.getRuntime().maxMemory() >= 2000000000);
        
        AssumeResource.assumeResource(MateMorphTagger.class, "morphtagger", aLanguage, null);

        AnalysisEngineDescription lemma = createEngineDescription(MateLemmatizer.class);
        AnalysisEngineDescription morphTag = createEngineDescription(MateMorphTagger.class);

        AnalysisEngineDescription aggregate = createEngineDescription(lemma, morphTag);

        return TestRunner.runTest(aggregate, aLanguage, aText);
    }
}
