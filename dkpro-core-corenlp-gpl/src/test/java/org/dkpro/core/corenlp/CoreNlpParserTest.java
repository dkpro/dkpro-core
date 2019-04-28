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
package org.dkpro.core.corenlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectSingle;
import static org.dkpro.core.testing.AssertAnnotations.assertConstituents;
import static org.dkpro.core.testing.AssertAnnotations.assertDependencies;
import static org.dkpro.core.testing.AssertAnnotations.assertPOS;
import static org.dkpro.core.testing.AssertAnnotations.assertPennTree;
import static org.dkpro.core.testing.AssertAnnotations.assertSyntacticFunction;
import static org.dkpro.core.testing.AssertAnnotations.assertTagset;
import static org.dkpro.core.testing.AssertAnnotations.assertTagsetMapping;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.corenlp.CoreNlpParser;
import org.dkpro.core.corenlp.CoreNlpPosTagger;
import org.dkpro.core.corenlp.internal.DKPro2CoreNlp;
import org.dkpro.core.testing.AssumeResource;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.TestRunner;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import edu.stanford.nlp.ling.StringLabel;
import edu.stanford.nlp.trees.Tree;

public class CoreNlpParserTest
{
    private static final String[] GERMAN_POS_TAGS = { "$,", "$.", "$[", "ADJA", "ADJD", "ADV",
            "APPO", "APPR", "APPRART", "APZR", "ART", "CARD", "FM", "ITJ", "KOKOM", "KON", "KOUI",
            "KOUS", "NE", "NN", "PDAT", "PDS", "PIAT", "PIDAT", "PIS", "PPER", "PPOSAT", "PPOSS",
            "PRELAT", "PRELS", "PRF", "PROAV", "PTKA", "PTKANT", "PTKNEG", "PTKVZ", "PTKZU", "PWAT",
            "PWAV", "PWS", "TRUNC", "VAFIN", "VAIMP", "VAINF", "VAPP", "VMFIN", "VMINF", "VMPP",
            "VVFIN", "VVIMP", "VVINF", "VVIZU", "VVPP", "XY" };

    private static final String[] GERMAN_CONSTITUENT_TAGS = { "AA", "AP", "AVP", "CAC", "CAP",
            "CAVP", "CCP", "CH", "CNP", "CO", "CPP", "CS", "CVP", "CVZ", "DL", "ISU", "MPN", "MTA",
            "NM", "NP", "NUR", "PP", "QL", "ROOT", "S", "VP", "VZ" };

    private static final String[] ENGLISH_POS_TAGS = { "#", "$", "''", ",", "-LRB-", "-RRB-", ".",
            ":", "CC", "CD", "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP",
            "NNPS", "NNS", "PDT", "POS", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM", "TO", "UH",
            "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };

    private static final String[] ENGLISH_POS_UNMAPPED = {};

    private static final String[] ENGLISH_CONSTITUENT_TAGS = { "ADJP", "ADVP", "CONJP", "FRAG",
            "INTJ", "LST", "NAC", "NP", "NX", "PP", "PRN", "PRT", "QP", "ROOT", "RRC", "S", "SBAR",
            "SBARQ", "SINV", "SQ", "UCP", "VP", "WHADJP", "WHADVP", "WHNP", "WHPP", "X" };

    private static final String[] ENGLISH_CONSTITUENT_UNMAPPED = {};

    private static final String[] ENGLISH_DEPENDENCY_TAGS = { "acomp", "advcl", "advmod", "agent",
            "amod", "appos", "arg", "aux", "auxpass", "cc", "ccomp", "comp", "conj", "cop", "csubj",
            "csubjpass", "dep", "det", "discourse", "dobj", "expl", "goeswith", "gov", "iobj",
            "mark", "mod", "mwe", "neg", "nn", "npadvmod", "nsubj", "nsubjpass", "num", "number",
            "obj", "parataxis", "pcomp", "pobj", "poss", "possessive", "preconj", "pred", "predet",
            "prep", "prt", "punct", "quantmod", "rcmod", "ref", "rel", "sdep", "subj", "tmod",
            "vmod", "xcomp" };

    private static final String[] SPANISH_POS_TAGS = { "359000", "NCMS000", "ac0000", "ao0000",
            "ap0000", "aq0000", "aqs000", "cc", "cs", "d00000", "da0000", "dd0000", "de0000",
            "di0000", "dn0000", "do0000", "dp0000", "dt0000", "f0", "faa", "fat", "fc", "fca",
            "fct", "fd", "fe", "fg", "fh", "fi", "fia", "fit", "fp", "fpa", "fpt", "fra", "frc",
            "fs", "fsa", "ft", "fx", "fz", "i", "nc00000", "nc0a000", "nc0c000", "nc0n000",
            "nc0p000", "nc0s000", "np00000", "p0000000", "pd000000", "pe000000", "pi000000",
            "pn000000", "po000000", "pp000000", "pr000000", "pt000000", "px000000", "rg", "rn",
            "sc000", "se000", "sp000", "va00000", "vag0000", "vaic000", "vaif000", "vaii000",
            "vaip000", "vais000", "vam0000", "van0000", "vap0000", "vasi000", "vasp000", "vass000",
            "vm00000", "vm0p000", "vmg0000", "vmi0000", "vmi2000", "vmic000", "vmif000", "vmii000",
            "vmim000", "vmip000", "vmis000", "vmm0000", "vmmp000", "vmms000", "vmn0000", "vmp0000",
            "vms0000", "vmsf000", "vmsi000", "vmsp000", "vq00000", "vs00000", "vsg0000", "vsic000",
            "vsif000", "vsii000", "vsip000", "vsis000", "vsm0000", "vsmp000", "vsn0000", "vsp0000",
            "vssf000", "vssi000", "vssp000", "vsss000", "w", "word", "z0", "zd", "zm", "zp", "zu" };

    private static final String[] FRENCH_POS_TAGS = { "A", "ADJ", "ADJWH", "ADV", "ADVWH", "C",
            "CC", "CL", "CLO", "CLR", "CLS", "CS", "DET", "DETWH", "ET", "I", "N", "NC", "NPP", "P",
            "PREF", "PRO", "PROREL", "PROWH", "PUNC", "V", "VIMP", "VINF", "VPP", "VPR", "VS" };

    // TODO Maybe test link to parents (not tested by syntax tree recreation)

    @Test
    public void testGermanPcfg()
        throws Exception
    {
        JCas jcas = runTest("de", "pcfg", "Wir brauchen ein sehr kompliziertes Beispiel , welches "
                + "möglichst viele Konstituenten und Dependenzen beinhaltet .");

        String[] constituentMapped = { "ADJP 17,35", "NP 13,111", "NP 55,100", "NP 71,100",
                "ROOT 0,113", "S 0,113", "S 47,111" };

        String[] constituentOriginal = { "AP 17,35", "CNP 71,100", "NP 13,111", "NP 55,100",
                "ROOT 0,113", "S 0,113", "S 47,111" };

        String[] synFunc = {};

        String[] posOriginal = { "PPER", "VVFIN", "ART", "ADV", "ADJA", "NN", "$,", "PRELS", "ADV",
                "PIDAT", "NN", "KON", "NN", "VVFIN", "$." };

        String[] posMapped = { "POS_PRON", "POS_VERB", "POS_DET", "POS_ADV", "POS_ADJ", "POS_NOUN", "POS_PUNCT", "POS_PRON", "POS_ADV",
                "POS_PRON", "POS_NOUN", "POS_CONJ", "POS_NOUN", "POS_VERB", "POS_PUNCT" };

        String[] dependencies = {/** No dependencies for German */ };

        String pennTree = "(ROOT (S (PPER Wir) (VVFIN brauchen) (NP (ART ein) (AP (ADV sehr) "
                + "(ADJA kompliziertes)) (NN Beispiel) ($, ,) (S (PRELS welches) (NP "
                + "(ADV möglichst) (PIDAT viele) (CNP (NN Konstituenten) (KON und) "
                + "(NN Dependenzen))) (VVFIN beinhaltet))) ($. .)))";

        String[] unmappedPos = { "$[" };

        String[] unmappedConst = { "NUR" };

        assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        assertSyntacticFunction(synFunc, select(jcas, Constituent.class));
        assertDependencies(dependencies, select(jcas, Dependency.class));
        assertTagset(POS.class, "stts", GERMAN_POS_TAGS, jcas);
        assertTagsetMapping(POS.class, "stts", unmappedPos, jcas);
        assertTagset(Constituent.class, "negra", GERMAN_CONSTITUENT_TAGS, jcas);
        assertTagsetMapping(Constituent.class, "negra", unmappedConst, jcas);
    }

    @Test
    public void testGermanFactored()
        throws Exception
    {
        JCas jcas = runTest("de", "factored",
                "Wir brauchen ein sehr kompliziertes Beispiel , welches "
                        + "möglichst viele Konstituenten und Dependenzen beinhaltet .");

        String[] constituentMapped = { "ADJP 17,35", "ADJP 55,70", "NP 13,111", "NP 55,100",
                "NP 71,100", "ROOT 0,113", "S 0,113", "S 47,111" };

        String[] constituentOriginal = { "AP 17,35", "AP 55,70", "CNP 71,100", "NP 13,111",
                "NP 55,100", "ROOT 0,113", "S 0,113", "S 47,111" };

        String[] posOriginal = { "PPER", "VVFIN", "ART", "ADV", "ADJA", "NN", "$,", "PRELS", "ADV",
                "PIDAT", "NN", "KON", "NN", "VVFIN", "$." };

        String[] posMapped = { "POS_PRON", "POS_VERB", "POS_DET", "POS_ADV", "POS_ADJ", "POS_NOUN", "POS_PUNCT", "POS_PRON", "POS_ADV",
                "POS_PRON", "POS_NOUN", "POS_CONJ", "POS_NOUN", "POS_VERB", "POS_PUNCT" };

        String[] dependencies = { /** No dependencies for German */ };

        String pennTree = "(ROOT (S (PPER Wir) (VVFIN brauchen) (NP (ART ein) (AP "
                + "(ADV sehr) (ADJA kompliziertes)) (NN Beispiel) ($, ,) (S (PRELS welches) "
                + "(NP (AP (ADV möglichst) (PIDAT viele)) (CNP (NN Konstituenten) (KON und) "
                + "(NN Dependenzen))) (VVFIN beinhaltet))) ($. .)))";

        String[] unmappedPos = { "$[" };

        String[] unmappedConst = { "NUR" };

        assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        assertDependencies(dependencies, select(jcas, Dependency.class));
        assertTagset(POS.class, "stts", GERMAN_POS_TAGS, jcas);
        assertTagsetMapping(POS.class, "stts", unmappedPos, jcas);
        assertTagset(Constituent.class, "negra", GERMAN_CONSTITUENT_TAGS, jcas);
        assertTagsetMapping(Constituent.class, "negra", unmappedConst, jcas, true);
    }

    @Test
    public void testEnglishPcfg()
        throws Exception
    {
        JCas jcas = runTest("en", "pcfg", "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String[] constituentMapped = { "ADJP 10,26", "ADJP 102,110", "NP 0,2", "NP 64,98",
                "NP 8,110", "NP 8,43", "PP 61,98", "PP 99,110", "ROOT 0,112", "S 0,112",
                "S 52,110", "SBAR 46,110", "VP 3,110", "VP 52,110", "WHNP 46,51" };

        String[] constituentOriginal = { "ADJP 10,26", "ADJP 102,110", "NP 0,2", "NP 64,98",
                "NP 8,110", "NP 8,43", "PP 61,98", "PP 99,110", "ROOT 0,112", "S 0,112",
                "S 52,110", "SBAR 46,110", "VP 3,110", "VP 52,110", "WHNP 46,51" };

        String[] dependencies = {
                "[  0,  2]NSUBJ(nsubj,basic) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root,basic) D[3,7](need) G[3,7](need)",
                "[  8,  9]DET(det,basic) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]ADVMOD(advmod,basic) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]AMOD(amod,basic) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]NN(nn,basic) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]DOBJ(dobj,basic) D[35,43](sentence) G[3,7](need)",
                "[ 46, 51]NSUBJ(nsubj,basic) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]RCMOD(rcmod,basic) D[52,60](contains) G[35,43](sentence)",
                "[ 64, 68]AMOD(amod,basic) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]PREP(prep_as,basic) D[69,81](constituents) G[52,60](contains)",
                "[ 86, 98]CONJ(conj_and,basic) D[86,98](dependencies) G[69,81](constituents)",
                "[102,110]PREP(prep_as,basic) D[102,110](possible) G[52,60](contains)" };

        String[] posMapped = { "POS_PRON", "POS_VERB", "POS_DET", "POS_ADV", "POS_VERB", "POS_NOUN",
                "POS_NOUN", "POS_PUNCT", "POS_DET", "POS_VERB", "POS_ADP", "POS_ADJ", "POS_NOUN",
                "POS_CONJ", "POS_NOUN", "POS_ADP", "POS_ADJ", "POS_PUNCT" };

        String[] posOriginal = { "PRP", "VBP", "DT", "RB", "VBN", "NN", "NN", ",", "WDT", "VBZ",
                "IN", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };

        String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) "
                + "(VBN complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP (WDT which)) "
                + "(S (VP (VBZ contains) (PP (IN as) (NP (JJ many) (NNS constituents) (CC and) "
                + "(NNS dependencies))) (PP (IN as) (ADJP (JJ possible)))))))) (. .)))";

        String[] unmappedDep = { "gov" };

        assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        assertDependencies(dependencies, select(jcas, Dependency.class));
        assertTagset(POS.class, "ptb", ENGLISH_POS_TAGS, jcas);
        assertTagsetMapping(POS.class, "ptb", ENGLISH_POS_UNMAPPED, jcas);
        assertTagset(Constituent.class, "ptb", ENGLISH_CONSTITUENT_TAGS, jcas);
        assertTagsetMapping(Constituent.class, "ptb", ENGLISH_CONSTITUENT_UNMAPPED, jcas);
        assertTagset(Dependency.class, "stanford341", ENGLISH_DEPENDENCY_TAGS, jcas);
        assertTagsetMapping(Dependency.class, "stanford341", unmappedDep, jcas);
    }

