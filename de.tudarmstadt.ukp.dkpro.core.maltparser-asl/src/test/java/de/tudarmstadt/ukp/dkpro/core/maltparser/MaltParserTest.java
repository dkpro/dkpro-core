/*******************************************************************************
 * Copyright 2012
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.maltparser;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TagsetDescriptionStripper;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

/**
 * @author Oliver Ferschke
 * @author Richard Eckart de Castilho
 */
public class MaltParserTest
{
    @Test
    public void testEnglishDependenciesDefault()
        throws Exception
    {
        JCas jcas = runTest("en", null, "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String[] dependencies = new String[] {
                "[  0,  2]Dependency(nsubj) D[0,2](We) G[3,7](need)",
                "[  8,  9]Dependency(det) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]Dependency(advmod) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]Dependency(amod) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]Dependency(nn) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]Dependency(dobj) D[35,43](sentence) G[3,7](need)",
                "[ 44, 45]Dependency(punct) D[44,45](,) G[35,43](sentence)",
                "[ 46, 51]Dependency(nsubj) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]Dependency(rcmod) D[52,60](contains) G[35,43](sentence)",
                "[ 61, 63]Dependency(prep) D[61,63](as) G[52,60](contains)",
                "[ 64, 68]Dependency(amod) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]Dependency(pobj) D[69,81](constituents) G[61,63](as)",
                "[ 82, 85]Dependency(cc) D[82,85](and) G[69,81](constituents)",
                "[ 86, 98]Dependency(conj) D[86,98](dependencies) G[69,81](constituents)",
                "[ 99,101]Dependency(prep) D[99,101](as) G[69,81](constituents)",
                "[102,110]Dependency(pobj) D[102,110](possible) G[99,101](as)",
                "[111,112]Dependency(punct) D[111,112](.) G[3,7](need)" };

        String[] posTags = new String[] { "#", "$", "''", "(", ")", ",", ".", ":", "CC", "CD",
                "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP", "NNPS", "NNS",
                "PDT", "POS", "PRP", "PRP$", "PRT", "RB", "RBR", "RBS", "RP", "SYM", "TO", "UH",
                "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };

        String[] depTags = new String[] { "ROOT", "abbrev", "acomp", "advcl", "advmod", "amod",
                "appos", "attr", "aux", "auxpass", "cc", "ccomp", "complm", "conj", "cop", "csubj",
                "csubjpass", "dep", "det", "dobj", "expl", "infmod", "iobj", "mark", "measure",
                "neg", "nn", "nsubj", "nsubjpass", "null", "num", "number", "parataxis", "partmod",
                "pcomp", "pobj", "poss", "possessive", "preconj", "pred", "predet", "prep", "prt",
                "punct", "purpcl", "quantmod", "rcmod", "rel", "tmod", "xcomp" };

        String[] unmappedPos = new String[] { "#", "$", "''", "(", ")", "PRT", "``" };

        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "stanford", depTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(Dependency.class, "stanford", new String[] {}, jcas);
    }

    @Test
    public void testEnglishDependenciesLinear()
        throws Exception
    {
        JCas jcas = runTest("en", "linear", "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String[] dependencies = new String[] {
                "[  0,  2]Dependency(nsubj) D[0,2](We) G[3,7](need)",
                "[  8,  9]Dependency(det) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]Dependency(advmod) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]Dependency(amod) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]Dependency(nn) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]Dependency(dobj) D[35,43](sentence) G[3,7](need)",
                "[ 44, 45]Dependency(punct) D[44,45](,) G[35,43](sentence)",
                "[ 46, 51]Dependency(nsubj) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]Dependency(rcmod) D[52,60](contains) G[35,43](sentence)",
                "[ 61, 63]Dependency(prep) D[61,63](as) G[52,60](contains)",
                "[ 64, 68]Dependency(amod) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]Dependency(pobj) D[69,81](constituents) G[61,63](as)",
                "[ 82, 85]Dependency(cc) D[82,85](and) G[69,81](constituents)",
                "[ 86, 98]Dependency(conj) D[86,98](dependencies) G[69,81](constituents)",
                "[ 99,101]Dependency(prep) D[99,101](as) G[69,81](constituents)",
                "[102,110]Dependency(pobj) D[102,110](possible) G[99,101](as)",
                "[111,112]Dependency(punct) D[111,112](.) G[3,7](need)" };

        String[] posTags = new String[] { "#", "$", "''", "(", ")", ",", ".", ":", "CC", "CD",
                "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP", "NNPS", "NNS",
                "PDT", "POS", "PRP", "PRP$", "PRT", "RB", "RBR", "RBS", "RP", "SYM", "TO", "UH",
                "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };

        String[] depTags = new String[] { "ROOT", "abbrev", "acomp", "advcl", "advmod", "amod",
                "appos", "attr", "aux", "auxpass", "cc", "ccomp", "complm", "conj", "cop", "csubj",
                "csubjpass", "dep", "det", "dobj", "expl", "infmod", "iobj", "mark", "measure",
                "neg", "nn", "nsubj", "nsubjpass", "null", "num", "number", "parataxis", "partmod",
                "pcomp", "pobj", "poss", "possessive", "preconj", "pred", "predet", "prep", "prt",
                "punct", "purpcl", "quantmod", "rcmod", "rel", "tmod", "xcomp" };

        String[] unmappedPos = new String[] { "#", "$", "''", "(", ")", "PRT", "``" };

        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "stanford", depTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(Dependency.class, "stanford", new String[] {}, jcas);
    }

    @Test
    public void testEnglishDependenciesPoly()
        throws Exception
    {
        JCas jcas = runTest("en", "poly", "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String[] dependencies = new String[] {
                "[  0,  2]Dependency(nsubj) D[0,2](We) G[3,7](need)",
                "[  8,  9]Dependency(det) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]Dependency(advmod) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]Dependency(amod) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]Dependency(nn) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]Dependency(dobj) D[35,43](sentence) G[3,7](need)",
                "[ 44, 45]Dependency(punct) D[44,45](,) G[35,43](sentence)",
                "[ 46, 51]Dependency(nsubj) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]Dependency(rcmod) D[52,60](contains) G[35,43](sentence)",
                "[ 61, 63]Dependency(prep) D[61,63](as) G[52,60](contains)",
                "[ 64, 68]Dependency(amod) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]Dependency(pobj) D[69,81](constituents) G[61,63](as)",
                "[ 82, 85]Dependency(cc) D[82,85](and) G[69,81](constituents)",
                "[ 86, 98]Dependency(conj) D[86,98](dependencies) G[69,81](constituents)",
                "[ 99,101]Dependency(prep) D[99,101](as) G[69,81](constituents)",
                "[102,110]Dependency(pobj) D[102,110](possible) G[99,101](as)",
                "[111,112]Dependency(punct) D[111,112](.) G[3,7](need)" };

        String[] posTags = new String[] { "#", "$", "''", "(", ")", ",", ".", ":", "CC", "CD",
                "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP", "NNPS", "NNS",
                "PDT", "POS", "PRP", "PRP$", "PRT", "RB", "RBR", "RBS", "RP", "SYM", "TO", "UH",
                "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };

        String[] depTags = new String[] { "ROOT", "abbrev", "acomp", "advcl", "advmod", "amod",
                "appos", "attr", "aux", "auxpass", "cc", "ccomp", "complm", "conj", "cop", "csubj",
                "csubjpass", "dep", "det", "dobj", "expl", "infmod", "iobj", "mark", "measure",
                "neg", "nn", "nsubj", "nsubjpass", "null", "num", "number", "parataxis", "partmod",
                "pcomp", "pobj", "poss", "possessive", "preconj", "pred", "predet", "prep", "prt",
                "punct", "purpcl", "quantmod", "rcmod", "rel", "tmod", "xcomp" };

        String[] unmappedPos = new String[] { "#", "$", "''", "(", ")", "PRT", "``" };

        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "stanford", depTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(Dependency.class, "stanford", new String[] {}, jcas);
    }

    /**
     * This test really only checks the tagsets and if any dependencies are created. Since the POS
     * tags expected by the Spanish model do <b>not</b> correspond to those that the pos tagger
     * running here produces, the dependencies are bogus.
     */
    @Test
    public void testSpanishDependenciesLinear()
        throws Exception
    {
        JCas jcas = runTest("dummy-es", "linear",
                "Tenemos un ejemplo de frase muy complicado, que "
                        + "contiene tantas componentes y dependencias como sea posible .");

        String[] dependencies = new String[] {
                "[  8, 10]Dependency(SPEC) D[8,10](un) G[11,18](ejemplo)",
                "[ 11, 18]Dependency(SUBJ) D[11,18](ejemplo) G[19,21](de)",
                "[ 19, 21]Dependency(MOD) D[19,21](de) G[0,7](Tenemos)",
                "[ 22, 27]Dependency(MOD) D[22,27](frase) G[32,43](complicado,)",
                "[ 28, 31]Dependency(SPEC) D[28,31](muy) G[32,43](complicado,)",
                "[ 32, 43]Dependency(SUBJ) D[32,43](complicado,) G[44,47](que)",
                "[ 44, 47]Dependency(DO) D[44,47](que) G[19,21](de)",
                "[ 48, 56]Dependency(MOD) D[48,56](contiene) G[57,63](tantas)",
                "[ 57, 63]Dependency(SUBJ) D[57,63](tantas) G[78,90](dependencias)",
                "[ 64, 75]Dependency(punct) D[64,75](componentes) G[57,63](tantas)",
                "[ 76, 77]Dependency(MOD) D[76,77](y) G[78,90](dependencias)",
                "[ 78, 90]Dependency(MOD) D[78,90](dependencias) G[0,7](Tenemos)",
                "[ 91, 95]Dependency(MOD) D[91,95](como) G[96,99](sea)",
                "[ 96, 99]Dependency(SUBJ) D[96,99](sea) G[100,107](posible)",
                "[100,107]Dependency(MOD) D[100,107](posible) G[78,90](dependencias)",
                "[108,109]Dependency(punct) D[108,109](.) G[100,107](posible)" };

        String[] posTags = new String[] { "AO0FP0", "AO0FS0", "AO0MP0", "AO0MS0", "AQ0CN0",
                "AQ0CP0", "AQ0CS0", "AQ0FP0", "AQ0FS0", "AQ0FSP", "AQ0MP0", "AQ0MPP", "AQ0MS0",
                "AQ0MSP", "AQDFS0", "AQDMP0", "AQDMS0", "AQSFP0", "AQSFS0", "AQSMP0", "AQSMS0",
                "CC", "CS", "DA0FP0", "DA0FS0", "DA0MP0", "DA0MS0", "DA0NS0", "DD0CP0", "DD0CS0",
                "DD0FP0", "DD0FS0", "DD0MP0", "DD0MS0", "DI0CP0", "DI0CS0", "DI0FP0", "DI0FS0",
                "DI0MP0", "DI0MS0", "DP1CPS", "DP1CSS", "DP1FPP", "DP1FSP", "DP1MPP", "DP1MSP",
                "DP2CSS", "DP3CP0", "DP3CS0", "DT0CN0", "DT0FP0", "Fat", "Fc", "Fd", "Fe", "Fia",
                "Fit", "Fp", "Fpa", "Fpt", "Fra", "Frc", "Fs", "Fx", "I", "NC00000", "NCCN000",
                "NCCP000", "NCCS000", "NCFN000", "NCFP000", "NCFP00A", "NCFP00D", "NCFS000",
                "NCFS00A", "NCFS00X", "NCMN000", "NCMP000", "NCMP00A", "NCMP00D", "NCMS000",
                "NCMS00A", "NCMS00D", "NP00000", "P03CN000", "PD0CP000", "PD0CS000", "PD0FP000",
                "PD0FS000", "PD0MP000", "PD0MS000", "PD0NS000", "PI0CC000", "PI0CP000", "PI0CS000",
                "PI0FP000", "PI0FS000", "PI0MP000", "PI0MS000", "PP1CP000", "PP1CS000", "PP1CSN00",
                "PP1CSO00", "PP1MP000", "PP2CP00P", "PP2CS000", "PP2CS00P", "PP3CN000", "PP3CNO00",
                "PP3CPD00", "PP3CSD00", "PP3FP000", "PP3FPA00", "PP3FS000", "PP3FSA00", "PP3MP000",
                "PP3MPA00", "PP3MS000", "PP3MSA00", "PP3NS000", "PR000000", "PR0CN000", "PR0CP000",
                "PR0CS000", "PR0FP000", "PR0FS000", "PR0MP000", "PR0MS000", "PT000000", "PT0CN000",
                "PT0CP000", "PT0CS000", "PT0MP000", "PT0MS000", "PX1FP0P0", "PX1FS0P0", "PX1MS0P0",
                "PX3MS0C0", "PX3NS0C0", "RG", "RN", "SPS00", "VAG0000", "VAIC1P0", "VAIC3P0",
                "VAIC4S0", "VAIF1P0", "VAIF3P0", "VAIF3S0", "VAII1P0", "VAII3P0", "VAII4S0",
                "VAIP1P0", "VAIP1S0", "VAIP3P0", "VAIP3S0", "VAIS3P0", "VAIS3S0", "VAN0000",
                "VAP00SM", "VASF3P0", "VASF4S0", "VASI1P0", "VASI3P0", "VASI4S0", "VASP1P0",
                "VASP3P0", "VASP4S0", "VMG0000", "VMIB1P0", "VMIC1P0", "VMIC3P0", "VMIC3S0",
                "VMIC4S0", "VMIF1P0", "VMIF1S0", "VMIF3P0", "VMIF3S0", "VMII1P0", "VMII2S0",
                "VMII3P0", "VMII3S0", "VMII4S0", "VMIP1P0", "VMIP1S0", "VMIP2S0", "VMIP3P0",
                "VMIP3S0", "VMIS1P0", "VMIS1S0", "VMIS3P0", "VMIS3S0", "VMM01P0", "VMM02S0",
                "VMM03P0", "VMM03S0", "VMN0000", "VMP00PF", "VMP00PM", "VMP00SF", "VMP00SM",
                "VMSF2S0", "VMSF3P0", "VMSF3S0", "VMSF4S0", "VMSI1P0", "VMSI3P0", "VMSI3S0",
                "VMSI4S0", "VMSP1P0", "VMSP1S0", "VMSP2S0", "VMSP3P0", "VMSP3S0", "VMSP4S0",
                "VSG0000", "VSIC3P0", "VSIC4S0", "VSIF3P0", "VSIF3S0", "VSII3P0", "VSII4S0",
                "VSIP1P0", "VSIP1S0", "VSIP3P0", "VSIP3S0", "VSIS3P0", "VSIS3S0", "VSN0000",
                "VSP00SM", "VSSF3P0", "VSSF4S0", "VSSI3P0", "VSSI4S0", "VSSP3P0", "VSSP4S0", "W",
                "Z", "ZD", "ZP", "_" };

        String[] depTags = new String[] { "ADV", "ATR", "AUX", "BYAG", "COMP", "COMP-GAP", "COMPL",
                "CONJ", "COORD", "DO", "IO", "MIMPERS", "MOD", "MOD-GAP", "MPAS", "MPRON", "OBLC",
                "OPRD", "PP-DIR", "PP-LOC", "PRD", "PRDC", "ROOT", "SPEC", "SUBJ", "SUBJ-GAP", "_",
                "punct" };

        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "freeling", posTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(POS.class, "freeling", new String[] {}, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "iula", depTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(Dependency.class, "iula", new String[] {}, jcas);
    }

    @Test
    public void testGermanDependencies()
        throws Exception
    {
        checkModel("de", "linear");

        JCas jcas = runTest("de", "linear", "Wir brauchen ein sehr kompliziertes Beispiel , "
                + "welches möglichst viele Konstituenten und Dependenzen beinhaltet .");

        String[] dependencies = new String[] {
                "[  4, 12]Dependency(ROOT) D[4,12](brauchen) G[0,3](Wir)",
                "[ 13, 16]Dependency(DET) D[13,16](ein) G[4,12](brauchen)",
                "[ 17, 21]Dependency(DET) D[17,21](sehr) G[13,16](ein)",
                "[ 22, 35]Dependency(DET) D[22,35](kompliziertes) G[36,44](Beispiel)",
                "[ 36, 44]Dependency(GMOD) D[36,44](Beispiel) G[17,21](sehr)",
                "[ 45, 46]Dependency(KON) D[45,46](,) G[4,12](brauchen)",
                "[ 47, 54]Dependency(DET) D[47,54](welches) G[45,46](,)",
                "[ 55, 64]Dependency(DET) D[55,64](möglichst) G[47,54](welches)",
                "[ 65, 70]Dependency(DET) D[65,70](viele) G[55,64](möglichst)",
                "[ 71, 84]Dependency(DET) D[71,84](Konstituenten) G[65,70](viele)",
                "[ 85, 88]Dependency(DET) D[85,88](und) G[71,84](Konstituenten)",
                "[ 89,100]Dependency(DET) D[89,100](Dependenzen) G[85,88](und)",
                "[101,111]Dependency(PN) D[101,111](beinhaltet) G[112,113](.)",
                "[112,113]Dependency(-PUNCT-) D[112,113](.) G[89,100](Dependenzen)" };

        String[] posTags = new String[] { "$(", "$,", "$.", "ADJA", "ADJD", "ADV", "APPO", "APPR",
                "APPRART", "APZR", "ART", "CARD", "FM", "ITJ", "KOKOM", "KON", "KOUI", "KOUS",
                "NE", "NN", "PDAT", "PDS", "PIAT", "PIDAT", "PIS", "PPER", "PPOSAT", "PPOSS",
                "PRELAT", "PRELS", "PRF", "PROP", "PTKA", "PTKANT", "PTKNEG", "PTKVZ", "PTKZU",
                "PWAT", "PWAV", "PWS", "TRUNC", "VAFIN", "VAIMP", "VAINF", "VAPP", "VMFIN",
                "VMINF", "VMPP", "VVFIN", "VVIMP", "VVINF", "VVIZU", "VVPP", "XY" };

        String[] depTags = new String[] { "-PUNCT-", "-UNKNOWN-", "ADV", "APP", "ATTR", "AUX",
                "AVZ", "CJ", "DET", "EXPL", "GMOD", "GRAD", "KOM", "KON", "KONJ", "NEB", "OBJA",
                "OBJC", "OBJD", "OBJG", "OBJI", "OBJP", "PAR", "PART", "PN", "PP", "PRED", "REL",
                "ROOT", "S", "SUBJ", "SUBJC", "ZEIT", "gmod-app", "koord" };

        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        // FIXME AssertAnnotations.assertTagset(POS.class, "stts", posTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(POS.class, "stts", new String[] {}, jcas);
        // FIXME AssertAnnotations.assertTagset(Dependency.class, "cdg", depTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(Dependency.class, "cdg", new String[] {}, jcas);
    }

    /**
     * This test really only checks the tagsets and if any dependencies are created. The
     * dependencies are at least half-bogus because the tagset used by the POS tagger does not
     * correspond to the one used by the dependency parser.
     */
    @Test
    public void testSwedishDependencies()
        throws Exception
    {
        JCas jcas = runTest("sv", "linear",
                "Vi behöver en mycket komplicerad exempel meningen som "
                        + "innehåller lika många beståndsdelar och beroenden som möjligt.");

        String[] dependencies = new String[] {
                "[ 14, 20]Dependency(DT) D[14,20](mycket) G[33,40](exempel)",
                "[ 21, 32]Dependency(HD) D[21,32](komplicerad) G[14,20](mycket)",
                "[ 50, 53]Dependency(HD) D[50,53](som) G[41,49](meningen)",
                "[ 54, 64]Dependency(HD) D[54,64](innehåller) G[41,49](meningen)",
                "[ 65, 69]Dependency(DT) D[65,69](lika) G[76,89](beståndsdelar)",
                "[ 70, 75]Dependency(HD) D[70,75](många) G[65,69](lika)",
                "[ 76, 89]Dependency(+F) D[76,89](beståndsdelar) G[33,40](exempel)",
                "[ 90, 93]Dependency(ROOT) D[90,93](och) G[76,89](beståndsdelar)",
                "[ 94,103]Dependency(AA) D[94,103](beroenden) G[90,93](och)",
                "[104,107]Dependency(HD) D[104,107](som) G[94,103](beroenden)",
                "[108,116]Dependency(HD) D[108,116](möjligt.) G[94,103](beroenden)" };

        String[] posTags = new String[] { "AB", "DT", "HA", "HD", "HP", "HS", "IE", "IN", "JJ",
                "KN", "MAD", "MID", "NN", "PAD", "PC", "PL", "PM", "PN", "PP", "PS", "RG", "RO",
                "SN", "UO", "VB" };

        String[] depTags = new String[] { "+A", "+F", "AA", "AG", "AN", "AT", "CA", "CJ", "DB",
                "DT", "EF", "EO", "ES", "ET", "FO", "FP", "FS", "FV", "HA", "HD", "I?", "IC", "IF",
                "IG", "IK", "IO", "IP", "IQ", "IR", "IS", "IT", "IU", "JC", "JG", "JR", "JT", "KA",
                "MA", "MS", "NA", "OA", "OO", "OP", "PA", "PL", "PT", "RA", "ROOT", "SP", "SS",
                "TA", "UA", "VA", "VG", "VO", "VS", "XA", "XF", "XT", "XX", "YY" };

        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "stb", posTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(POS.class, "stb", new String[] {}, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "stb", depTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(Dependency.class, "stb", new String[] {}, jcas);
    }

    /**
     * This test really only checks the tagsets and if any dependencies are created. Since we
     * currently to not have a POS tagger for French, the dependencies are just bogus.
     */
    // @Ignore("The tags produced by our French TreeTagger model are different form the ones that "
    // +
    // "the pre-trained MaltParser model expects. Also the input format in our MaltParser " +
    // "class is currently hardcoded to the format used by the English pre-trained model. " +
    // "For the French model the 5th column of the input format should contain fine-grained " +
    // "tags. See http://www.maltparser.org/mco/french_parser/fremalt.html")
    @Test
    public void testFrenchDependencies()
        throws Exception
    {
        JCas jcas = runTest(
                "dummy-fr",
                "linear",
                "Nous avons besoin d'une phrase par exemple très "
                        + "compliqué, qui contient des constituants que de nombreuses dépendances et que "
                        + "possible.");

        String[] dependencies = new String[] {
                "[ 96,106]Dependency(det) D[96,106](nombreuses) G[107,118](dépendances)",
                "[107,118]Dependency(obj) D[107,118](dépendances) G[93,95](de)",
                "[119,121]Dependency(dep) D[119,121](et) G[107,118](dépendances)",
                "[122,125]Dependency(dep) D[122,125](que) G[107,118](dépendances)",
                "[126,135]Dependency(dep) D[126,135](possible.) G[107,118](dépendances)" };

        String[] posTags = new String[] { "/CC", "/P", "/PONCT", "4/DET", "ADJ", "ADJWH", "ADV",
                "ADVWH", "CC", "CLO", "CLR", "CLS", "CS", "DET", "DETWH", "ET", "I", "NC", "NPP",
                "P", "P+D", "P+PRO", "PONCT", "PREF", "PRO", "PROREL", "PROWH", "V", "VIMP",
                "VINF", "VPP", "VPR", "VS", "_9/NC", "_OPE/NC", "_S/ET", "_S/NPP", "_an/NC",
                "_h/NC" };

        String[] depTags = new String[] { "a_obj", "aff", "arg", "ato", "ats", "aux_caus",
                "aux_pass", "aux_tps", "comp", "coord", "de_obj", "dep", "dep_coord", "det",
                "missinghead", "mod", "mod_rel", "obj", "obj1", "p_obj", "ponct", "root", "suj" };

        String[] unmappedPos = new String[] { "/CC", "/P", "/PONCT", "4/DET", "_9/NC", "_OPE/NC",
                "_S/ET", "_S/NPP", "_an/NC", "_h/NC" };
        
        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "melt", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "melt", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "ftb", depTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(Dependency.class, "ftb", new String[] {}, jcas);
    }

    private JCas runTest(String aLanguage, String aVariant, String aText)
        throws Exception
    {
        List<AnalysisEngineDescription> engines = new ArrayList<AnalysisEngineDescription>();

        if (aLanguage.startsWith("dummy-")) {
            aLanguage = aLanguage.substring("dummy-".length());
            // This is used if we do not have a proper tagger for this language
            engines.add(createEngineDescription(OpenNlpPosTagger.class,
                    OpenNlpPosTagger.PARAM_LANGUAGE, "en"));
        }
        else {
            engines.add(createEngineDescription(OpenNlpPosTagger.class));
        }

        engines.add(createEngineDescription(TagsetDescriptionStripper.class));

        engines.add(createEngineDescription(MaltParser.class, MaltParser.PARAM_VARIANT, aVariant,
                MaltParser.PARAM_PRINT_TAGSET, true));

        AnalysisEngineDescription engine = createEngineDescription(engines
                .toArray(new AnalysisEngineDescription[engines.size()]));

        return TestRunner.runTest(engine, aLanguage, aText);
    }

    private void checkModel(String aLanguage, String aVariant)
    {
        Assume.assumeTrue(getClass().getResource(
                "/de/tudarmstadt/ukp/dkpro/core/maltparser/lib/parser-" + aLanguage + "-"
                        + aVariant + ".mco") != null);
    }

    @Rule
    public TestName testName = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + testName.getMethodName() + " =====================");
    }
}
