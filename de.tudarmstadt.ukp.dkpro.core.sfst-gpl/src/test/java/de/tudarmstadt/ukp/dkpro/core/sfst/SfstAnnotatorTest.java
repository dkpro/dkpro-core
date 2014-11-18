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
package de.tudarmstadt.ukp.dkpro.core.sfst;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;
import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.*;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class SfstAnnotatorTest
{
    @Test
    public void testTurkish()
        throws Exception
    {
        JCas jcas = runTest("tr", "trmorph-ca", "Doktor hastane çalış .");

        String[] morphemes = new String[] { 
                "[  0,  6]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - Doktor ()",
                "[  7, 14]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - hastane (hastane<n>)",
                "[  7, 14]     -     -    -    -    -     -    -    -  Plur      -  3    -    -    -     -      -     - hastane (hastane<n><3p>)",
                "[  7, 14]     -     -    -    -    -     -    -    -  Sing      -  3    -    -    -     -      -     - hastane (hastane<n><3s>)",
                "[ 15, 20]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - çalış (çal<v><vn_yis>)",
                "[ 15, 20]     -     -    -    -    -     -    -    -  Plur      -  3    -    -    -     -      -     - çalış (çalış<v><t_imp><3p>)",
                "[ 15, 20]     -     -    -    -    -     -    -    -  Sing      -  2    -    -    -     -      -     - çalış (çalış<v><t_imp><2s>)",
                "[ 15, 20]     -     -    -    -    -     -    -    -  Plur      -  3    -    -    -     -      -     - çalış (çal<v><D_yIS><n><3p>)",
                "[ 15, 20]     -     -    -    -    -     -    -    -  Sing      -  3    -    -    -     -      -     - çalış (çal<v><vn_yis><3s>)",
                "[ 15, 20]     -     -    -    -    -     -    -    -  Sing      -  3    -    -    -     -      -     - çalış (çal<v><D_yIS><n><3s>)",
                "[ 15, 20]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - çalış (çal<v><D_yIS><n>)",
                "[ 15, 20]     -     -    -    -    -     -    -    -  Plur      -  3    -    -    -     -      -     - çalış (çal<v><vn_yis><3p>)",
                "[ 21, 22]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - . (.<pnct>)" };
        
        assertMorph(morphemes, select(jcas, MorphologicalFeatures.class));
    }

    @Test
    public void testGermanMorphisto()
        throws Exception
    {
        JCas jcas = runTest("de", "morphisto-ca", "Der Arzt arbeitet im Krankenhaus .");

        String[] morphemes = new String[] { 
                "[  0,  3]     -     -  Gen    -    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>die<+ART><Def><Fem><Gen><Sg>)",
                "[  0,  3]     -     -  Gen    -    -     -    -    -  Plur      -  -    -    -    -     -      -     - Der (<CAP>die<+ART><Def><NoGend><Gen><Pl>)",
                "[  0,  3]     -     -  Gen    -    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>die<+DEM><subst><Fem><Gen><Sg>)",
                "[  0,  3]     -     -  Nom    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>der<+ART><Def><Masc><Nom><Sg>)",
                "[  0,  3]     -     -  Nom    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>der<+DEM><subst><Masc><Nom><Sg>)",
                "[  0,  3]     -     -  Nom    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>der<+REL><subst><Masc><Nom><Sg>)",
                "[  0,  3]     -     -  Dat    -    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>die<+DEM><subst><Fem><Dat><Sg>)",
                "[  0,  3]     -     -  Dat    -    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>die<+REL><subst><Fem><Dat><Sg>)",
                "[  0,  3]     -     -  Dat    -    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>die<+ART><Def><Fem><Dat><Sg>)",
                "[  4,  8]     -     -  Nom    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Arzt (Arzt<+NN><Masc><Nom><Sg>)",
                "[  4,  8]     -     -  Acc    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Arzt (Arzt<+NN><Masc><Akk><Sg>)",
                "[  4,  8]     -     -  Dat    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Arzt (Arzt<+NN><Masc><Dat><Sg>)",
                "[  9, 17]     -     -    -    -    -     -  Sub    -  Plur      -  2    -    -    -  Pres      -     - arbeitet (arbeiten<+V><2><Pl><Pres><Konj>)",
                "[  9, 17]     -     -    -    -    -     -  Ind    -  Plur      -  2    -    -    -  Pres      -     - arbeitet (arbeiten<+V><2><Pl><Pres><Ind>)",
                "[  9, 17]     -     -    -    -    -     -  Ind    -  Sing      -  3    -    -    -  Pres      -     - arbeitet (arbeiten<+V><3><Sg><Pres><Ind>)",
                "[  9, 17]     -     -    -    -    -     -    -    -  Plur      -  -    -    -    -   Imp      -     - arbeitet (arbeiten<+V><Imp><Pl>)",
                "[ 18, 20]     -     -  Dat    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - im (im<+PREP/ART><Masc><Dat><Sg>)",
                "[ 18, 20]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - im (im<+PREP/ART><Neut><Dat><Sg>)",
                "[ 21, 32]     -     -  Nom    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Kranke<NN>Haus<+NN><Neut><Nom><Sg>)",
                "[ 21, 32]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (kranken<V><NN><SUFF>Haus<+NN><Neut><Dat><Sg>)",
                "[ 21, 32]     -     -  Acc    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (kranken<V><NN><SUFF>Haus<+NN><Neut><Akk><Sg>)",
                "[ 21, 32]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (krank<ADJ><NN><SUFF>Haus<+NN><Neut><Dat><Sg>)",
                "[ 21, 32]     -     -  Acc    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (krank<ADJ><NN><SUFF>Haus<+NN><Neut><Akk><Sg>)",
                "[ 21, 32]     -     -  Nom    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (krank<ADJ><NN><SUFF>Haus<+NN><Neut><Nom><Sg>)",
                "[ 21, 32]     -     -  Nom    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Krankenhaus<+NN><Neut><Nom><Sg>)",
                "[ 21, 32]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Krankenhaus<+NN><Neut><Dat><Sg>)",
                "[ 21, 32]     -     -  Nom    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (kranken<V><NN><SUFF>Haus<+NN><Neut><Nom><Sg>)",
                "[ 21, 32]     -     -  Acc    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Krankenhaus<+NN><Neut><Akk><Sg>)",
                "[ 21, 32]     -     -  Acc    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Kranke<NN>Haus<+NN><Neut><Akk><Sg>)",
                "[ 21, 32]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Kranke<NN>Haus<+NN><Neut><Dat><Sg>)",
                "[ 33, 34]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - . (.<+IP><Norm>)" };

        assertMorph(morphemes, select(jcas, MorphologicalFeatures.class));
    }

    @Test
    public void testGermanSmor()
        throws Exception
    {
        JCas jcas = runTest("de", "smor-ca", "Der Arzt arbeitet im Krankenhaus .");

        String[] morphemes = new String[] { 
                "[  0,  3]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - Der ()",
                "[  4,  8]     -     -  Nom    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Arzt (Arzt<+NN><Masc><Nom><Sg>)",
                "[  4,  8]     -     -  Acc    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Arzt (Arzt<+NN><Masc><Acc><Sg>)",
                "[  4,  8]     -     -  Dat    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Arzt (Arzt<+NN><Masc><Dat><Sg>)",
                "[  9, 17]     -     -    -    -    -     -    -    -  Plur      -  2    -    -    -  Pres      -     - arbeitet (arbeiten<+V><2><Pl><Pres><Subj>)",
                "[  9, 17]     -     -    -    -    -     -    -    -  Plur      -  -    -    -    -   Imp      -     - arbeitet (arbeiten<+V><Imp><Pl>)",
                "[  9, 17]     -     -    -    -    -     -  Ind    -  Sing      -  3    -    -    -  Pres      -     - arbeitet (arbeiten<+V><3><Sg><Pres><Ind>)",
                "[  9, 17]     -     -    -    -    -     -  Ind    -  Plur      -  2    -    -    -  Pres      -     - arbeitet (arbeiten<+V><2><Pl><Pres><Ind>)",
                "[ 18, 20]     -     -  Dat    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - im (in<+PREPART><Masc><Dat><Sg>)",
                "[ 18, 20]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - im (in<+PREPART><Neut><Dat><Sg>)",
                "[ 21, 32]     -     -  Nom    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (kranken<V><NN><SUFF>Haus<+NN><Neut><Nom><Sg>)",
                "[ 21, 32]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (krank<ADJ><NN><SUFF>Haus<+NN><Neut><Dat><Sg>)",
                "[ 21, 32]     -     -  Nom    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Krankenhaus<+NN><Neut><Nom><Sg>)",
                "[ 21, 32]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Krankenhaus<+NN><Neut><Dat><Sg>)",
                "[ 21, 32]     -     -  Acc    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Krankenhaus<+NN><Neut><Acc><Sg>)",
                "[ 21, 32]     -     -  Acc    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (krank<ADJ><NN><SUFF>Haus<+NN><Neut><Acc><Sg>)",
                "[ 21, 32]     -     -  Acc    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (kranken<V><NN><SUFF>Haus<+NN><Neut><Acc><Sg>)",
                "[ 21, 32]     -     -  Nom    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (krank<ADJ><NN><SUFF>Haus<+NN><Neut><Nom><Sg>)",
                "[ 21, 32]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (kranken<V><NN><SUFF>Haus<+NN><Neut><Dat><Sg>)",
                "[ 33, 34]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - . (.<+PUNCT><Norm>)" };

        assertMorph(morphemes, select(jcas, MorphologicalFeatures.class));
    }

    @Test
    public void testItalian()
        throws Exception
    {
        JCas jcas = runTest("it", "pippi-ca", "Il medico che lavora in ospedale .");

        String[] morphemes = new String[] { 
                "[  0,  2]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - Il ()",
                "[  3,  9]     -     -    -    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - medico (medico<ADJ><pos><m><s>)",
                "[  3,  9]     -     -    -    -    -     -  Ind    -  Sing      -  1    -    -    -  Pres      -     - medico (medicare<VER><ind><pres><1><s>)",
                "[  3,  9]     -     -    -    -    -     -    -    -  Sing      -  -    -    -    -     -      -     - medico (medico<NOUN><M><s>)",
                "[ 10, 13]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - che (che<CON>)",
                "[ 10, 13]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - che (che<WH><CHE>)",
                "[ 10, 13]     -     -    -    -    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - che (che<DET><WH><f><s>)",
                "[ 10, 13]     -     -    -    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - che (che<DET><WH><m><s>)",
                "[ 10, 13]     -     -    -    -    -  Masc    -    -  Plur      -  -    -    -    -     -      -     - che (che<DET><WH><m><p>)",
                "[ 10, 13]     -     -    -    -    -   Fem    -    -  Plur      -  -    -    -    -     -      -     - che (che<DET><WH><f><p>)",
                "[ 14, 20]     -     -    -    -    -     -    -    -  Sing      -  2    -    -    -  Pres      -     - lavora (lavorare<VER><impr><pres><2><s>)",
                "[ 14, 20]     -     -    -    -    -     -  Ind    -  Sing      -  3    -    -    -  Pres      -     - lavora (lavorare<VER><ind><pres><3><s>)",
                "[ 21, 23]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - in (in<PRE>)",
                "[ 24, 32]     -     -    -    -    -     -    -    -  Sing      -  -    -    -    -     -      -     - ospedale (ospedale<NOUN><M><s>)",
                "[ 33, 34]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - . (.<SENT>)" };

        assertMorph(morphemes, select(jcas, MorphologicalFeatures.class));
    }

    private JCas runTest(String language, String variant, String testDocument)
        throws Exception
    {
        AnalysisEngine engine = createEngine(SfstAnnotator.class,
                SfstAnnotator.PARAM_VARIANT, variant,
                SfstAnnotator.PARAM_MODE, SfstAnnotator.Mode.ALL);

        JCas jcas = TestRunner.runTest(engine, language, testDocument);

        return jcas;
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