    @Test
    public void testEnglishFactored()
        throws Exception
    {
        JCas jcas = runTest("en", "factored", "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String[] constituentMapped = { "ADJP 10,26", "ADJP 102,110", "ADJP 61,68", "NP 0,2",
                "NP 61,98", "NP 8,110", "NP 8,43", "PP 99,110", "ROOT 0,112", "S 0,112",
                "S 52,110", "SBAR 46,110", "VP 3,110", "VP 52,110", "WHNP 46,51" };

        String[] constituentOriginal = { "ADJP 10,26", "ADJP 102,110", "ADJP 61,68", "NP 0,2",
                "NP 61,98", "NP 8,110", "NP 8,43", "PP 99,110", "ROOT 0,112", "S 0,112",
                "S 52,110", "SBAR 46,110", "VP 3,110", "VP 52,110", "WHNP 46,51" };

        String[] dependencies = {
                "[  0,  2]NSUBJ(nsubj,basic) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root,basic) D[3,7](need) G[3,7](need)",
                "[  8,  9]DET(det,basic) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]ADVMOD(advmod,basic) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]AMOD(amod,basic) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]NN(nn,basic) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]DOBJ(dobj,basic) D[35,43](sentence) G[3,7](need)",
                "[ 46, 51]NSUBJ(nsubj,basic) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]RCMOD(rcmod,basic) D[52,60](contains) G[35,43](sentence)",
                "[ 61, 63]ADVMOD(advmod,basic) D[61,63](as) G[64,68](many)",
                "[ 64, 68]AMOD(amod,basic) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]DOBJ(dobj,basic) D[69,81](constituents) G[52,60](contains)",
                "[ 86, 98]CONJ(conj_and,basic) D[86,98](dependencies) G[69,81](constituents)",
                "[102,110]PREP(prep_as,basic) D[102,110](possible) G[52,60](contains)" };

        String[] posMapped = { "POS_PRON", "POS_VERB", "POS_DET", "POS_ADV", "POS_VERB", "POS_NOUN",
                "POS_NOUN", "POS_PUNCT", "POS_DET", "POS_VERB", "POS_ADV", "POS_ADJ", "POS_NOUN",
                "POS_CONJ", "POS_NOUN", "POS_ADP", "POS_ADJ", "POS_PUNCT" };

        String[] posOriginal = { "PRP", "VBP", "DT", "RB", "VBN", "NN", "NN", ",", "WDT", "VBZ",
                "RB", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };

        String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) "
                + "(VBN complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP (WDT which)) "
                + "(S (VP (VBZ contains) (NP (ADJP (RB as) (JJ many)) (NNS constituents) (CC and) "
                + "(NNS dependencies)) (PP (IN as) (ADJP (JJ possible)))))))) (. .)))";

