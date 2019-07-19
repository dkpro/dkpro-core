/*
 * Copyright 2007-2018
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
package org.dkpro.core.sfst;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.dkpro.core.testing.AssertAnnotations.assertMorph;
import static org.dkpro.core.testing.AssertAnnotations.assertTagset;
import static org.dkpro.core.testing.AssertAnnotations.assertTagsetParser;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.sfst.SfstAnnotator;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.TestRunner;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;

public class SfstAnnotatorTest
{
    @Test
    public void testTurkish()
        throws Exception
    {
        JCas jcas = runTest("tr", "trmorph-ca", "Doktor hastane çalış .");

        String[] morphemes = { 
                "[  0,  6]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - Doktor ()",
                "[  7, 14]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - hastane (hastane<n>)",
                "[  7, 14]     -     -    -    -    -     -    -    -  Sing      -  3    -    -    -     -      -     - hastane (hastane<n><3s>)",
                "[  7, 14]     -     -    -    -    -     -    -    -  Plur      -  3    -    -    -     -      -     - hastane (hastane<n><3p>)",
                "[ 15, 20]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - çalış (çal<v><vn_yis>)",
                "[ 15, 20]     -     -    -    -    -     -    -    -  Plur      -  3    -    -    -     -      -     - çalış (çal<v><vn_yis><3p>)",
                "[ 15, 20]     -     -    -    -    -     -    -    -  Sing      -  3    -    -    -     -      -     - çalış (çal<v><vn_yis><3s>)",
                "[ 15, 20]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - çalış (çal<v><D_yIS><n>)",
                "[ 15, 20]     -     -    -    -    -     -    -    -  Sing      -  3    -    -    -     -      -     - çalış (çal<v><D_yIS><n><3s>)",
                "[ 15, 20]     -     -    -    -    -     -    -    -  Plur      -  3    -    -    -     -      -     - çalış (çal<v><D_yIS><n><3p>)",
                "[ 15, 20]     -     -    -    -    -     -    -    -  Plur      -  3    -    -    -     -      -     - çalış (çalış<v><t_imp><3p>)",
                "[ 15, 20]     -     -    -    -    -     -    -    -  Sing      -  2    -    -    -     -      -     - çalış (çalış<v><t_imp><2s>)",
                "[ 21, 22]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - . (.<pnct>)"
        };
        
        String[] tags = { "<1p>", "<1s>", "<2p>", "<2s>", "<3p>", "<3s>", "<D_AcIK>", "<D_CA>",
                "<D_CAK>", "<D_CAgIz>", "<D_CI>", "<D_CIK>", "<D_IcIK>", "<D_IncI>", "<D_ca>",
                "<D_cil>", "<D_gil>", "<D_lA>", "<D_lAn>", "<D_lAs>", "<D_lI>", "<D_lIK>",
                "<D_mA>", "<D_mAdIK>", "<D_sAl>", "<D_sAr>", "<D_sa>", "<D_siz>", "<D_yIS>",
                "<Dan_0>", "<Djn_0>", "<Dmn_0>", "<Dnn_siz>", "<Dvn_yIcI>", "<abil>", "<abl>",
                "<acc>", "<acr>", "<adj>", "<adur>", "<adv>", "<agel>", "<agor>", "<akal>",
                "<akoy>", "<ant>", "<apos>", "<ayaz>", "<ca>", "<caus>", "<cnjadv>", "<cnjcoo>",
                "<cnjsub>", "<cog>", "<cpl_di>", "<cpl_ken>", "<cpl_mis>", "<cpl_sa>", "<cv_acak>",
                "<cv_cesine>", "<cv_dan>", "<cv_dik>", "<cv_ecek>", "<cv_eli>", "<cv_erek>",
                "<cv_ince>", "<cv_ip>", "<cv_iyor>", "<cv_ma>", "<cv_mak>", "<cv_mis>",
                "<cv_zdan>", "<dat>", "<dem>", "<det>", "<dir>", "<exist>", "<gen>", "<ij>",
                "<ins>", "<iver>", "<ki>", "<loc>", "<locp>", "<n>", "<neg>", "<nexist>", "<not>",
                "<np>", "<num>", "<org>", "<p1p>", "<p1s>", "<p2p>", "<p2s>", "<p3p>", "<p3s>",
                "<part_acak>", "<part_dik>", "<part_yan>", "<pass>", "<pers>", "<pl>", "<pnct>",
                "<postp>", "<prn>", "<q>", "<qst>", "<rec>", "<ref>", "<t_aor>", "<t_cond>",
                "<t_cont>", "<t_fut>", "<t_imp>", "<t_makta>", "<t_narr>", "<t_obl>", "<t_opt>",
                "<t_past>", "<top>", "<v>", "<vinf>", "<vn_acak>", "<vn_dik>", "<vn_ma>",
                "<vn_mak>", "<vn_yis>" };

        String[] unmappedTags = { "<D_AcIK>", "<D_CA>", "<D_CAK>", "<D_CAgIz>", "<D_CI>",
                "<D_CIK>", "<D_IcIK>", "<D_IncI>", "<D_ca>", "<D_cil>", "<D_gil>", "<D_lA>",
                "<D_lAn>", "<D_lAs>", "<D_lI>", "<D_lIK>", "<D_mA>", "<D_mAdIK>", "<D_sAl>",
                "<D_sAr>", "<D_sa>", "<D_siz>", "<D_yIS>", "<Dan_0>", "<Djn_0>", "<Dmn_0>",
                "<Dnn_siz>", "<Dvn_yIcI>", "<abil>", "<acr>", "<adj>", "<adur>", "<adv>", "<agel>",
                "<agor>", "<akal>", "<akoy>", "<ant>", "<apos>", "<ayaz>", "<ca>", "<caus>",
                "<cnjadv>", "<cnjcoo>", "<cnjsub>", "<cog>", "<cpl_di>", "<cpl_ken>", "<cpl_mis>",
                "<cpl_sa>", "<cv_acak>", "<cv_cesine>", "<cv_dan>", "<cv_dik>", "<cv_ecek>",
                "<cv_eli>", "<cv_erek>", "<cv_ince>", "<cv_ip>", "<cv_iyor>", "<cv_ma>",
                "<cv_mak>", "<cv_mis>", "<cv_zdan>", "<dem>", "<det>", "<dir>", "<exist>", "<ij>",
                "<iver>", "<ki>", "<locp>", "<n>", "<neg>", "<nexist>", "<not>", "<np>", "<num>",
                "<org>", "<p1p>", "<p1s>", "<p2p>", "<p2s>", "<p3p>", "<p3s>", "<part_acak>",
                "<part_dik>", "<part_yan>", "<pass>", "<pers>", "<pl>", "<pnct>", "<postp>",
                "<prn>", "<q>", "<qst>", "<rec>", "<ref>", "<t_aor>", "<t_cond>", "<t_cont>",
                "<t_fut>", "<t_imp>", "<t_makta>", "<t_narr>", "<t_obl>", "<t_opt>", "<t_past>",
                "<top>", "<v>", "<vinf>", "<vn_acak>", "<vn_dik>", "<vn_ma>", "<vn_mak>",
                "<vn_yis>" };

        assertMorph(morphemes, select(jcas, MorphologicalFeatures.class));
        assertTagset(MorphologicalFeatures.class, "trmorph", tags, jcas);
        assertTagsetParser(MorphologicalFeatures.class, "trmorph", unmappedTags, jcas);
    }

    @Test
    public void testGermanMorphisto()
        throws Exception
    {
        JCas jcas = runTest("de", "morphisto-ca", "Der Arzt arbeitet im Krankenhaus .");

        String[] morphemes = { 
                "[  0,  3]     -     -  Gen    -    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>die<+ART><Def><Fem><Gen><Sg>)",
                "[  0,  3]     -     -  Dat    -    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>die<+ART><Def><Fem><Dat><Sg>)",
                "[  0,  3]     -     -  Gen    -    -     -    -    -  Plur      -  -    -    -    -     -      -     - Der (<CAP>die<+ART><Def><NoGend><Gen><Pl>)",
                "[  0,  3]     -     -  Dat    -    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>die<+REL><subst><Fem><Dat><Sg>)",
                "[  0,  3]     -     -  Gen    -    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>die<+DEM><subst><Fem><Gen><Sg>)",
                "[  0,  3]     -     -  Dat    -    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>die<+DEM><subst><Fem><Dat><Sg>)",
                "[  0,  3]     -     -  Nom    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>der<+ART><Def><Masc><Nom><Sg>)",
                "[  0,  3]     -     -  Nom    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>der<+REL><subst><Masc><Nom><Sg>)",
                "[  0,  3]     -     -  Nom    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>der<+DEM><subst><Masc><Nom><Sg>)",
                "[  4,  8]     -     -  Nom    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Arzt (Arzt<+NN><Masc><Nom><Sg>)",
                "[  4,  8]     -     -  Dat    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Arzt (Arzt<+NN><Masc><Dat><Sg>)",
                "[  4,  8]     -     -  Acc    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Arzt (Arzt<+NN><Masc><Akk><Sg>)",
                "[  9, 17]     -     -    -    -    -     -  Sub    -  Plur      -  2    -    -    -  Pres      -     - arbeitet (arbeiten<+V><2><Pl><Pres><Konj>)",
                "[  9, 17]     -     -    -    -    -     -    -    -  Plur      -  -    -    -    -   Imp      -     - arbeitet (arbeiten<+V><Imp><Pl>)",
                "[  9, 17]     -     -    -    -    -     -  Ind    -  Plur      -  2    -    -    -  Pres      -     - arbeitet (arbeiten<+V><2><Pl><Pres><Ind>)",
                "[  9, 17]     -     -    -    -    -     -  Ind    -  Sing      -  3    -    -    -  Pres      -     - arbeitet (arbeiten<+V><3><Sg><Pres><Ind>)",
                "[ 18, 20]     -     -  Dat    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - im (im<+PREP/ART><Masc><Dat><Sg>)",
                "[ 18, 20]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - im (im<+PREP/ART><Neut><Dat><Sg>)",
                "[ 21, 32]     -     -  Nom    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Kranke<NN>Haus<+NN><Neut><Nom><Sg>)",
                "[ 21, 32]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Kranke<NN>Haus<+NN><Neut><Dat><Sg>)",
                "[ 21, 32]     -     -  Acc    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Kranke<NN>Haus<+NN><Neut><Akk><Sg>)",
                "[ 21, 32]     -     -  Nom    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Krankenhaus<+NN><Neut><Nom><Sg>)",
                "[ 21, 32]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Krankenhaus<+NN><Neut><Dat><Sg>)",
                "[ 21, 32]     -     -  Acc    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Krankenhaus<+NN><Neut><Akk><Sg>)",
                "[ 21, 32]     -     -  Nom    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (kranken<V><NN><SUFF>Haus<+NN><Neut><Nom><Sg>)",
                "[ 21, 32]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (kranken<V><NN><SUFF>Haus<+NN><Neut><Dat><Sg>)",
                "[ 21, 32]     -     -  Acc    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (kranken<V><NN><SUFF>Haus<+NN><Neut><Akk><Sg>)",
                "[ 21, 32]     -     -  Nom    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (krank<ADJ><NN><SUFF>Haus<+NN><Neut><Nom><Sg>)",
                "[ 21, 32]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (krank<ADJ><NN><SUFF>Haus<+NN><Neut><Dat><Sg>)",
                "[ 21, 32]     -     -  Acc    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (krank<ADJ><NN><SUFF>Haus<+NN><Neut><Akk><Sg>)",
                "[ 33, 34]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - . (.<+IP><Norm>)"
        };

        String[] tags = { "<+ADJ>", "<+ADV>", "<+ART>", "<+CARD>", "<+CHAR>", "<+CIRCP>", "<+DEM>",
                "<+DEMPRO>", "<+INDEF>", "<+INTJ>", "<+IP>", "<+KONJ>", "<+NE>", "<+NN>", "<+ORD>",
                "<+POSS>", "<+POSTP>", "<+PPRO>", "<+PREP/ART>", "<+PREP>", "<+PROADV>", "<+PTKL>",
                "<+REL>", "<+SYMBOL>", "<+TRUNC>", "<+V>", "<+VPRE>", "<+WADV>", "<+WPRO>", "<1>",
                "<2>", "<3>", "<ADJ>", "<ADV>", "<Adj>", "<Adv>", "<Akk>", "<Ant>", "<CAP>",
                "<CARD>", "<Comp>", "<DIGCARD>", "<Dat>", "<Def>", "<Fem>", "<Gen>", "<Imp>",
                "<Ind>", "<Indef>", "<Inf>", "<Invar>", "<Komma>", "<Kon>", "<Konj>", "<Masc>",
                "<NE>", "<NN>", "<Neg>", "<Neut>", "<NoGend>", "<Nom>", "<Norm>", "<ORD>",
                "<OTHER>", "<PPast>", "<PPres>", "<PREF>", "<Past>", "<Pl>", "<Pos>", "<Pred>",
                "<Pres>", "<ProAdv>", "<QUANT>", "<SUFF>", "<Sg>", "<St/Mix>", "<St>", "<Sub>",
                "<Sup>", "<Sw/Mix>", "<Sw>", "<UC>", "<V>", "<Vgl>", "<^ABK>", "<^VPAST>",
                "<^VPRES>", "<attr>", "<links>", "<mD>", "<oD>", "<pers>", "<prfl>", "<pro>",
                "<rechts>", "<refl>", "<rez>", "<subst>", "<zu>" };

        String[] unmappedTags = { "<+ADJ>", "<+ADV>", "<+ART>", "<+CARD>", "<+CHAR>", "<+CIRCP>",
                "<+DEM>", "<+DEMPRO>", "<+INDEF>", "<+INTJ>", "<+IP>", "<+KONJ>", "<+NE>", "<+NN>",
                "<+ORD>", "<+POSS>", "<+POSTP>", "<+PPRO>", "<+PREP/ART>", "<+PREP>", "<+PROADV>",
                "<+PTKL>", "<+REL>", "<+SYMBOL>", "<+TRUNC>", "<+V>", "<+VPRE>", "<+WADV>",
                "<+WPRO>", "<ADJ>", "<ADV>", "<Adj>", "<Adv>", "<Ant>", "<CAP>", "<CARD>",
                "<Comp>", "<DIGCARD>", "<Def>", "<Indef>", "<Inf>", "<Invar>", "<Komma>", "<Kon>",
                "<NE>", "<NN>", "<Neg>", "<NoGend>", "<Norm>", "<ORD>", "<OTHER>", "<PPast>",
                "<PPres>", "<PREF>", "<Past>", "<Pos>", "<Pred>", "<ProAdv>", "<QUANT>", "<SUFF>",
                "<St/Mix>", "<St>", "<Sub>", "<Sup>", "<Sw/Mix>", "<Sw>", "<UC>", "<V>", "<Vgl>",
                "<^ABK>", "<^VPAST>", "<^VPRES>", "<attr>", "<links>", "<mD>", "<oD>", "<pers>",
                "<prfl>", "<pro>", "<rechts>", "<refl>", "<rez>", "<subst>", "<zu>" };

        assertMorph(morphemes, select(jcas, MorphologicalFeatures.class));
        assertTagset(MorphologicalFeatures.class, "morphisto", tags, jcas);
        assertTagsetParser(MorphologicalFeatures.class, "morphisto", unmappedTags, jcas);
    }

    @Test
    public void testGermanSmor()
        throws Exception
    {
        JCas jcas = runTest("de", "smor-ca", "Der Arzt arbeitet im Krankenhaus .");

        String[] morphemes = {
                "[  0,  3]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - Der ()",
                "[  4,  8]     -     -  Nom    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Arzt (Arzt<+NN><Masc><Nom><Sg>)",
                "[  4,  8]     -     -  Dat    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Arzt (Arzt<+NN><Masc><Dat><Sg>)",
                "[  4,  8]     -     -  Acc    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Arzt (Arzt<+NN><Masc><Acc><Sg>)",
                "[  9, 17]     -     -    -    -    -     -  Sub    -  Plur      -  2    -    -    -  Pres      -     - arbeitet (arbeiten<+V><2><Pl><Pres><Subj>)",
                "[  9, 17]     -     -    -    -    -     -  Ind    -  Plur      -  2    -    -    -  Pres      -     - arbeitet (arbeiten<+V><2><Pl><Pres><Ind>)",
                "[  9, 17]     -     -    -    -    -     -  Imp    -  Plur      -  -    -    -    -     -      -     - arbeitet (arbeiten<+V><Imp><Pl>)",
                "[  9, 17]     -     -    -    -    -     -  Ind    -  Sing      -  3    -    -    -  Pres      -     - arbeitet (arbeiten<+V><3><Sg><Pres><Ind>)",
                "[ 18, 20]     -     -  Dat    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - im (in<+PREPART><Masc><Dat><Sg>)",
                "[ 18, 20]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - im (in<+PREPART><Neut><Dat><Sg>)",
                "[ 21, 32]     -     -  Nom    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (kranken<V><NN><SUFF>Haus<+NN><Neut><Nom><Sg>)",
                "[ 21, 32]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (kranken<V><NN><SUFF>Haus<+NN><Neut><Dat><Sg>)",
                "[ 21, 32]     -     -  Acc    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (kranken<V><NN><SUFF>Haus<+NN><Neut><Acc><Sg>)",
                "[ 21, 32]     -     -  Nom    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (krank<ADJ><NN><SUFF>Haus<+NN><Neut><Nom><Sg>)",
                "[ 21, 32]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (krank<ADJ><NN><SUFF>Haus<+NN><Neut><Dat><Sg>)",
                "[ 21, 32]     -     -  Acc    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (krank<ADJ><NN><SUFF>Haus<+NN><Neut><Acc><Sg>)",
                "[ 21, 32]     -     -  Nom    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Krankenhaus<+NN><Neut><Nom><Sg>)",
                "[ 21, 32]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Krankenhaus<+NN><Neut><Dat><Sg>)",
                "[ 21, 32]     -     -  Acc    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Krankenhaus<+NN><Neut><Acc><Sg>)",
                "[ 33, 34]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - . (.<+PUNCT><Norm>)"
        };

        String[] tags = { "<+ADJ>", "<+ADV>", "<+ART>", "<+CARD>", "<+CIRCP>", "<+CONJ>", "<+DEM>",
                "<+INDEF>", "<+INTJ>", "<+NN>", "<+NPROP>", "<+ORD>", "<+POSS>", "<+POSTP>",
                "<+PPRO>", "<+PREP>", "<+PREPART>", "<+PROADV>", "<+PTCL>", "<+PUNCT>", "<+REL>",
                "<+SYMBOL>", "<+TRUNC>", "<+V>", "<+VPART>", "<+WADV>", "<+WPRO>", "<1>", "<2>",
                "<3>", "<ADJ>", "<ADV>", "<Acc>", "<Adj>", "<Adv>", "<Ans>", "<Attr>", "<CAP>",
                "<CARD>", "<Comma>", "<Comp>", "<Compar>", "<Coord>", "<Dat>", "<Def>", "<F>",
                "<Fem>", "<Ge-Nom>", "<Gen>", "<Imp>", "<Ind>", "<Indef>", "<Inf>", "<Invar>",
                "<KSF>", "<Left>", "<Masc>", "<NEWORTH>", "<NN>", "<NPROP>", "<Neg>", "<Neut>",
                "<NoGend>", "<Nom>", "<Norm>", "<OLDORTH>", "<ORD>", "<Old>", "<PPast>", "<PPres>",
                "<PREF>", "<Past>", "<Pers>", "<Pl>", "<Pos>", "<Pred>", "<Pres>", "<Pro>",
                "<Rec>", "<Refl>", "<Right>", "<SUFF>", "<Sg>", "<Simp>", "<St>", "<Sub>",
                "<Subj>", "<Subst>", "<Sup>", "<TRUNC>", "<V>", "<VPART>", "<VPREF>", "<Wk>",
                "<^ABBR>", "<zu>" };

        String[] unmappedTags = {};

        assertMorph(morphemes, select(jcas, MorphologicalFeatures.class));
        assertTagset(MorphologicalFeatures.class, "smor", tags, jcas);
        assertTagsetParser(MorphologicalFeatures.class, "smor", unmappedTags, jcas);
    }

    @Test
    public void testGermanZmorgeOrig()
        throws Exception
    {
        JCas jcas = runTest("de", "zmorge-orig-ca", "Der Arzt arbeitet im Krankenhaus .");

        String[] morphemes = {
                "[  0,  3]     -     -  Dat    -    -   Fem    -    -  Sing      -  -    -  Rel    -     -      -     - Der (<CAP>die<+REL><Subst><Fem><Dat><Sg><St>)",
                "[  0,  3]     -     -  Nom    -    -  Masc    -    -  Sing      -  -    -  Rel    -     -      -     - Der (<CAP>die<+REL><Subst><Masc><Nom><Sg><St>)",
                "[  0,  3]     -     -  Dat    -    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>die<+DEM><Subst><Fem><Dat><Sg><St>)",
                "[  0,  3]     -     -  Nom    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>die<+DEM><Subst><Masc><Nom><Sg><St>)",
                "[  0,  3]     -     -  Gen  Def    -     -    -    -  Plur      -  -    -    -    -     -      -     - Der (<CAP>die<+ART><Def><NoGend><Gen><Pl><St>)",
                "[  0,  3]     -     -  Dat  Def    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>die<+ART><Def><Fem><Dat><Sg><St>)",
                "[  0,  3]     -     -  Gen  Def    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>die<+ART><Def><Fem><Gen><Sg><St>)",
                "[  0,  3]     -     -  Nom  Def    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>die<+ART><Def><Masc><Nom><Sg><St>)",
                "[  4,  8]     -     -  Acc    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Arzt (Arzt<+NN><Masc><Acc><Sg>)",
                "[  4,  8]     -     -  Dat    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Arzt (Arzt<+NN><Masc><Dat><Sg>)",
                "[  4,  8]     -     -  Nom    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Arzt (Arzt<+NN><Masc><Nom><Sg>)",
                "[  9, 17]     -     -    -    -    -     -  Sub    -  Plur      -  2    -    -    -  Pres      -     - arbeitet (arbeiten<+V><2><Pl><Pres><Subj>)",
                "[  9, 17]     -     -    -    -    -     -  Imp    -  Plur      -  -    -    -    -     -      -     - arbeitet (arbeiten<+V><Imp><Pl>)",
                "[  9, 17]     -     -    -    -    -     -  Ind    -  Sing      -  3    -    -    -  Pres      -     - arbeitet (arbeiten<+V><3><Sg><Pres><Ind>)",
                "[  9, 17]     -     -    -    -    -     -  Ind    -  Plur      -  2    -    -    -  Pres      -     - arbeitet (arbeiten<+V><2><Pl><Pres><Ind>)",
                "[ 18, 20]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - im (in<+PREPART><Neut><Dat><Sg>)",
                "[ 18, 20]     -     -  Dat    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - im (in<+PREPART><Masc><Dat><Sg>)",
                "[ 21, 32]     -     -  Acc    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (krank<ADJ><NN><SUFF>Haus<+NN><Neut><Acc><Sg>)",
                "[ 21, 32]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (krank<ADJ><NN><SUFF>Haus<+NN><Neut><Dat><Sg>)",
                "[ 21, 32]     -     -  Nom    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (krank<ADJ><NN><SUFF>Haus<+NN><Neut><Nom><Sg>)",
                "[ 21, 32]     -     -  Acc    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (kranken<V><NN><SUFF>Haus<+NN><Neut><Acc><Sg>)",
                "[ 21, 32]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (kranken<V><NN><SUFF>Haus<+NN><Neut><Dat><Sg>)",
                "[ 21, 32]     -     -  Nom    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (kranken<V><NN><SUFF>Haus<+NN><Neut><Nom><Sg>)",
                "[ 21, 32]     -     -  Acc    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Kran<NN>Ken<NN>Haus<+NN><Neut><Acc><Sg>)",
                "[ 21, 32]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Kran<NN>Ken<NN>Haus<+NN><Neut><Dat><Sg>)",
                "[ 21, 32]     -     -  Nom    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Kran<NN>Ken<NN>Haus<+NN><Neut><Nom><Sg>)",
                "[ 21, 32]     -     -  Acc    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Krankenhaus<+NN><Neut><Acc><Sg>)",
                "[ 21, 32]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Krankenhaus<+NN><Neut><Dat><Sg>)",
                "[ 21, 32]     -     -  Nom    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Krankenhaus<+NN><Neut><Nom><Sg>)",
                "[ 33, 34]     -     -  Acc    -    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - . (.<^ABBR><+NN><Fem><Acc><Sg>)",
                "[ 33, 34]     -     -  Dat    -    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - . (.<^ABBR><+NN><Fem><Dat><Sg>)",
                "[ 33, 34]     -     -  Gen    -    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - . (.<^ABBR><+NN><Fem><Gen><Sg>)",
                "[ 33, 34]     -     -  Nom    -    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - . (.<^ABBR><+NN><Fem><Nom><Sg>)",
                "[ 33, 34]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - . (.<+PUNCT><Norm>)"
        };

        String[] tags = { "<+ADJ>", "<+ADV>", "<+ART>", "<+CARD>", "<+CONJ>", "<+DEM>", "<+INDEF>",
                "<+INTJ>", "<+NN>", "<+NPROP>", "<+ORD>", "<+POSS>", "<+POSTP>", "<+PPRO>",
                "<+PREP>", "<+PREPART>", "<+PROADV>", "<+PTCL>", "<+PUNCT>", "<+REL>", "<+SYMBOL>",
                "<+TRUNC>", "<+V>", "<+VPART>", "<+WADV>", "<+WPRO>", "<1>", "<2>", "<3>", "<ADJ>",
                "<ADV>", "<Acc>", "<Adv>", "<Ans>", "<Attr>", "<CAP>", "<CARD>", "<Comma>",
                "<Comp>", "<Compar>", "<Coord>", "<Dat>", "<Def>", "<F>", "<Fem>", "<GUESSER>",
                "<Ge-Nom>", "<Gen>", "<Imp>", "<Ind>", "<Indef>", "<Inf>", "<Invar>", "<Left>",
                "<Masc>", "<NEWORTH>", "<NN>", "<NPROP>", "<Neg>", "<Neut>", "<NoGend>", "<Nom>",
                "<Norm>", "<OLDORTH>", "<ORD>", "<Old>", "<PPast>", "<PPres>", "<PREF>", "<Past>",
                "<Pers>", "<Pl>", "<Pos>", "<Pred>", "<Pres>", "<Pro>", "<Rec>", "<Refl>",
                "<Right>", "<SUFF>", "<Sg>", "<Simp>", "<St>", "<Sub>", "<Subj>", "<Subst>",
                "<Sup>", "<TRUNC>", "<V>", "<VPART>", "<VPREF>", "<Wk>", "<^ABBR>", "<zu>" };

        String[] unmappedTags = {};

        assertMorph(morphemes, select(jcas, MorphologicalFeatures.class));
        assertTagset(MorphologicalFeatures.class, "smor", tags, jcas);
        assertTagsetParser(MorphologicalFeatures.class, "smor", unmappedTags, jcas);
    }

    @Test
    public void testGermanZmorgeNewlemma()
        throws Exception
    {
        JCas jcas = runTest("de", "zmorge-newlemma-ca", "Der Arzt arbeitet im Krankenhaus .");

        String[] morphemes = {
                "[  0,  3]     -     -  Dat    -    -   Fem    -    -  Sing      -  -    -  Rel    -     -      -     - Der (<CAP>die<+REL><Subst><Fem><Dat><Sg><St>)",
                "[  0,  3]     -     -  Nom    -    -  Masc    -    -  Sing      -  -    -  Rel    -     -      -     - Der (<CAP>die<+REL><Subst><Masc><Nom><Sg><St>)",
                "[  0,  3]     -     -  Dat    -    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>die<+DEM><Subst><Fem><Dat><Sg><St>)",
                "[  0,  3]     -     -  Nom    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>die<+DEM><Subst><Masc><Nom><Sg><St>)",
                "[  0,  3]     -     -  Gen  Def    -     -    -    -  Plur      -  -    -    -    -     -      -     - Der (<CAP>die<+ART><Def><NoGend><Gen><Pl><St>)",
                "[  0,  3]     -     -  Dat  Def    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>die<+ART><Def><Fem><Dat><Sg><St>)",
                "[  0,  3]     -     -  Gen  Def    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>die<+ART><Def><Fem><Gen><Sg><St>)",
                "[  0,  3]     -     -  Nom  Def    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Der (<CAP>die<+ART><Def><Masc><Nom><Sg><St>)",
                "[  4,  8]     -     -  Acc    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Arzt (Arzt<+NN><Masc><Acc><Sg>)",
                "[  4,  8]     -     -  Dat    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Arzt (Arzt<+NN><Masc><Dat><Sg>)",
                "[  4,  8]     -     -  Nom    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - Arzt (Arzt<+NN><Masc><Nom><Sg>)",
                "[  9, 17]     -     -    -    -    -     -  Sub    -  Plur      -  2    -    -    -  Pres      -     - arbeitet (arbeit<~>en<+V><2><Pl><Pres><Subj>)",
                "[  9, 17]     -     -    -    -    -     -  Imp    -  Plur      -  -    -    -    -     -      -     - arbeitet (arbeit<~>en<+V><Imp><Pl>)",
                "[  9, 17]     -     -    -    -    -     -  Ind    -  Sing      -  3    -    -    -  Pres      -     - arbeitet (arbeit<~>en<+V><3><Sg><Pres><Ind>)",
                "[  9, 17]     -     -    -    -    -     -  Ind    -  Plur      -  2    -    -    -  Pres      -     - arbeitet (arbeit<~>en<+V><2><Pl><Pres><Ind>)",
                "[ 18, 20]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - im (in<+PREPART><Neut><Dat><Sg>)",
                "[ 18, 20]     -     -  Dat    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - im (in<+PREPART><Masc><Dat><Sg>)",
                "[ 21, 32]     -     -  Acc    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Kran<#>ken<#>haus<+NN><Neut><Acc><Sg>)",
                "[ 21, 32]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Kran<#>ken<#>haus<+NN><Neut><Dat><Sg>)",
                "[ 21, 32]     -     -  Nom    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Kran<#>ken<#>haus<+NN><Neut><Nom><Sg>)",
                "[ 21, 32]     -     -  Acc    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Krank<~>en<#>haus<+NN><Neut><Acc><Sg>)",
                "[ 21, 32]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Krank<~>en<#>haus<+NN><Neut><Dat><Sg>)",
                "[ 21, 32]     -     -  Nom    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Krank<~>en<#>haus<+NN><Neut><Nom><Sg>)",
                "[ 21, 32]     -     -  Acc    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Krank<~>en<#>haus<+NN><Neut><Acc><Sg>)",
                "[ 21, 32]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Krank<~>en<#>haus<+NN><Neut><Dat><Sg>)",
                "[ 21, 32]     -     -  Nom    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Krank<~>en<#>haus<+NN><Neut><Nom><Sg>)",
                "[ 21, 32]     -     -  Acc    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Krankenhaus<+NN><Neut><Acc><Sg>)",
                "[ 21, 32]     -     -  Dat    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Krankenhaus<+NN><Neut><Dat><Sg>)",
                "[ 21, 32]     -     -  Nom    -    -  Neut    -    -  Sing      -  -    -    -    -     -      -     - Krankenhaus (Krankenhaus<+NN><Neut><Nom><Sg>)",
                "[ 33, 34]     -     -  Acc    -    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - . (.<+NN><Fem><Acc><Sg>)",
                "[ 33, 34]     -     -  Dat    -    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - . (.<+NN><Fem><Dat><Sg>)",
                "[ 33, 34]     -     -  Gen    -    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - . (.<+NN><Fem><Gen><Sg>)",
                "[ 33, 34]     -     -  Nom    -    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - . (.<+NN><Fem><Nom><Sg>)",
                "[ 33, 34]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - . (.<+PUNCT><Norm>)"
        };

        String[] tags = { "<#>", "<+ADJ>", "<+ADV>", "<+ART>", "<+CARD>", "<+CONJ>", "<+DEM>",
                "<+INDEF>", "<+INTJ>", "<+NN>", "<+NPROP>", "<+ORD>", "<+POSS>", "<+POSTP>",
                "<+PPRO>", "<+PREP>", "<+PREPART>", "<+PROADV>", "<+PTCL>", "<+PUNCT>", "<+REL>",
                "<+SYMBOL>", "<+TRUNC>", "<+V>", "<+VPART>", "<+WADV>", "<+WPRO>", "<->", "<1>",
                "<2>", "<3>", "<Acc>", "<Adv>", "<Ans>", "<Attr>", "<CAP>", "<Comma>", "<Comp>",
                "<Compar>", "<Coord>", "<Dat>", "<Def>", "<Fem>", "<GUESSER>", "<Gen>", "<Imp>",
                "<Ind>", "<Indef>", "<Inf>", "<Invar>", "<Left>", "<Masc>", "<NEWORTH>", "<Neg>",
                "<Neut>", "<NoGend>", "<Nom>", "<Norm>", "<OLDORTH>", "<Old>", "<PPast>",
                "<PPres>", "<Past>", "<Pers>", "<Pl>", "<Pos>", "<Pred>", "<Pres>", "<Pro>",
                "<Rec>", "<Refl>", "<Right>", "<SUFF>", "<Sg>", "<Simp>", "<St>", "<Sub>",
                "<Subj>", "<Subst>", "<Sup>", "<TRUNC>", "<V>", "<Wk>", "<^ABBR>", "<zu>", "<~>" };

        String[] unmappedTags = { "<#>", "<->", "<~>" };

        assertMorph(morphemes, select(jcas, MorphologicalFeatures.class));
        assertTagset(MorphologicalFeatures.class, "smor", tags, jcas);
        assertTagsetParser(MorphologicalFeatures.class, "smor", unmappedTags, jcas);
    }

    @Test
    public void testItalian()
        throws Exception
    {
        JCas jcas = runTest("it", "pippi-ca", "Il medico che lavora in ospedale .");

        String[] morphemes = { 
                "[  0,  2]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - Il ()",
                "[  3,  9]     -     -    -    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - medico (medico<ADJ><pos><m><s>)",
                "[  3,  9]     -     -    -    -    -     -    -    -  Sing      -  -    -    -    -     -      -     - medico (medico<NOUN><M><s>)",
                "[  3,  9]     -     -    -    -    -     -  Ind    -  Sing      -  1    -    -    -  Pres      -     - medico (medicare<VER><ind><pres><1><s>)",
                "[ 10, 13]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - che (che<CON>)",
                "[ 10, 13]     -     -    -    -    -   Fem    -    -  Plur      -  -    -    -    -     -      -     - che (che<DET><WH><f><p>)",
                "[ 10, 13]     -     -    -    -    -   Fem    -    -  Sing      -  -    -    -    -     -      -     - che (che<DET><WH><f><s>)",
                "[ 10, 13]     -     -    -    -    -  Masc    -    -  Plur      -  -    -    -    -     -      -     - che (che<DET><WH><m><p>)",
                "[ 10, 13]     -     -    -    -    -  Masc    -    -  Sing      -  -    -    -    -     -      -     - che (che<DET><WH><m><s>)",
                "[ 10, 13]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - che (che<WH><CHE>)",
                "[ 14, 20]     -     -    -    -    -     -    -    -  Sing      -  2    -    -    -  Pres      -     - lavora (lavorare<VER><impr><pres><2><s>)",
                "[ 14, 20]     -     -    -    -    -     -  Ind    -  Sing      -  3    -    -    -  Pres      -     - lavora (lavorare<VER><ind><pres><3><s>)",
                "[ 21, 23]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - in (in<PRE>)",
                "[ 24, 32]     -     -    -    -    -     -    -    -  Sing      -  -    -    -    -     -      -     - ospedale (ospedale<NOUN><M><s>)",
                "[ 33, 34]     -     -    -    -    -     -    -    -     -      -  -    -    -    -     -      -     - . (.<SENT>)"
        };

        String[] tags = { "<1>", "<2>", "<3>", "<ABL>", "<ADJ>", "<ADV>", "<ART>", "<ARTPRE>",
                "<ASP>", "<AUX>", "<CARD>", "<CAU>", "<CE>", "<CHE>", "<CI>", "<CLI>", "<COM>",
                "<CON>", "<DEMO>", "<DET>", "<F>", "<INDEF>", "<INT>", "<M>", "<MOD>", "<NE>",
                "<NOUN>", "<NUM>", "<P>", "<PERS>", "<PON>", "<POSS>", "<PRE>", "<PRO>", "<S>",
                "<SENT>", "<SI>", "<TALE>", "<VER>", "<WH>", "<cela>", "<cele>", "<celi>",
                "<celo>", "<cene>", "<ci>", "<comp>", "<cond>", "<f>", "<fut>", "<ger>", "<gli>",
                "<gliela>", "<gliele>", "<glieli>", "<glielo>", "<gliene>", "<impf>", "<impr>",
                "<ind>", "<inf>", "<la>", "<le>", "<li>", "<lo>", "<m>", "<mela>", "<mele>",
                "<meli>", "<melo>", "<mene>", "<mi>", "<ne>", "<p>", "<part>", "<past>", "<pos>",
                "<pres>", "<s>", "<sela>", "<sele>", "<seli>", "<selo>", "<sene>", "<si>", "<sub>",
                "<sup>", "<tela>", "<tele>", "<teli>", "<telo>", "<tene>", "<ti>", "<vela>",
                "<vele>", "<veli>", "<velo>", "<vene>", "<vi>" };

        String[] unmappedTags = { "<ABL>", "<ADJ>", "<ADV>", "<ART>", "<ARTPRE>", "<ASP>", "<AUX>",
                "<CARD>", "<CAU>", "<CE>", "<CHE>", "<CI>", "<CLI>", "<COM>", "<CON>", "<DEMO>",
                "<DET>", "<F>", "<INT>", "<M>", "<MOD>", "<NE>", "<NOUN>", "<NUM>", "<P>",
                "<PERS>", "<PON>", "<POSS>", "<PRE>", "<PRO>", "<S>", "<SENT>", "<SI>", "<TALE>",
                "<VER>", "<WH>", "<cela>", "<cele>", "<celi>", "<celo>", "<cene>", "<ci>",
                "<comp>", "<cond>", "<gli>", "<gliela>", "<gliele>", "<glieli>", "<glielo>",
                "<gliene>", "<impr>", "<la>", "<le>", "<li>", "<lo>", "<mela>", "<mele>", "<meli>",
                "<melo>", "<mene>", "<mi>", "<ne>", "<part>", "<pos>", "<sela>", "<sele>",
                "<seli>", "<selo>", "<sene>", "<si>", "<sub>", "<sup>", "<tela>", "<tele>",
                "<teli>", "<telo>", "<tene>", "<ti>", "<vela>", "<vele>", "<veli>", "<velo>",
                "<vene>", "<vi>" };

        assertMorph(morphemes, select(jcas, MorphologicalFeatures.class));
        assertTagset(MorphologicalFeatures.class, "pippi", tags, jcas);
        assertTagsetParser(MorphologicalFeatures.class, "pippi", unmappedTags, jcas);
    }

    private JCas runTest(String language, String variant, String testDocument)
        throws Exception
    {
        AnalysisEngine engine = createEngine(SfstAnnotator.class,
                SfstAnnotator.PARAM_VARIANT, variant,
                SfstAnnotator.PARAM_MODE, SfstAnnotator.Mode.ALL,
                SfstAnnotator.PARAM_PRINT_TAGSET, true);

        JCas jcas = TestRunner.runTest(engine, language, testDocument);

        return jcas;
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