        String[] unmappedDep = { "gov" };

        assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        assertDependencies(dependencies, select(jcas, Dependency.class));
        assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        assertTagset(POS.class, "ptb", ENGLISH_POS_TAGS, jcas);
        assertTagsetMapping(POS.class, "ptb", ENGLISH_POS_UNMAPPED, jcas);
        assertTagset(Constituent.class, "ptb", ENGLISH_CONSTITUENT_TAGS, jcas);
        assertTagsetMapping(Constituent.class, "ptb", ENGLISH_CONSTITUENT_UNMAPPED, jcas);
        assertTagset(Dependency.class, "stanford341", ENGLISH_DEPENDENCY_TAGS, jcas);
        assertTagsetMapping(Dependency.class, "stanford341", unmappedDep, jcas);
    }

    // CoreNlpParser PARAM_KEEP_PUNCTUATION has no effect #965
//    @Ignore("Only supported in CoreNLP 3.6.0")
//    @Test
//    public void testEnglishKeepPunctuation()
//        throws Exception
//    {
//        JCas jcas = runTest("en", "rnn", "This is a test .", 
//                CoreNlpParser.PARAM_KEEP_PUNCTUATION, true);
//
//        String[] dependencies = {
//                "[  0,  4]NSUBJ(nsubj) D[0,4](This) G[10,14](test)",
//                "[  5,  7]COP(cop) D[5,7](is) G[10,14](test)",
//                "[  8,  9]DET(det) D[8,9](a) G[10,14](test)",
//                "[ 10, 14]ROOT(root) D[10,14](test) G[10,14](test)" };
//
//        assertDependencies(dependencies, select(jcas, Dependency.class));
//    }

    @Test
    public void testEnglishRnn()
        throws Exception
    {
        JCas jcas = runTest("en", "rnn", "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String[] constituentMapped = { "ADJP 10,26", "ADJP 102,110", "NP 0,2", "NP 61,98",
                "NP 8,110", "NP 8,43", "PP 99,110", "QP 61,68", "ROOT 0,112", "S 0,112",
                "S 52,110", "SBAR 46,110", "VP 3,110", "VP 52,110", "WHNP 46,51" };

        String[] constituentOriginal = { "ADJP 10,26", "ADJP 102,110", "NP 0,2", "NP 61,98",
                "NP 8,110", "NP 8,43", "PP 99,110", "QP 61,68", "ROOT 0,112", "S 0,112",
                "S 52,110", "SBAR 46,110", "VP 3,110", "VP 52,110", "WHNP 46,51" };

        String[] dependencies = {
                "[  0,  2]NSUBJ(nsubj,basic) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root,basic) D[3,7](need) G[3,7](need)",
                "[  8,  9]DET(det,basic) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]ADVMOD(advmod,basic) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]AMOD(amod,basic) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]NN(nn,basic) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]DOBJ(dobj,basic) D[35,43](sentence) G[3,7](need)",
                "[ 46, 51]NSUBJ(nsubj,basic) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]RCMOD(rcmod,basic) D[52,60](contains) G[35,43](sentence)",
                "[ 61, 63]QUANTMOD(quantmod,basic) D[61,63](as) G[64,68](many)",
                "[ 64, 68]NUM(num,basic) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]DOBJ(dobj,basic) D[69,81](constituents) G[52,60](contains)",
                "[ 86, 98]CONJ(conj_and,basic) D[86,98](dependencies) G[69,81](constituents)",
                "[102,110]PREP(prep_as,basic) D[102,110](possible) G[52,60](contains)" };

        String[] posMapped = { "POS_PRON", "POS_VERB", "POS_DET", "POS_ADV", "POS_VERB", "POS_NOUN",
                "POS_NOUN", "POS_PUNCT", "POS_DET", "POS_VERB", "POS_ADV", "POS_ADJ", "POS_NOUN",
                "POS_CONJ", "POS_NOUN", "POS_ADP", "POS_ADJ", "POS_PUNCT" };

        String[] posOriginal = { "PRP", "VBP", "DT", "RB", "VBN", "NN", "NN", ",", "WDT", "VBZ",
                "RB", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };

        String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) "
                + "(VBN complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP (WDT which)) "
                + "(S (VP (VBZ contains) (NP (QP (RB as) (JJ many)) (NNS constituents) (CC and) "
                + "(NNS dependencies)) (PP (IN as) (ADJP (JJ possible)))))))) (. .)))";

        String[] unmappedDep = { "gov" };

        assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        assertDependencies(dependencies, select(jcas, Dependency.class));
        assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        assertTagset(POS.class, "ptb", ENGLISH_POS_TAGS, jcas);
        assertTagsetMapping(POS.class, "ptb", ENGLISH_POS_UNMAPPED, jcas);
        assertTagset(Constituent.class, "ptb", ENGLISH_CONSTITUENT_TAGS, jcas);
        assertTagsetMapping(Constituent.class, "ptb", ENGLISH_CONSTITUENT_UNMAPPED, jcas);
        assertTagset(Dependency.class, "stanford341", ENGLISH_DEPENDENCY_TAGS, jcas);
        assertTagsetMapping(Dependency.class, "stanford341", unmappedDep, jcas);
    }

    @Test
    public void testEnglishShiftReduce()
        throws Exception
    {
        JCas jcas = runTestWithPosTagger("en", "sr", "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String[] constituentMapped = { "ADJP 10,26", "ADJP 102,110", "NP 0,2", "NP 64,110",
                "NP 64,98", "NP 8,110", "NP 8,43", "PP 61,110", "PP 99,110", "ROOT 0,112",
                "S 0,112", "S 52,110", "SBAR 46,110", "VP 3,110", "VP 52,110", "WHNP 46,51" };

        String[] constituentOriginal = { "ADJP 10,26", "ADJP 102,110", "NP 0,2", "NP 64,110",
                "NP 64,98", "NP 8,110", "NP 8,43", "PP 61,110", "PP 99,110", "ROOT 0,112",
                "S 0,112", "S 52,110", "SBAR 46,110", "VP 3,110", "VP 52,110", "WHNP 46,51" };

        String[] dependencies = {
                "[  0,  2]NSUBJ(nsubj,basic) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root,basic) D[3,7](need) G[3,7](need)",
                "[  8,  9]DET(det,basic) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]ADVMOD(advmod,basic) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]AMOD(amod,basic) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]NN(nn,basic) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]DOBJ(dobj,basic) D[35,43](sentence) G[3,7](need)",
                "[ 46, 51]NSUBJ(nsubj,basic) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]RCMOD(rcmod,basic) D[52,60](contains) G[35,43](sentence)",
                "[ 64, 68]AMOD(amod,basic) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]PREP(prep_as,basic) D[69,81](constituents) G[52,60](contains)",
                "[ 86, 98]CONJ(conj_and,basic) D[86,98](dependencies) G[69,81](constituents)",
                "[102,110]PREP(prep_as,basic) D[102,110](possible) G[69,81](constituents)" };

        String[] posMapped = { "POS_PRON", "POS_VERB", "POS_DET", "POS_ADV", "POS_ADJ", "POS_NOUN",
                "POS_NOUN", "POS_PUNCT", "POS_DET", "POS_VERB", "POS_ADP", "POS_ADJ", "POS_NOUN",
                "POS_CONJ", "POS_NOUN", "POS_ADP", "POS_ADJ", "POS_PUNCT" };

        String[] posOriginal = { "PRP", "VBP", "DT", "RB", "JJ", "NN", "NN", ",", "WDT", "VBZ",
                "IN", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };

        String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) "
                + "(JJ complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP (WDT which)) "
                + "(S (VP (VBZ contains) (PP (IN as) (NP (NP (JJ many) (NNS constituents) "
                + "(CC and) (NNS dependencies)) (PP (IN as) (ADJP (JJ possible)))))))))) (. .)))";

        String[] unmappedDep = { "gov" };

        assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        assertDependencies(dependencies, select(jcas, Dependency.class));
        assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        assertTagset(CoreNlpPosTagger.class, POS.class, "ptb", ENGLISH_POS_TAGS,
                jcas);
        assertTagsetMapping(CoreNlpPosTagger.class, POS.class, "ptb",
                ENGLISH_POS_UNMAPPED, jcas);
        assertTagset(CoreNlpParser.class, Constituent.class, "ptb",
                ENGLISH_CONSTITUENT_TAGS, jcas);
        assertTagsetMapping(CoreNlpParser.class, Constituent.class, "ptb",
                ENGLISH_CONSTITUENT_UNMAPPED, jcas);
        assertTagset(CoreNlpParser.class, Dependency.class, "stanford341",
                ENGLISH_DEPENDENCY_TAGS, jcas);
        assertTagsetMapping(CoreNlpParser.class, Dependency.class, "stanford341",
                unmappedDep, jcas);
    }

    @Test
    public void testEnglishShiftReduceBeam()
        throws Exception
    {
        JCas jcas = runTestWithPosTagger("en", "sr-beam", "We need a very complicated example "
                + "sentence , which contains as many constituents and dependencies as possible .");

        String[] constituentMapped = { "ADJP 10,26", "ADJP 102,110", "NP 0,2", "NP 64,110",
                "NP 64,98", "NP 8,110", "NP 8,43", "PP 61,110", "PP 99,110", "ROOT 0,112",
                "S 0,112", "S 52,110", "SBAR 46,110", "VP 3,110", "VP 52,110", "WHNP 46,51" };

        String[] constituentOriginal = { "ADJP 10,26", "ADJP 102,110", "NP 0,2", "NP 64,110",
                "NP 64,98", "NP 8,110", "NP 8,43", "PP 61,110", "PP 99,110", "ROOT 0,112",
                "S 0,112", "S 52,110", "SBAR 46,110", "VP 3,110", "VP 52,110", "WHNP 46,51" };

        String[] dependencies = {
                "[  0,  2]NSUBJ(nsubj,basic) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root,basic) D[3,7](need) G[3,7](need)",
                "[  8,  9]DET(det,basic) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]ADVMOD(advmod,basic) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]AMOD(amod,basic) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]NN(nn,basic) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]DOBJ(dobj,basic) D[35,43](sentence) G[3,7](need)",
                "[ 46, 51]NSUBJ(nsubj,basic) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]RCMOD(rcmod,basic) D[52,60](contains) G[35,43](sentence)",
                "[ 64, 68]AMOD(amod,basic) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]PREP(prep_as,basic) D[69,81](constituents) G[52,60](contains)",
                "[ 86, 98]CONJ(conj_and,basic) D[86,98](dependencies) G[69,81](constituents)",
                "[102,110]PREP(prep_as,basic) D[102,110](possible) G[69,81](constituents)" };

        String[] posMapped = { "POS_PRON", "POS_VERB", "POS_DET", "POS_ADV", "POS_ADJ", "POS_NOUN",
                "POS_NOUN", "POS_PUNCT", "POS_DET", "POS_VERB", "POS_ADP", "POS_ADJ", "POS_NOUN",
                "POS_CONJ", "POS_NOUN", "POS_ADP", "POS_ADJ", "POS_PUNCT" };

        String[] posOriginal = { "PRP", "VBP", "DT", "RB", "JJ", "NN", "NN", ",", "WDT", "VBZ",
                "IN", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };

        String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) "
                + "(JJ complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP (WDT which)) "
                + "(S (VP (VBZ contains) (PP (IN as) (NP (NP (JJ many) (NNS constituents) (CC and) "
                + "(NNS dependencies)) (PP (IN as) (ADJP (JJ possible)))))))))) (. .)))";

        String[] unmappedDep = { "gov" };

        assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        assertDependencies(dependencies, select(jcas, Dependency.class));
        assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        assertTagset(POS.class, "ptb", ENGLISH_POS_TAGS, jcas);
        assertTagsetMapping(POS.class, "ptb", ENGLISH_POS_UNMAPPED, jcas);
        assertTagset(Constituent.class, "ptb", ENGLISH_CONSTITUENT_TAGS, jcas);
        assertTagsetMapping(Constituent.class, "ptb", ENGLISH_CONSTITUENT_UNMAPPED, jcas);
        assertTagset(Dependency.class, "stanford341", ENGLISH_DEPENDENCY_TAGS, jcas);
        assertTagsetMapping(Dependency.class, "stanford341", unmappedDep, jcas);
    }

    @Test
    public void testEnglishWsjRnn()
        throws Exception
    {
        JCas jcas = runTest("en", "wsj-rnn", "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String[] constituentMapped = { "ADJP 10,26", "ADJP 102,110", "NP 0,2", "NP 61,98",
                "NP 8,110", "NP 8,43", "PP 99,110", "QP 61,68", "ROOT 0,112", "S 0,112",
                "S 52,110", "SBAR 46,110", "VP 3,110", "VP 52,110", "WHNP 46,51" };

        String[] constituentOriginal = { "ADJP 10,26", "ADJP 102,110", "NP 0,2", "NP 61,98",
                "NP 8,110", "NP 8,43", "PP 99,110", "QP 61,68", "ROOT 0,112", "S 0,112",
                "S 52,110", "SBAR 46,110", "VP 3,110", "VP 52,110", "WHNP 46,51" };

        String[] dependencies = {
                "[  0,  2]NSUBJ(nsubj,basic) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root,basic) D[3,7](need) G[3,7](need)",
                "[  8,  9]DET(det,basic) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]ADVMOD(advmod,basic) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]AMOD(amod,basic) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]NN(nn,basic) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]DOBJ(dobj,basic) D[35,43](sentence) G[3,7](need)",
                "[ 46, 51]NSUBJ(nsubj,basic) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]RCMOD(rcmod,basic) D[52,60](contains) G[35,43](sentence)",
                "[ 61, 63]QUANTMOD(quantmod,basic) D[61,63](as) G[64,68](many)",
                "[ 64, 68]NUM(num,basic) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]DOBJ(dobj,basic) D[69,81](constituents) G[52,60](contains)",
                "[ 86, 98]CONJ(conj_and,basic) D[86,98](dependencies) G[69,81](constituents)",
                "[102,110]PREP(prep_as,basic) D[102,110](possible) G[52,60](contains)" };

        String[] posMapped = { "POS_PRON", "POS_VERB", "POS_DET", "POS_ADV", "POS_VERB", "POS_NOUN",
                "POS_NOUN", "POS_PUNCT", "POS_DET", "POS_VERB", "POS_ADV", "POS_ADJ", "POS_NOUN",
                "POS_CONJ", "POS_NOUN", "POS_ADP", "POS_ADJ", "POS_PUNCT" };

        String[] posOriginal = { "PRP", "VBP", "DT", "RB", "VBN", "NN", "NN", ",", "WDT", "VBZ",
                "RB", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };

        String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) "
                + "(VBN complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP (WDT which)) "
                + "(S (VP (VBZ contains) (NP (QP (RB as) (JJ many)) (NNS constituents) (CC and) "
                + "(NNS dependencies)) (PP (IN as) (ADJP (JJ possible)))))))) (. .)))";

        String[] unmappedDep = { "gov" };

        assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        assertDependencies(dependencies, select(jcas, Dependency.class));
        assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        assertTagset(POS.class, "ptb", ENGLISH_POS_TAGS, jcas);
        assertTagsetMapping(POS.class, "ptb", ENGLISH_POS_UNMAPPED, jcas);
        assertTagset(Constituent.class, "ptb", ENGLISH_CONSTITUENT_TAGS, jcas);
        assertTagsetMapping(Constituent.class, "ptb", ENGLISH_CONSTITUENT_UNMAPPED, jcas);
        assertTagset(Dependency.class, "stanford341", ENGLISH_DEPENDENCY_TAGS, jcas);
        assertTagsetMapping(Dependency.class, "stanford341", unmappedDep, jcas);
    }

    /**
     * This test uses simple double quotes.
     *
     * @throws Exception
     *             if there is an error.
     */
    @Test
    public void testEnglishFactoredDirectSpeech()
        throws Exception
    {
        JCas jcas = runTest("en", "factored",
                "\" It 's cold outside , \" he said , \" and it 's starting to rain . \"");

        String[] posOriginal = new String[] { "``", "PRP", "VBZ", "JJ", "JJ", ",", "''", "PRP",
                "VBD", ",", "``", "CC", "PRP", "VBZ", "VBG", "TO", "NN", ".", "''" };

        String pennTree = "(ROOT (S (`` \") (S (NP (PRP It)) (VP (VBZ 's) (ADJP (JJ cold)) (S "
                + "(ADJP (JJ outside))))) (PRN (, ,) ('' \") (S (NP (PRP he)) (VP (VBD said))) (, "
                + ",) (`` \")) (CC and) (S (NP (PRP it)) (VP (VBZ 's) (VP (VBG starting) (PP "
                + "(TO to) (NP (NN rain)))))) (. .) ('' \")))";

        assertPOS(null, posOriginal, select(jcas, POS.class));
        assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
    }

    /**
     * This test uses UTF-8 quotes as they can be found in the British National Corpus.
     *
     * @throws Exception
     *             if there is an error.
     */
    @Test
    public void testEnglishFactoredDirectSpeech2()
        throws Exception
    {
        // JCas jcas = runTest("en", "factored",
        // "‘Prices are used as a barrier so that the sort of " +
        // "people we don't want go over the road ,’ he said .");
        JCas jcas = runTest("en", "factored", new String[] { "‘", "It", "'s", "cold", "outside",
                ",", "’", "he", "said", ",", "‘", "and", "it", "'s", "starting", "to", "rain", ".",
                "’" });

        String[] posOriginal = new String[] { "``", "PRP", "VBZ", "JJ", "JJ", ",", "''", "PRP",
                "VBD", ",", "``", "CC", "PRP", "VBZ", "VBG", "TO", "NN", ".", "''" };

        String pennTree = "(ROOT (S (`` ‘) (S (NP (PRP It)) (VP (VBZ 's) (ADJP (JJ cold)) (S "
                + "(ADJP (JJ outside))))) (PRN (, ,) ('' ’) (S (NP (PRP he)) (VP (VBD said))) "
                + "(, ,) (`` ‘)) (CC and) (S (NP (PRP it)) (VP (VBZ 's) (VP (VBG starting) (PP "
                + "(TO to) (NP (NN rain)))))) (. .) ('' ’)))";

        assertPOS(null, posOriginal, select(jcas, POS.class));
        assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
    }

    @Test
    public void testSpanishShiftReduceBeam()
        throws Exception
    {
        JCas jcas = runTestWithPosTagger("es", "sr-beam", "Necesitamos una oración de ejemplo "
                + "muy complicado , que contiene la mayor cantidad de componentes y dependencias "
                + "como sea posible .");

        String[] constituentMapped = { "ADJP 122,129", "ADJP 68,73", "ADVP 35,38", "CONJP 113,117",
                "CONJP 98,99", "NP 100,112", "NP 12,129", "NP 16,129", "NP 27,34", "NP 65,112",
                "NP 68,112", "NP 86,112", "NP 86,97", "PP 24,34", "PP 83,112", "ROOT 0,131",
                "S 0,131", "S 113,129", "S 35,49", "S 50,129", "VP 0,11", "VP 118,121", "VP 56,64",
                "X 12,15", "X 24,26", "X 39,49", "X 52,55", "X 65,67", "X 83,85" };

        String[] constituentOriginal = { "ROOT 0,131", "S 113,129", "S 35,49", "S 50,129",
                "conj 113,117", "conj 98,99", "grup.a 122,129", "grup.a 68,73", "grup.adv 35,38",
                "grup.nom 100,112", "grup.nom 16,129", "grup.nom 27,34", "grup.nom 68,112",
                "grup.nom 86,112", "grup.nom 86,97", "grup.verb 0,11", "grup.verb 118,121",
                "grup.verb 56,64", "participi 39,49", "prep 24,26", "prep 83,85", "relatiu 52,55",
                "s.a 122,129", "s.a 68,73", "sadv 35,38", "sentence 0,131", "sn 12,129",
                "sn 27,34", "sn 65,112", "sn 86,112", "sp 24,34", "sp 83,112", "spec 12,15",
                "spec 65,67" };

        String[] dependencies = { };

        String[] posMapped = { "POS_VERB", "POS_DET", "POS_NOUN", "POS_ADP", "POS_NOUN", "POS_ADV", "POS_ADJ", "POS_PUNCT", "POS_PRON",
                "POS_VERB", "POS_DET", "POS_ADJ", "POS_NOUN", "POS_ADP", "POS_NOUN", "POS_CONJ", "POS_NOUN", "POS_CONJ", "POS_VERB", "POS_ADJ",
                "POS_PUNCT" };

        String[] posOriginal = { "vmip000", "di0000", "nc0s000", "sp000", "nc0s000", "rg",
                "aq0000", "fc", "pr000000", "vmip000", "da0000", "aq0000", "nc0s000", "sp000",
                "nc0p000", "cc", "nc0p000", "cs", "vssp000", "aq0000", "fp" };

        String pennTree = "(ROOT (sentence (grup.verb (vmip000 Necesitamos)) (sn (spec "
                + "(di0000 una)) (grup.nom (nc0s000 oración) (sp (prep (sp000 de)) (sn "
                + "(grup.nom (nc0s000 ejemplo)))) (S (sadv (grup.adv (rg muy))) (participi "
                + "(aq0000 complicado))) (S (fc ,) (relatiu (pr000000 que)) (grup.verb "
                + "(vmip000 contiene)) (sn (spec (da0000 la)) (grup.nom (s.a (grup.a "
                + "(aq0000 mayor))) (nc0s000 cantidad) (sp (prep (sp000 de)) (sn (grup.nom "
                + "(grup.nom (nc0p000 componentes)) (conj (cc y)) (grup.nom "
                + "(nc0p000 dependencias))))))) (S (conj (cs como)) (grup.verb (vssp000 sea)) "
                + "(s.a (grup.a (aq0000 posible))))))) (fp .)))";

        String[] posTags = SPANISH_POS_TAGS;

        String[] constituentTags = { "ROOT", "S", "conj", "f", "gerundi", "grup.a", "grup.adv",
                "grup.cc", "grup.cs", "grup.nom", "grup.prep", "grup.pron", "grup.verb", "grup.w",
                "grup.z", "inc", "infinitiu", "interjeccio", "morfema.pronominal", "morfema.verbal",
                "neg", "participi", "prep", "relatiu", "s.a", "sadv", "sentence", "sn", "sp",
                "spec" };

        String[] unmappedPos = { "359000", "NCMS000", "word" };

        String[] unmappedConst = { "f" };

        assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        assertDependencies(dependencies, select(jcas, Dependency.class));
        assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        assertTagset(CoreNlpPosTagger.class, POS.class, "ancora", posTags, jcas);
        assertTagsetMapping(CoreNlpPosTagger.class, POS.class, "ancora",
                unmappedPos, jcas);
        assertTagset(CoreNlpParser.class, Constituent.class, "ancora",
                constituentTags, jcas);
        assertTagsetMapping(CoreNlpParser.class, Constituent.class, "ancora",
                unmappedConst, jcas);
        //        assertTagset(Dependency.class, "stanford341", depTags, jcas);
//        assertTagsetMapping(Dependency.class, "stanford341", unmappedDep, jcas);
    }

    /**
     * Tests the parser reading pre-existing POS tags
     *
     * @throws Exception
     *             if there is an error.
     */
    @Test
    public void testExistingPos()
        throws Exception
    {
        AnalysisEngineDescription engine = createEngineDescription(
                createEngineDescription(CoreNlpPosTagger.class),
                createEngineDescription(CoreNlpParser.class,
                        CoreNlpParser.PARAM_READ_POS, true,
                        CoreNlpParser.PARAM_WRITE_POS, false,
                        CoreNlpParser.PARAM_WRITE_PENN_TREE, true));

        JCas jcas = TestRunner.runTest(engine, "en", "This is a test .");

        String[] posOriginal = new String[] { "DT", "VBZ", "DT", "NN", "." };

        String pennTree = "(ROOT (S (NP (DT This)) (VP (VBZ is) (NP (DT a) (NN test))) (. .)))";
        String pennTreeVariant = "(ROOT (S (NP (DT This)) (VP (VBZ is) (NP-TMP (DT a) (NN test))) (. .)))";

        assertPOS(null, posOriginal, select(jcas, POS.class));

        /* Due to https://github.com/dkpro/dkpro-core/issues/852, the results are instable;
         * if the test fails for the expected output, try the 2nd variant.
         * FIXME: once https://github.com/dkpro/dkpro-core/issues/852 is resolved, the try/catch clause should be removed.*/
        try {
            assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        }
        catch (Throwable e) {
            assertPennTree(pennTreeVariant, selectSingle(jcas, PennTree.class));
        }
    }

    @Test
    public void testFrenchFactored()
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

        JCas jcas = runTest("fr", "factored", "Nous avons besoin d' une phrase par exemple très "
                + "compliqué , qui contient des constituants que de nombreuses dépendances et que "
                + "possible .");

        String[] constituentMapped = { "ADJP 128,136", "ADVP 32,48", "NP 11,48", "NP 21,48",
                "NP 61,64", "NP 74,90", "NP 95,120", "PP 18,48", "ROOT 0,138", "S 0,138",
                "SBAR 124,136", "SBAR 61,90", "SBAR 91,136", "VP 0,58", "VP 65,73", "X 121,136",
                "X 32,43" };

        String[] constituentOriginal = { "AP 128,136", "AdP 32,48", "COORD 121,136", "MWADV 32,43",
                "NP 11,48", "NP 21,48", "NP 61,64", "NP 74,90", "NP 95,120", "PP 18,48",
                "ROOT 0,138", "SENT 0,138", "Srel 61,90", "Ssub 124,136", "Ssub 91,136", "VN 0,58",
                "VN 65,73" };

        String[] dependencies = {/** No dependencies for French */ };

        String[] posMapped = { "POS_PRON", "POS_VERB", "POS_NOUN", "POS_ADP", "POS_DET", "POS_NOUN", "POS_ADP", "POS_NOUN", "POS_ADV",
                "POS_VERB", "POS_PUNCT", "POS_PRON", "POS_VERB", "POS_DET", "POS_NOUN", "POS_CONJ", "POS_DET", "POS_ADJ", "POS_NOUN",
                "POS_CONJ", "POS_CONJ", "POS_ADJ", "POS_PUNCT" };

        String[] posOriginal = { "CLS", "V", "NC", "P", "DET", "NC", "P", "N", "ADV", "VPP",
                "PUNC", "PROREL", "V", "DET", "NC", "CS", "DET", "ADJ", "NC", "CC", "CS", "ADJ",
                "PUNC" };

        String pennTree = "(ROOT (SENT (VN (CLS Nous) (V avons) (NP (NC besoin) (PP (P d') (NP "
                + "(DET une) (NC phrase) (AdP (MWADV (P par) (N exemple)) (ADV très))))) "
                + "(VPP compliqué)) (PUNC ,) (Srel (NP (PROREL qui)) (VN (V contient)) (NP "
                + "(DET des) (NC constituants))) (Ssub (CS que) (NP (DET de) (ADJ nombreuses) "
                + "(NC dépendances)) (COORD (CC et) (Ssub (CS que) (AP (ADJ possible))))) "
                + "(PUNC .)))";

        String[] posTags = FRENCH_POS_TAGS;

        String[] constituentTags = { "AP", "AdP", "COORD", "MWA", "MWADV", "MWC", "MWCL", "MWD",
                "MWET", "MWI", "MWN", "MWP", "MWPRO", "MWV", "NP", "PP", "ROOT", "SENT", "Sint",
                "Srel", "Ssub", "VN", "VPinf", "VPpart" };

        // NO DEP TAGS String[] depTags = {};

        String[] unmappedPos = { };

        String[] unmappedConst = { "MWA", "MWADV", "MWC", "MWCL", "MWD", "MWET",
                "MWI", "MWN", "MWP", "MWPRO", "MWV" };

        // NO DEP TAGS String[] unmappedDep = {};

        assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        assertDependencies(dependencies, select(jcas, Dependency.class));

        assertTagset(POS.class, "corenlp34", posTags, jcas);
        assertTagsetMapping(POS.class, "corenlp34", unmappedPos, jcas);
        assertTagset(Constituent.class, "ftb", constituentTags, jcas);
        assertTagsetMapping(Constituent.class, "ftb", unmappedConst, jcas);
        // NO DEP TAGS assertTagset(Dependency.class, null, depTags, jcas);
        // NO DEP TAGS assertTagsetMapping(Dependency.class, null, unmappedDep, jcas);
    }

    @Test
    public void testFrench2()
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

        JCas jcas = runTest("fr", null, "La traduction d' un texte du français vers l' anglais .");

        String[] constituentMapped = { "ADJP 29,37", "NP 0,53", "NP 17,37", "NP 43,53", "PP 14,37",
                "PP 26,37", "PP 38,53", "ROOT 0,55", "S 0,55" };

        String[] constituentOriginal = { "AP 29,37", "NP 0,53", "NP 17,37", "NP 43,53", "PP 14,37",
                "PP 26,37", "PP 38,53", "ROOT 0,55", "SENT 0,55" };

        String[] posMapped = { "POS_DET", "POS_NOUN", "POS_ADP", "POS_DET", "POS_NOUN", "POS_ADP", "POS_ADJ", "POS_ADP", "POS_DET",
                "POS_NOUN", "POS_PUNCT" };

        String[] posOriginal = { "DET", "NC", "P", "DET", "NC", "P", "ADJ", "P", "DET", "NC",
                "PUNC" };

        String pennTree = "(ROOT (SENT (NP (DET La) (NC traduction) (PP (P d') (NP (DET un) "
                + "(NC texte) (PP (P du) (AP (ADJ français))))) (PP (P vers) (NP (DET l') "
                + "(NC anglais)))) (PUNC .)))";

        assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
    }

    @Test
    public void testChineseFactored()
        throws Exception
    {
        JCas jcas = runTest("zh", "factored",
                "我们 需要 一个 非常 复杂 的 句子 例如 其中 包含 许多 成分 和 尽可能 的 依赖 。");

        String[] constituentMapped = { "ADJP 12,14", "ADJP 9,14", "ADVP 20,22", "ADVP 37,40",
                "ADVP 9,11", "NP 0,2", "NP 17,19", "NP 23,25", "NP 29,34", "NP 32,34", "NP 6,19",
                "QP 29,31", "QP 6,8", "ROOT 0,47", "VP 26,34", "VP 26,45", "VP 3,19", "VP 37,45",
                "VP 43,45", "X 0,19", "X 0,47", "X 20,45", "X 37,42", "X 9,16" };

        String[] constituentOriginal = { "ADJP 12,14", "ADJP 9,14", "ADVP 20,22", "ADVP 37,40",
                "ADVP 9,11", "DNP 9,16", "DVP 37,42", "IP 0,19", "IP 0,47", "IP 20,45", "NP 0,2",
                "NP 17,19", "NP 23,25", "NP 29,34", "NP 32,34", "NP 6,19", "QP 29,31", "QP 6,8",
                "ROOT 0,47", "VP 26,34", "VP 26,45", "VP 3,19", "VP 37,45", "VP 43,45" };

        String[] dependencies = {
                "[  0,  2]NSUBJ(nsubj,basic) D[0,2](我们) G[3,5](需要)",
                "[  3,  5]ROOT(root,basic) D[3,5](需要) G[3,5](需要)",
                "[  6,  8]Dependency(nummod,basic) D[6,8](一个) G[17,19](句子)",
                "[  9, 11]ADVMOD(advmod,basic) D[9,11](非常) G[12,14](复杂)",
                "[ 12, 14]Dependency(assmod,basic) D[12,14](复杂) G[17,19](句子)",
                "[ 15, 16]Dependency(assm,basic) D[15,16](的) G[12,14](复杂)",
                "[ 17, 19]DOBJ(dobj,basic) D[17,19](句子) G[3,5](需要)",
                "[ 20, 22]ADVMOD(advmod,basic) D[20,22](例如) G[26,28](包含)",
                "[ 23, 25]NSUBJ(nsubj,basic) D[23,25](其中) G[26,28](包含)",
                "[ 26, 28]CONJ(conj,basic) D[26,28](包含) G[3,5](需要)",
                "[ 29, 31]Dependency(nummod,basic) D[29,31](许多) G[32,34](成分)",
                "[ 32, 34]DOBJ(dobj,basic) D[32,34](成分) G[26,28](包含)",
                "[ 35, 36]CC(cc,basic) D[35,36](和) G[26,28](包含)",
                "[ 37, 40]Dependency(dvpmod,basic) D[37,40](尽可能) G[43,45](依赖)",
                "[ 41, 42]Dependency(dvpm,basic) D[41,42](的) G[37,40](尽可能)",
                "[ 43, 45]CONJ(conj,basic) D[43,45](依赖) G[26,28](包含)" };

        String[] posMapped = { "POS_PRON", "POS_VERB", "POS_NUM", "POS_ADJ", "POS_ADJ", "POS_PART",
                "POS_NOUN", "POS_ADJ", "POS_NOUN", "POS_VERB", "POS_NUM", "POS_NOUN", "POS_CONJ",
                "POS_ADJ", "POS_PART", "POS_VERB", "POS_PUNCT" };

        String[] posOriginal = { "PN", "VV", "CD", "AD", "JJ", "DEG", "NN", "AD", "NN", "VV", "CD",
                "NN", "CC", "AD", "DEV", "VV", "PU" };

        String pennTree = "(ROOT (IP (IP (NP (PN 我们)) (VP (VV 需要) (NP (QP (CD 一个)) (DNP "
                + "(ADJP (ADVP (AD 非常)) (ADJP (JJ 复杂))) (DEG 的)) (NP (NN 句子))))) (IP (ADVP "
                + "(AD 例如)) (NP (NN 其中)) (VP (VP (VV 包含) (NP (QP (CD 许多)) (NP (NN 成分)))) "
                + "(CC 和) (VP (DVP (ADVP (AD 尽可能)) (DEV 的)) (VP (VV 依赖))))) (PU 。)))";

        String[] posTags = { "AD", "AS", "BA", "CC", "CD", "CS", "DEC", "DEG", "DER",
                "DEV", "DT", "ETC", "FW", "IJ", "JJ", "LB", "LC", "M", "MSP", "NN", "NR", "NT",
                "OD", "ON", "P", "PN", "PU", "SB", "SP", "URL", "VA", "VC", "VE", "VV", "X" };

        String[] constituentTags = { "ADJP", "ADVP", "CLP", "CP", "DFL", "DNP", "DP", "DVP", "FLR",
                "FRAG", "INC", "INTJ", "IP", "LCP", "LST", "NP", "PP", "PRN", "QP", "ROOT", "UCP",
                "VCD", "VCP", "VNV", "VP", "VPT", "VRD", "VSB", "WHPP" };

        // NO DEP TAGS String[] depTags = new String[] {};

        String[] unmappedPos = { "URL" };

        String[] unmappedConst = { "DFL", "FLR", "INC", "WHPP" };

        // NO DEP TAGS String[] unmappedDep = new String[] {};

        assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        assertDependencies(dependencies, select(jcas, Dependency.class));
        assertTagset(POS.class, "ctb", posTags, jcas);
        assertTagsetMapping(POS.class, "ctb", unmappedPos, jcas);
        assertTagset(Constituent.class, "ctb", constituentTags, jcas);
        assertTagsetMapping(Constituent.class, "ctb", unmappedConst, jcas);
        // NO DEP TAGS assertTagset(Dependency.class, null, depTags, jcas);
        // NO DEP TAGS assertTagsetMapping(Dependency.class, null, unmappedDep, jcas);
    }

    @Test
    public void testChineseXinhuaFactored()
        throws Exception
    {
        JCas jcas = runTest("zh", "xinhua-factored",
                "我们 需要 一个 非常 复杂 的 句子 例如 其中 包含 许多 成分 和 尽可能 的 依赖 。");

        String[] constituentMapped = { "ADVP 20,22", "ADVP 37,40", "ADVP 9,11", "NP 0,2",
                "NP 17,19", "NP 23,25", "NP 29,34", "NP 32,34", "NP 43,45", "NP 6,45", "NP 9,19",
                "QP 29,31", "QP 6,8", "ROOT 0,47", "VP 12,14", "VP 26,34", "VP 26,40", "VP 3,45",
                "VP 37,40", "VP 9,14", "X 0,47", "X 20,40", "X 9,14", "X 9,16", "X 9,40",
                "X 9,42" };

        String[] constituentOriginal = { "ADVP 20,22", "ADVP 37,40", "ADVP 9,11", "CP 9,16",
                "CP 9,42", "IP 0,47", "IP 20,40", "IP 9,14", "IP 9,40", "NP 0,2", "NP 17,19",
                "NP 23,25", "NP 29,34", "NP 32,34", "NP 43,45", "NP 6,45", "NP 9,19", "QP 29,31",
                "QP 6,8", "ROOT 0,47", "VP 12,14", "VP 26,34", "VP 26,40", "VP 3,45", "VP 37,40",
                "VP 9,14" };

        String[] dependencies = {
                "[  0,  2]NSUBJ(nsubj,basic) D[0,2](我们) G[3,5](需要)",
                "[  3,  5]ROOT(root,basic) D[3,5](需要) G[3,5](需要)",
                "[  6,  8]Dependency(nummod,basic) D[6,8](一个) G[43,45](依赖)",
                "[  9, 11]ADVMOD(advmod,basic) D[9,11](非常) G[12,14](复杂)",
                "[ 12, 14]RCMOD(rcmod,basic) D[12,14](复杂) G[17,19](句子)",
                "[ 15, 16]Dependency(cpm,basic) D[15,16](的) G[12,14](复杂)",
                "[ 17, 19]NSUBJ(nsubj,basic) D[17,19](句子) G[26,28](包含)",
                "[ 20, 22]ADVMOD(advmod,basic) D[20,22](例如) G[26,28](包含)",
                "[ 23, 25]NSUBJ(nsubj,basic) D[23,25](其中) G[26,28](包含)",
                "[ 26, 28]RCMOD(rcmod,basic) D[26,28](包含) G[43,45](依赖)",
                "[ 29, 31]Dependency(nummod,basic) D[29,31](许多) G[32,34](成分)",
                "[ 32, 34]DOBJ(dobj,basic) D[32,34](成分) G[26,28](包含)",
                "[ 35, 36]CC(cc,basic) D[35,36](和) G[26,28](包含)",
                "[ 37, 40]CONJ(conj,basic) D[37,40](尽可能) G[26,28](包含)",
                "[ 41, 42]Dependency(cpm,basic) D[41,42](的) G[26,28](包含)",
                "[ 43, 45]DOBJ(dobj,basic) D[43,45](依赖) G[3,5](需要)" };

        String[] posMapped = { "POS_PRON", "POS_VERB", "POS_NUM", "POS_ADJ", "POS_VERB", "POS_PART",
                "POS_NOUN", "POS_ADJ", "POS_NOUN", "POS_VERB", "POS_NUM", "POS_NOUN", "POS_CONJ",
                "POS_ADJ", "POS_PART", "POS_NOUN", "POS_PUNCT" };

        String[] posOriginal = { "PN", "VV", "CD", "AD", "VA", "DEC", "NN", "AD", "NN", "VV", "CD",
                "NN", "CC", "AD", "DEC", "NN", "PU" };

        String pennTree = "(ROOT (IP (NP (PN 我们)) (VP (VV 需要) (NP (QP (CD 一个)) (CP (IP (NP "
                + "(CP (IP (VP (ADVP (AD 非常)) (VP (VA 复杂)))) (DEC 的)) (NP (NN 句子))) (IP "
                + "(ADVP (AD 例如)) (NP (NN 其中)) (VP (VP (VV 包含) (NP (QP (CD 许多)) (NP "
                + "(NN 成分)))) (CC 和) (VP (ADVP (AD 尽可能)))))) (DEC 的)) (NP (NN 依赖)))) "
                + "(PU 。)))";

        String[] posTags = { "AD", "AS", "BA", "CC", "CD", "CS", "DEC", "DEG", "DER", "DEV", "DT",
                "ETC", "FW", "JJ", "LB", "LC", "M", "MSP", "NN", "NR", "NT", "OD", "P", "PN", "PU",
                "SB", "SP", "VA", "VC", "VE", "VV" };

        String[] constituentTags = { "ADJP", "ADVP", "CLP", "CP", "DNP", "DP", "DVP", "FRAG", "IP",
                "LCP", "LST", "NP", "PP", "PRN", "QP", "ROOT", "UCP", "VCD", "VCP", "VNV", "VP",
                "VPT", "VRD", "VSB" };

        // NO DEP TAGS String[] depTags = new String[] {};

        String[] unmappedPos = { };

        String[] unmappedConst = { };

        // NO DEP TAGS String[] unmappedDep = new String[] {};

        assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        assertDependencies(dependencies, select(jcas, Dependency.class));
        assertTagset(POS.class, "ctb", posTags, jcas);
        assertTagsetMapping(POS.class, "ctb", unmappedPos, jcas);
        assertTagset(Constituent.class, "ctb", constituentTags, jcas);
        assertTagsetMapping(Constituent.class, "ctb", unmappedConst, jcas);
        // NO DEP TAGS assertTagset(Dependency.class, null, depTags, jcas);
        // NO DEP TAGS assertTagsetMapping(Dependency.class, null, unmappedDep, jcas);
    }

    @Test
    public void testArabicFactored()
        throws Exception
    {
        JCas jcas = runTest("ar", "factored",
                "نحتاج مثالا معقدا جدا ل جملة تحتوي على أكبر قدر ممكن من العناصر و الروابط .");

        String[] constituentMapped = { "NP 24,28", "NP 24,73", "NP 39,73", "NP 44,52", "NP 44,73",
                "NP 56,73", "NP 6,21", "PP 22,73", "PP 35,73", "PP 53,73", "ROOT 0,75", "S 0,75",
                "S 29,73", "SBAR 29,73", "VP 0,73", "VP 29,73" };

        String[] constituentOriginal = { "NP 24,28", "NP 24,73", "NP 39,73", "NP 44,52",
                "NP 44,73", "NP 56,73", "NP 6,21", "PP 22,73", "PP 35,73", "PP 53,73", "ROOT 0,75",
                "S 0,75", "S 29,73", "SBAR 29,73", "VP 0,73", "VP 29,73" };

        String[] dependencies = {};

        String pennTree = "(ROOT (S (VP (VBP نحتاج) (NP (NN مثالا) (JJ معقدا) (NN جدا)) (PP (IN ل) (NP "
                + "(NP (NN جملة)) (SBAR (S (VP (VBP تحتوي) (PP (IN على) (NP (NN أكبر) (NP (NP (NN قدر) "
                + "(JJ ممكن)) (PP (IN من) (NP (DTNN العناصر) (CC و) (DTNN الروابط)))))))))))) (PUNC .)))";

        String[] posMapped = { "POS_VERB", "POS_NOUN", "POS_ADJ", "POS_NOUN", "POS_ADP", "POS_NOUN", "POS_VERB", "POS_ADP", "POS_NOUN",
                "POS_NOUN", "POS_ADJ", "POS_ADP", "POS_NOUN", "POS_CONJ", "POS_NOUN", "POS_PUNCT" };

        String[] posOriginal = { "VBP", "NN", "JJ", "NN", "IN", "NN", "VBP", "IN", "NN", "NN",
                "JJ", "IN", "DTNN", "CC", "DTNN", "PUNC" };

        String[] posTags = { "ADJ_NUM", "CC", "CD", "DT", "DTJJ", "DTJJR", "DTNN", "DTNNP",
                "DTNNPS", "DTNNS", "IN", "JJ", "JJR", "NN", "NNP", "NNPS", "NNS", "NOUN_QUANT",
                "PRP", "PRP$", "PUNC", "RB", "RP", "UH", "VB", "VBD", "VBG", "VBN", "VBP", "VN",
                "WP", "WRB" };

        String[] constituentTags = { "ADJP", "ADVP", "CONJP", "FRAG", "INTJ", "LST", "NAC", "NP",
                "PP", "PRN", "PRT", "ROOT", "S", "SBAR", "SBARQ", "SQ", "UCP", "VP", "WHADVP",
                "WHNP", "WHPP", "X" };

        String[] unmappedPos = { "ADJ_NUM", "NOUN_QUANT", "PRP$" };

        String[] unmappedConst = { "LST" };

        assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        assertDependencies(dependencies, select(jcas, Dependency.class));
        assertTagset(POS.class, "atb", posTags, jcas);
        assertTagsetMapping(POS.class, "atb", unmappedPos, jcas);
        assertTagset(Constituent.class, "atb", constituentTags, jcas);
        assertTagsetMapping(Constituent.class, "atb", unmappedConst, jcas);
    }

    /**
     * This tests whether a complete syntax tree can be recreated from the annotations without any
     * loss. Consequently, all links to children should be correct. (This makes no assertions about
     * the parent-links, because they are not used for the recreation)
     *
     * @throws Exception
     *             if there is an error.
     */
    @Test
    public void testEnglishSyntaxTreeReconstruction()
        throws Exception
    {
        JCas jcas = runTest("en", "factored", "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String pennOriginal = "";
        String pennFromRecreatedTree = "";

        // As we only have one input sentence, each loop only runs once!

        for (PennTree curPenn : select(jcas, PennTree.class)) {
            // get original penn representation of syntax tree
            pennOriginal = curPenn.getPennTree();
        }

        for (ROOT curRoot : select(jcas, ROOT.class)) {
            // recreate syntax tree
            Tree recreation = DKPro2CoreNlp.createStanfordTree(curRoot);

            // make a tree with simple string-labels
            recreation = recreation.deepCopy(recreation.treeFactory(), StringLabel.factory());

            pennFromRecreatedTree = recreation.pennString();
        }

        assertTrue("The recreated syntax-tree did not match the input syntax-tree.",
                pennOriginal.equals(pennFromRecreatedTree));
    }

    private JCas runTestWithPosTagger(String aLanguage, String aVariant, String aText,
            Object... aExtraParams)
        throws Exception
    {
        AssumeResource.assumeResource(CoreNlpParser.class,
                "de/tudarmstadt/ukp/dkpro/core/stanfordnlp", "parser", aLanguage, aVariant);
        
        AggregateBuilder aggregate = new AggregateBuilder();

        aggregate.add(createEngineDescription(CoreNlpPosTagger.class));

        Object[] params = new Object[] {
                CoreNlpParser.PARAM_VARIANT, aVariant,
                CoreNlpParser.PARAM_PRINT_TAGSET, true,
                CoreNlpParser.PARAM_WRITE_CONSTITUENT, true,
                CoreNlpParser.PARAM_WRITE_DEPENDENCY, true,
                CoreNlpParser.PARAM_WRITE_PENN_TREE, true,
                CoreNlpParser.PARAM_WRITE_POS, false};
        params = ArrayUtils.addAll(params, aExtraParams);
        aggregate.add(createEngineDescription(CoreNlpParser.class, params));

        return TestRunner.runTest(aggregate.createAggregateDescription(), aLanguage, aText);
    }

    private JCas runTest(String aLanguage, String aVariant, String aText, Object... aExtraParams)
        throws Exception
    {
        AssumeResource.assumeResource(CoreNlpParser.class,
                "de/tudarmstadt/ukp/dkpro/core/stanfordnlp", "parser", aLanguage, aVariant);
        
        AggregateBuilder aggregate = new AggregateBuilder();

        Object[] params = new Object[] {
                CoreNlpParser.PARAM_VARIANT, aVariant,
                CoreNlpParser.PARAM_PRINT_TAGSET, true,
                CoreNlpParser.PARAM_WRITE_CONSTITUENT, true,
                CoreNlpParser.PARAM_WRITE_DEPENDENCY, true,
                CoreNlpParser.PARAM_WRITE_PENN_TREE, true,
                CoreNlpParser.PARAM_READ_POS, false,
                CoreNlpParser.PARAM_WRITE_POS, true};
        params = ArrayUtils.addAll(params, aExtraParams);
        aggregate.add(createEngineDescription(CoreNlpParser.class, params));

        return TestRunner.runTest(aggregate.createAggregateDescription(), aLanguage, aText);
    }

    private JCas runTest(String aLanguage, String aVariant, String[] aTokens)
        throws Exception
    {
        AssumeResource.assumeResource(CoreNlpParser.class,
                "de/tudarmstadt/ukp/dkpro/core/stanfordnlp", "parser", aLanguage, aVariant);

        // setup English
        AnalysisEngineDescription parser = createEngineDescription(CoreNlpParser.class,
                CoreNlpParser.PARAM_VARIANT, aVariant,
                CoreNlpParser.PARAM_PRINT_TAGSET, true,
                CoreNlpParser.PARAM_WRITE_CONSTITUENT, true,
                CoreNlpParser.PARAM_WRITE_DEPENDENCY, true,
                CoreNlpParser.PARAM_WRITE_PENN_TREE, true,
                CoreNlpParser.PARAM_READ_POS, false,
                CoreNlpParser.PARAM_WRITE_POS, true,
                CoreNlpParser.PARAM_QUOTE_BEGIN, new String[] { "‘" },
                CoreNlpParser.PARAM_QUOTE_END, new String[] { "’" });

        AnalysisEngine engine = createEngine(parser);
        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage(aLanguage);

        JCasBuilder builder = new JCasBuilder(jcas);
        for (String t : aTokens) {
            builder.add(t, Token.class);
            builder.add(" ");
        }
        builder.add(0, Sentence.class);
        builder.close();

        engine.process(jcas);

        return jcas;
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
