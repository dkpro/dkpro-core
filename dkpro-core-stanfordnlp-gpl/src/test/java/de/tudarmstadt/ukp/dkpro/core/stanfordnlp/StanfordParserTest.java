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
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectSingle;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.jcas.JCas;
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
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.TreeUtils;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;
import edu.stanford.nlp.ling.StringLabel;
import edu.stanford.nlp.trees.Tree;

/**
 */
public class StanfordParserTest
{
    private static final String[] GERMAN_POS_TAGS = { "$,", "$.", "$[", ".$$.", "ADJA", "ADJD",
        "ADV", "APPO", "APPR", "APPRART", "APZR", "ART", "CARD", "FM", "ITJ", "KOKOM", "KON",
        "KOUI", "KOUS", "NE", "NN", "PDAT", "PDS", "PIAT", "PIDAT", "PIS", "PPER", "PPOSAT",
        "PPOSS", "PRELAT", "PRELS", "PRF", "PROAV", "PTKA", "PTKANT", "PTKNEG", "PTKVZ",
        "PTKZU", "PWAT", "PWAV", "PWS", "TRUNC", "VAFIN", "VAIMP", "VAINF", "VAPP", "VMFIN",
        "VMINF", "VMPP", "VVFIN", "VVIMP", "VVINF", "VVIZU", "VVPP", "XY" };

    private static final String[] GERMAN_CONSTITUENT_TAGS = { "AA", "AP", "AVP", "CAC", "CAP",
            "CAVP", "CCP", "CH", "CNP", "CO", "CPP", "CS", "CVP", "CVZ", "DL", "ISU", "MPN", "MTA",
            "NM", "NP", "NUR", "PP", "QL", "ROOT", "S", "VP", "VZ" };
    
    private static final String[] ENGLISH_POS_TAGS = { "#", "$", "''", ",", "-LRB-", "-RRB-", ".",
            ".$$.", ":", "CC", "CD", "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN",
            "NNP", "NNPS", "NNS", "PDT", "POS", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM",
            "TO", "UH", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };
    
    private static final String[] ENGLISH_POS_UNMAPPED = { ".$$."};
    
    private static final String[] ENGLISH_CONSTITUENT_TAGS = { "ADJP", "ADVP", "CONJP", "FRAG",
            "INTJ", "LST", "NAC", "NP", "NX", "PP", "PRN", "PRT", "QP", "ROOT", "RRC", "S", "SBAR",
            "SBARQ", "SINV", "SQ", "UCP", "VP", "WHADJP", "WHADVP", "WHNP", "WHPP", "X" };
    
    private static final String[] ENGLISH_CONSTITUENT_UNMAPPED = { };
    
    private static final String[] ENGLISH_DEPENDENCY_TAGS = { "acomp", "advcl", "advmod", "agent",
            "amod", "appos", "arg", "aux", "auxpass", "cc", "ccomp", "comp", "conj", "cop",
            "csubj", "csubjpass", "dep", "det", "discourse", "dobj", "expl", "goeswith", "gov",
            "iobj", "mark", "mod", "mwe", "neg", "nn", "npadvmod", "nsubj", "nsubjpass", "num",
            "number", "obj", "parataxis", "pcomp", "pobj", "poss", "possessive", "preconj", "pred",
            "predet", "prep", "prt", "punct", "quantmod", "rcmod", "ref", "rel", "sdep", "subj",
            "tmod", "vmod", "xcomp" };
    
    private static final String[] SPANISH_POS_TAGS = { ".$$.", "359000", "ao0000", "aq0000", "cc",
            "cs", "da0000", "dd0000", "de0000", "di0000", "dn0000", "dp0000", "dt0000", "f0",
            "faa", "fat", "fc", "fd", "fe", "fg", "fh", "fia", "fit", "fp", "fpa", "fpt", "fs",
            "ft", "fx", "fz", "i", "nc00000", "nc0n000", "nc0p000", "nc0s000", "np00000",
            "p0000000", "pd000000", "pe000000", "pi000000", "pn000000", "pp000000", "pr000000",
            "pt000000", "px000000", "rg", "rn", "sp000", "vag0000", "vaic000", "vaif000",
            "vaii000", "vaip000", "vais000", "vam0000", "van0000", "vap0000", "vasi000", "vasp000",
            "vmg0000", "vmic000", "vmif000", "vmii000", "vmip000", "vmis000", "vmm0000", "vmn0000",
            "vmp0000", "vmsi000", "vmsp000", "vsg0000", "vsic000", "vsif000", "vsii000", "vsip000",
            "vsis000", "vsm0000", "vsn0000", "vsp0000", "vssf000", "vssi000", "vssp000", "w", "z0",
            "zm", "zu" };
    
    private static final String[] FRENCH_POS_TAGS = { ".$$.", "A", "ADJ", "ADJWH", "ADV", "ADVWH",
            "C", "CC", "CL", "CLO", "CLR", "CLS", "CS", "DET", "DETWH", "ET", "I", "N", "NC",
            "NPP", "P", "PREF", "PRO", "PROREL", "PROWH", "PUNC", "V", "VIMP", "VINF", "VPP",
            "VPR", "VS" };
    
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

        String[] posMapped = { "PRON", "VERB", "DET", "ADV", "ADJ", "NOUN", "PUNCT", "PRON", "ADV",
                "PRON", "NOUN", "CONJ", "NOUN", "VERB", "PUNCT" };

        String[] dependencies = {/** No dependencies for German */ };

        String pennTree = "(ROOT (S (PPER Wir) (VVFIN brauchen) (NP (ART ein) (AP (ADV sehr) "
                + "(ADJA kompliziertes)) (NN Beispiel) ($, ,) (S (PRELS welches) (NP "
                + "(ADV möglichst) (PIDAT viele) (CNP (NN Konstituenten) (KON und) "
                + "(NN Dependenzen))) (VVFIN beinhaltet))) ($. .)))";

        String[] unmappedPos = { "$[", ".$$." };

        String[] unmappedConst = { "NUR" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertSyntacticFunction(synFunc, select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "stts", GERMAN_POS_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "stts", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "negra", GERMAN_CONSTITUENT_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "negra", unmappedConst, jcas);
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

        String[] posMapped = { "PRON", "VERB", "DET", "ADV", "ADJ", "NOUN", "PUNCT", "PRON", "ADV",
                "PRON", "NOUN", "CONJ", "NOUN", "VERB", "PUNCT" };

        String[] dependencies = { /** No dependencies for German */ };

        String pennTree = "(ROOT (S (PPER Wir) (VVFIN brauchen) (NP (ART ein) (AP "
                + "(ADV sehr) (ADJA kompliziertes)) (NN Beispiel) ($, ,) (S (PRELS welches) "
                + "(NP (AP (ADV möglichst) (PIDAT viele)) (CNP (NN Konstituenten) (KON und) "
                + "(NN Dependenzen))) (VVFIN beinhaltet))) ($. .)))";

        String[] unmappedPos = { "$[", ".$$." };

        String[] unmappedConst = { "NUR" };

        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "stts", GERMAN_POS_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "stts", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "negra", GERMAN_CONSTITUENT_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "negra", unmappedConst, jcas, true);
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
                "[  0,  2]NSUBJ(nsubj) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root) D[3,7](need) G[3,7](need)",
                "[  8,  9]DET(det) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]ADVMOD(advmod) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]AMOD(amod) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]NN(nn) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]DOBJ(dobj) D[35,43](sentence) G[3,7](need)",
                "[ 46, 51]NSUBJ(nsubj) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]RCMOD(rcmod) D[52,60](contains) G[35,43](sentence)",
                "[ 64, 68]AMOD(amod) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]PREP(prep_as) D[69,81](constituents) G[52,60](contains)",
                "[ 86, 98]CONJ(conj_and) D[86,98](dependencies) G[69,81](constituents)",
                "[102,110]PREP(prep_as) D[102,110](possible) G[52,60](contains)" };

        String[] posMapped = { "PRON", "VERB", "DET", "ADV", "VERB", "NOUN", "NOUN", "PUNCT", "DET",
                "VERB", "ADP", "ADJ", "NOUN", "CONJ", "NOUN", "ADP", "ADJ", "PUNCT" };

        String[] posOriginal = { "PRP", "VBP", "DT", "RB", "VBN", "NN", "NN", ",", "WDT", "VBZ",
                "IN", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };

        String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) "
                + "(VBN complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP (WDT which)) "
                + "(S (VP (VBZ contains) (PP (IN as) (NP (JJ many) (NNS constituents) (CC and) "
                + "(NNS dependencies))) (PP (IN as) (ADJP (JJ possible)))))))) (. .)))";

        String[] unmappedDep = { "gov" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", ENGLISH_POS_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", ENGLISH_POS_UNMAPPED, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "ptb", ENGLISH_CONSTITUENT_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "ptb", ENGLISH_CONSTITUENT_UNMAPPED, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "stanford341", ENGLISH_DEPENDENCY_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(Dependency.class, "stanford341", unmappedDep, jcas);
    }

    @Test
    public void testEnglishPcfgCollapsed()
        throws Exception
    {
        JCas jcas = runTest("en", "pcfg", "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .", 
                StanfordParser.PARAM_MODE, StanfordParser.DependenciesMode.COLLAPSED_WITH_EXTRA);

        String[] constituentMapped = { "ADJP 10,26", "ADJP 102,110", "NP 0,2", "NP 64,98",
                "NP 8,110", "NP 8,43", "PP 61,98", "PP 99,110", "ROOT 0,112", "S 0,112",
                "S 52,110", "SBAR 46,110", "VP 3,110", "VP 52,110", "WHNP 46,51" };

        String[] constituentOriginal = { "ADJP 10,26", "ADJP 102,110", "NP 0,2", "NP 64,98",
                "NP 8,110", "NP 8,43", "PP 61,98", "PP 99,110", "ROOT 0,112", "S 0,112",
                "S 52,110", "SBAR 46,110", "VP 3,110", "VP 52,110", "WHNP 46,51" };

        String[] dependencies = {
                "[  0,  2]NSUBJ(nsubj) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root) D[3,7](need) G[3,7](need)",
                "[  8,  9]DET(det) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]ADVMOD(advmod) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]AMOD(amod) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]NN(nn) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]DOBJ(dobj) D[35,43](sentence) G[3,7](need)",
                "[ 35, 43]NSUBJ(nsubj) D[35,43](sentence) G[52,60](contains)",
                "[ 52, 60]RCMOD(rcmod) D[52,60](contains) G[35,43](sentence)",
                "[ 64, 68]AMOD(amod) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]PREP(prep_as) D[69,81](constituents) G[52,60](contains)",
                "[ 86, 98]CONJ(conj_and) D[86,98](dependencies) G[69,81](constituents)",
                "[102,110]PREP(prep_as) D[102,110](possible) G[52,60](contains)" };

        String[] posMapped = { "PRON", "VERB", "DET", "ADV", "VERB", "NOUN", "NOUN", "PUNCT", "DET",
                "VERB", "ADP", "ADJ", "NOUN", "CONJ", "NOUN", "ADP", "ADJ", "PUNCT" };

        String[] posOriginal = { "PRP", "VBP", "DT", "RB", "VBN", "NN", "NN", ",", "WDT", "VBZ",
                "IN", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };

        String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) "
                + "(VBN complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP (WDT which)) "
                + "(S (VP (VBZ contains) (PP (IN as) (NP (JJ many) (NNS constituents) (CC and) "
                + "(NNS dependencies))) (PP (IN as) (ADJP (JJ possible)))))))) (. .)))";

        String[] unmappedDep = { "gov" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", ENGLISH_POS_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", ENGLISH_POS_UNMAPPED, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "ptb", ENGLISH_CONSTITUENT_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "ptb", ENGLISH_CONSTITUENT_UNMAPPED, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "stanford341", ENGLISH_DEPENDENCY_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(Dependency.class, "stanford341", unmappedDep, jcas);
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
                "[  0,  2]NSUBJ(nsubj) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root) D[3,7](need) G[3,7](need)",
                "[  8,  9]DET(det) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]ADVMOD(advmod) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]AMOD(amod) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]NN(nn) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]DOBJ(dobj) D[35,43](sentence) G[3,7](need)",
                "[ 46, 51]NSUBJ(nsubj) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]RCMOD(rcmod) D[52,60](contains) G[35,43](sentence)",
                "[ 61, 63]ADVMOD(advmod) D[61,63](as) G[64,68](many)",
                "[ 64, 68]AMOD(amod) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]DOBJ(dobj) D[69,81](constituents) G[52,60](contains)",
                "[ 86, 98]CONJ(conj_and) D[86,98](dependencies) G[69,81](constituents)",
                "[102,110]PREP(prep_as) D[102,110](possible) G[52,60](contains)" };

        String[] posMapped = { "PRON", "VERB", "DET", "ADV", "VERB", "NOUN", "NOUN", "PUNCT", "DET",
                "VERB", "ADV", "ADJ", "NOUN", "CONJ", "NOUN", "ADP", "ADJ", "PUNCT" };

        String[] posOriginal = { "PRP", "VBP", "DT", "RB", "VBN", "NN", "NN", ",", "WDT", "VBZ",
                "RB", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };

        String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) "
                + "(VBN complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP (WDT which)) "
                + "(S (VP (VBZ contains) (NP (ADJP (RB as) (JJ many)) (NNS constituents) (CC and) "
                + "(NNS dependencies)) (PP (IN as) (ADJP (JJ possible)))))))) (. .)))";

        String[] unmappedDep = { "gov" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", ENGLISH_POS_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", ENGLISH_POS_UNMAPPED, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "ptb", ENGLISH_CONSTITUENT_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "ptb", ENGLISH_CONSTITUENT_UNMAPPED, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "stanford341", ENGLISH_DEPENDENCY_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(Dependency.class, "stanford341", unmappedDep, jcas);
    }

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
                "[  0,  2]NSUBJ(nsubj) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root) D[3,7](need) G[3,7](need)",
                "[  8,  9]DET(det) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]ADVMOD(advmod) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]AMOD(amod) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]NN(nn) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]DOBJ(dobj) D[35,43](sentence) G[3,7](need)",
                "[ 46, 51]NSUBJ(nsubj) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]RCMOD(rcmod) D[52,60](contains) G[35,43](sentence)",
                "[ 61, 63]QUANTMOD(quantmod) D[61,63](as) G[64,68](many)",
                "[ 64, 68]NUM(num) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]DOBJ(dobj) D[69,81](constituents) G[52,60](contains)",
                "[ 86, 98]CONJ(conj_and) D[86,98](dependencies) G[69,81](constituents)",
                "[102,110]PREP(prep_as) D[102,110](possible) G[52,60](contains)" };

        String[] posMapped = { "PRON", "VERB", "DET", "ADV", "VERB", "NOUN", "NOUN", "PUNCT", "DET",
                "VERB", "ADV", "ADJ", "NOUN", "CONJ", "NOUN", "ADP", "ADJ", "PUNCT" };

        String[] posOriginal = { "PRP", "VBP", "DT", "RB", "VBN", "NN", "NN", ",", "WDT", "VBZ",
                "RB", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };

        String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) "
                + "(VBN complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP (WDT which)) "
                + "(S (VP (VBZ contains) (NP (QP (RB as) (JJ many)) (NNS constituents) (CC and) "
                + "(NNS dependencies)) (PP (IN as) (ADJP (JJ possible)))))))) (. .)))";

        String[] unmappedDep = { "gov" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", ENGLISH_POS_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", ENGLISH_POS_UNMAPPED, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "ptb", ENGLISH_CONSTITUENT_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "ptb", ENGLISH_CONSTITUENT_UNMAPPED, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "stanford341", ENGLISH_DEPENDENCY_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(Dependency.class, "stanford341", unmappedDep, jcas);
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
                "[  0,  2]NSUBJ(nsubj) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root) D[3,7](need) G[3,7](need)",
                "[  8,  9]DET(det) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]ADVMOD(advmod) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]AMOD(amod) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]NN(nn) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]DOBJ(dobj) D[35,43](sentence) G[3,7](need)",
                "[ 46, 51]NSUBJ(nsubj) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]RCMOD(rcmod) D[52,60](contains) G[35,43](sentence)",
                "[ 64, 68]AMOD(amod) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]PREP(prep_as) D[69,81](constituents) G[52,60](contains)",
                "[ 86, 98]CONJ(conj_and) D[86,98](dependencies) G[69,81](constituents)",
                "[102,110]PREP(prep_as) D[102,110](possible) G[69,81](constituents)" };

        String[] posMapped = { "PRON", "VERB", "DET", "ADV", "ADJ", "NOUN", "NOUN", "PUNCT", "DET",
                "VERB", "ADP", "ADJ", "NOUN", "CONJ", "NOUN", "ADP", "ADJ", "PUNCT" };

        String[] posOriginal = { "PRP", "VBP", "DT", "RB", "JJ", "NN", "NN", ",", "WDT", "VBZ",
                "IN", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };

        String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) "
                + "(JJ complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP (WDT which)) "
                + "(S (VP (VBZ contains) (PP (IN as) (NP (NP (JJ many) (NNS constituents) "
                + "(CC and) (NNS dependencies)) (PP (IN as) (ADJP (JJ possible)))))))))) (. .)))";

        String[] unmappedDep = { "gov" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", ENGLISH_POS_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", ENGLISH_POS_UNMAPPED, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "ptb", ENGLISH_CONSTITUENT_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "ptb", ENGLISH_CONSTITUENT_UNMAPPED, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "stanford341", ENGLISH_DEPENDENCY_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(Dependency.class, "stanford341", unmappedDep, jcas);
    }

    @Test
    public void testEnglishShiftReduceBeam()
        throws Exception
    {
        JCas jcas = runTestWithPosTagger("en", "sr-beam", "We need a very complicated example "
                + "sentence , which contains as many constituents and dependencies as possible .");

        String[] constituentMapped = { "ADJP 10,26", "ADJP 102,110", "NP 0,2", "NP 64,98",
                "NP 8,110", "NP 8,43", "PP 61,110", "PP 61,98", "PP 99,110", "ROOT 0,112",
                "S 0,112", "S 52,110", "SBAR 46,110", "VP 3,110", "VP 52,110", "WHNP 46,51" };

        String[] constituentOriginal = { "ADJP 10,26", "ADJP 102,110", "NP 0,2", "NP 64,98",
                "NP 8,110", "NP 8,43", "PP 61,110", "PP 61,98", "PP 99,110", "ROOT 0,112",
                "S 0,112", "S 52,110", "SBAR 46,110", "VP 3,110", "VP 52,110", "WHNP 46,51" };

        String[] dependencies = { 
                "[  0,  2]NSUBJ(nsubj) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root) D[3,7](need) G[3,7](need)",
                "[  8,  9]DET(det) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]ADVMOD(advmod) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]AMOD(amod) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]NN(nn) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]DOBJ(dobj) D[35,43](sentence) G[3,7](need)",
                "[ 46, 51]NSUBJ(nsubj) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]RCMOD(rcmod) D[52,60](contains) G[35,43](sentence)",
                "[ 64, 68]AMOD(amod) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]PREP(prep_as) D[69,81](constituents) G[52,60](contains)",
                "[ 86, 98]CONJ(conj_and) D[86,98](dependencies) G[69,81](constituents)",
                "[102,110]PREP(prep_as) D[102,110](possible) G[52,60](contains)" };

        String[] posMapped = { "PRON", "VERB", "DET", "ADV", "ADJ", "NOUN", "NOUN", "PUNCT", "DET",
                "VERB", "ADP", "ADJ", "NOUN", "CONJ", "NOUN", "ADP", "ADJ", "PUNCT" };

        String[] posOriginal = { "PRP", "VBP", "DT", "RB", "JJ", "NN", "NN", ",", "WDT", "VBZ",
                "IN", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };

        String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) "
                + "(JJ complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP (WDT which)) "
                + "(S (VP (VBZ contains) (PP (PP (IN as) (NP (JJ many) (NNS constituents) "
                + "(CC and) (NNS dependencies))) (PP (IN as) (ADJP (JJ possible))))))))) (. .)))";

        String[] unmappedDep = { "gov" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", ENGLISH_POS_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", ENGLISH_POS_UNMAPPED, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "ptb", ENGLISH_CONSTITUENT_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "ptb", ENGLISH_CONSTITUENT_UNMAPPED, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "stanford341", ENGLISH_DEPENDENCY_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(Dependency.class, "stanford341", unmappedDep, jcas);
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
                "[  0,  2]NSUBJ(nsubj) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root) D[3,7](need) G[3,7](need)",
                "[  8,  9]DET(det) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]ADVMOD(advmod) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]AMOD(amod) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]NN(nn) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]DOBJ(dobj) D[35,43](sentence) G[3,7](need)",
                "[ 46, 51]NSUBJ(nsubj) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]RCMOD(rcmod) D[52,60](contains) G[35,43](sentence)",
                "[ 61, 63]QUANTMOD(quantmod) D[61,63](as) G[64,68](many)",
                "[ 64, 68]NUM(num) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]DOBJ(dobj) D[69,81](constituents) G[52,60](contains)",
                "[ 86, 98]CONJ(conj_and) D[86,98](dependencies) G[69,81](constituents)",
                "[102,110]PREP(prep_as) D[102,110](possible) G[52,60](contains)"};

        String[] posMapped = { "PRON", "VERB", "DET", "ADV", "VERB", "NOUN", "NOUN", "PUNCT", "DET",
                "VERB", "ADV", "ADJ", "NOUN", "CONJ", "NOUN", "ADP", "ADJ", "PUNCT" };

        String[] posOriginal = { "PRP", "VBP", "DT", "RB", "VBN", "NN", "NN", ",", "WDT", "VBZ",
                "RB", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };

        String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) "
                + "(VBN complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP (WDT which)) "
                + "(S (VP (VBZ contains) (NP (QP (RB as) (JJ many)) (NNS constituents) (CC and) "
                + "(NNS dependencies)) (PP (IN as) (ADJP (JJ possible)))))))) (. .)))";

        String[] unmappedDep = { "gov" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", ENGLISH_POS_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", ENGLISH_POS_UNMAPPED, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "ptb", ENGLISH_CONSTITUENT_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "ptb", ENGLISH_CONSTITUENT_UNMAPPED, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "stanford341", ENGLISH_DEPENDENCY_TAGS, jcas);
        AssertAnnotations.assertTagsetMapping(Dependency.class, "stanford341", unmappedDep, jcas);
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

        AssertAnnotations.assertPOS(null, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
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

        AssertAnnotations.assertPOS(null, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
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

        String[] posMapped = { "VERB", "DET", "NOUN", "ADP", "NOUN", "ADV", "ADJ", "PUNCT", "PRON",
                "VERB", "DET", "ADJ", "NOUN", "ADP", "NOUN", "CONJ", "NOUN", "CONJ", "VERB", "ADJ",
                "PUNCT" };

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

        String[] constituentTags = { "ROOT", "S", "conj", "gerundi", "grup.a", "grup.adv",
                "grup.cc", "grup.cs", "grup.nom", "grup.prep", "grup.pron", "grup.verb", "grup.w",
                "grup.z", "inc", "infinitiu", "interjeccio", "morfema.pronominal",
                "morfema.verbal", "neg", "participi", "prep", "relatiu", "s.a", "sadv", "sentence",
                "sn", "sp", "spec" };

        String[] unmappedPos = { ".$$.", "359000" };

        String[] unmappedConst = {};

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertTagset(POS.class, "ancora", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ancora", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "ancora", constituentTags, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "ancora", unmappedConst, jcas);
//        AssertAnnotations.assertTagset(Dependency.class, "stanford341", depTags, jcas);
//        AssertAnnotations.assertTagsetMapping(Dependency.class, "stanford341", unmappedDep, jcas);
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
                createEngineDescription(StanfordPosTagger.class),
                createEngineDescription(StanfordParser.class,
                        StanfordParser.PARAM_READ_POS, true,
                        StanfordParser.PARAM_WRITE_POS, false,
                        StanfordParser.PARAM_WRITE_PENN_TREE, true));
        
        JCas jcas = TestRunner.runTest(engine, "en", "This is a test .");
        
        String[] posOriginal = new String[] { "DT", "VBZ", "DT", "NN", "." };

        String pennTree = "(ROOT (S (NP (DT This)) (VP (VBZ is) (NP (DT a) (NN test))) (. .)))";

        AssertAnnotations.assertPOS(null, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
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

        String[] posMapped = { "PRON", "VERB", "NOUN", "ADP", "DET", "NOUN", "ADP", "NOUN", "ADV",
                "VERB", "PUNCT", "PRON", "VERB", "DET", "NOUN", "CONJ", "DET", "ADJ", "NOUN",
                "CONJ", "CONJ", "ADJ", "PUNCT" };

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

        String[] unmappedPos = { ".$$." };

        String[] unmappedConst = { "MWA", "MWADV", "MWC", "MWCL", "MWD", "MWET",
                "MWI", "MWN", "MWP", "MWPRO", "MWV" };

        // NO DEP TAGS String[] unmappedDep = {};

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));

        AssertAnnotations.assertTagset(POS.class, "corenlp34", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "corenlp34", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "ftb", constituentTags, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "ftb", unmappedConst, jcas);
        // NO DEP TAGS AssertAnnotations.assertTagset(Dependency.class, null, depTags, jcas);
        // NO DEP TAGS AssertAnnotations.assertTagsetMapping(Dependency.class, null, unmappedDep, jcas);
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

        String[] posMapped = { "DET", "NOUN", "ADP", "DET", "NOUN", "ADP", "ADJ", "ADP", "DET",
                "NOUN", "PUNCT" };

        String[] posOriginal = { "DET", "NC", "P", "DET", "NC", "P", "ADJ", "P", "DET", "NC",
                "PUNC" };

        String pennTree = "(ROOT (SENT (NP (DET La) (NC traduction) (PP (P d') (NP (DET un) "
                + "(NC texte) (PP (P du) (AP (ADJ français))))) (PP (P vers) (NP (DET l') "
                + "(NC anglais)))) (PUNC .)))";

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
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
                "[  0,  2]NSUBJ(nsubj) D[0,2](我们) G[3,5](需要)",
                "[  3,  5]ROOT(root) D[3,5](需要) G[3,5](需要)",
                "[  6,  8]Dependency(nummod) D[6,8](一个) G[17,19](句子)",
                "[  9, 11]ADVMOD(advmod) D[9,11](非常) G[12,14](复杂)",
                "[ 12, 14]Dependency(assmod) D[12,14](复杂) G[17,19](句子)",
                "[ 15, 16]Dependency(case) D[15,16](的) G[12,14](复杂)",
                "[ 17, 19]DOBJ(dobj) D[17,19](句子) G[3,5](需要)",
                "[ 20, 22]ADVMOD(advmod) D[20,22](例如) G[26,28](包含)",
                "[ 23, 25]NSUBJ(nsubj) D[23,25](其中) G[26,28](包含)",
                "[ 26, 28]CONJ(conj) D[26,28](包含) G[3,5](需要)",
                "[ 29, 31]Dependency(nummod) D[29,31](许多) G[32,34](成分)",
                "[ 32, 34]DOBJ(dobj) D[32,34](成分) G[26,28](包含)",
                "[ 35, 36]CC(cc) D[35,36](和) G[26,28](包含)",
                "[ 37, 40]Dependency(dvpmod) D[37,40](尽可能) G[43,45](依赖)",
                "[ 41, 42]MARK(mark) D[41,42](的) G[37,40](尽可能)",
                "[ 43, 45]CONJ(conj) D[43,45](依赖) G[26,28](包含)" };

        String[] posMapped = { "PRON", "VERB", "NUM", "ADJ", "ADJ", "PART", "NOUN", "ADJ", "NOUN",
                "VERB", "NUM", "NOUN", "CONJ", "ADJ", "PART", "VERB", "PUNCT" };

        String[] posOriginal = { "PN", "VV", "CD", "AD", "JJ", "DEG", "NN", "AD", "NN", "VV", "CD",
                "NN", "CC", "AD", "DEV", "VV", "PU" };

        String pennTree = "(ROOT (IP (IP (NP (PN 我们)) (VP (VV 需要) (NP (QP (CD 一个)) (DNP "
                + "(ADJP (ADVP (AD 非常)) (ADJP (JJ 复杂))) (DEG 的)) (NP (NN 句子))))) (IP (ADVP "
                + "(AD 例如)) (NP (NN 其中)) (VP (VP (VV 包含) (NP (QP (CD 许多)) (NP (NN 成分)))) "
                + "(CC 和) (VP (DVP (ADVP (AD 尽可能)) (DEV 的)) (VP (VV 依赖))))) (PU 。)))";

        String[] posTags = { ".$$.", "AD", "AS", "BA", "CC", "CD", "CS", "DEC", "DEG", "DER",
                "DEV", "DT", "ETC", "FW", "IJ", "JJ", "LB", "LC", "M", "MSP", "NN", "NR", "NT",
                "OD", "ON", "P", "PN", "PU", "SB", "SP", "URL", "VA", "VC", "VE", "VV", "X" };

        String[] constituentTags = { "ADJP", "ADVP", "CLP", "CP", "DFL", "DNP", "DP", "DVP", "FLR",
                "FRAG", "INC", "INTJ", "IP", "LCP", "LST", "NP", "PP", "PRN", "QP", "ROOT", "UCP",
                "VCD", "VCP", "VNV", "VP", "VPT", "VRD", "VSB", "WHPP" };

        // NO DEP TAGS String[] depTags = new String[] {};

        String[] unmappedPos = { ".$$.", "URL" };

        String[] unmappedConst = { "DFL", "FLR", "INC", "WHPP" };

        // NO DEP TAGS String[] unmappedDep = new String[] {};

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "ctb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ctb", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "ctb", constituentTags, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "ctb", unmappedConst, jcas);
        // NO DEP TAGS AssertAnnotations.assertTagset(Dependency.class, null, depTags, jcas);
        // NO DEP TAGS AssertAnnotations.assertTagsetMapping(Dependency.class, null, unmappedDep, jcas);
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
                "VP 37,40", "VP 9,14", "X 0,47", "X 20,40", "X 9,14", "X 9,16", "X 9,40", "X 9,42" };

        String[] constituentOriginal = { "ADVP 20,22", "ADVP 37,40", "ADVP 9,11", "CP 9,16",
                "CP 9,42", "IP 0,47", "IP 20,40", "IP 9,14", "IP 9,40", "NP 0,2", "NP 17,19",
                "NP 23,25", "NP 29,34", "NP 32,34", "NP 43,45", "NP 6,45", "NP 9,19", "QP 29,31",
                "QP 6,8", "ROOT 0,47", "VP 12,14", "VP 26,34", "VP 26,40", "VP 3,45", "VP 37,40",
                "VP 9,14" };

        String[] dependencies = {
                "[  0,  2]NSUBJ(nsubj) D[0,2](我们) G[3,5](需要)",
                "[  3,  5]ROOT(root) D[3,5](需要) G[3,5](需要)",
                "[  6,  8]Dependency(nummod) D[6,8](一个) G[43,45](依赖)",
                "[  9, 11]ADVMOD(advmod) D[9,11](非常) G[12,14](复杂)",
                "[ 12, 14]Dependency(relcl) D[12,14](复杂) G[17,19](句子)",
                "[ 15, 16]MARK(mark) D[15,16](的) G[12,14](复杂)",
                "[ 17, 19]NSUBJ(nsubj) D[17,19](句子) G[26,28](包含)",
                "[ 20, 22]ADVMOD(advmod) D[20,22](例如) G[26,28](包含)",
                "[ 23, 25]NSUBJ(nsubj) D[23,25](其中) G[26,28](包含)",
                "[ 26, 28]Dependency(relcl) D[26,28](包含) G[43,45](依赖)",
                "[ 29, 31]Dependency(nummod) D[29,31](许多) G[32,34](成分)",
                "[ 32, 34]DOBJ(dobj) D[32,34](成分) G[26,28](包含)",
                "[ 35, 36]CC(cc) D[35,36](和) G[26,28](包含)",
                "[ 37, 40]CONJ(conj) D[37,40](尽可能) G[26,28](包含)",
                "[ 41, 42]MARK(mark) D[41,42](的) G[26,28](包含)",
                "[ 43, 45]DOBJ(dobj) D[43,45](依赖) G[3,5](需要)" };

        String[] posMapped = { "PRON", "VERB", "NUM", "ADJ", "VERB", "PART", "NOUN", "ADJ", "NOUN",
                "VERB", "NUM", "NOUN", "CONJ", "ADJ", "PART", "NOUN", "PUNCT" };

        String[] posOriginal = { "PN", "VV", "CD", "AD", "VA", "DEC", "NN", "AD", "NN", "VV", "CD",
                "NN", "CC", "AD", "DEC", "NN", "PU" };

        String pennTree = "(ROOT (IP (NP (PN 我们)) (VP (VV 需要) (NP (QP (CD 一个)) (CP (IP (NP "
                + "(CP (IP (VP (ADVP (AD 非常)) (VP (VA 复杂)))) (DEC 的)) (NP (NN 句子))) (IP "
                + "(ADVP (AD 例如)) (NP (NN 其中)) (VP (VP (VV 包含) (NP (QP (CD 许多)) (NP "
                + "(NN 成分)))) (CC 和) (VP (ADVP (AD 尽可能)))))) (DEC 的)) (NP (NN 依赖)))) "
                + "(PU 。)))";

        String[] posTags = { ".$$.", "AD", "AS", "BA", "CC", "CD", "CS", "DEC", "DEG", "DER",
                "DEV", "DT", "ETC", "FW", "JJ", "LB", "LC", "M", "MSP", "NN", "NR", "NT", "OD",
                "P", "PN", "PU", "SB", "SP", "VA", "VC", "VE", "VV" };

        String[] constituentTags = { "ADJP", "ADVP", "CLP", "CP", "DNP", "DP", "DVP", "FRAG", "IP",
                "LCP", "LST", "NP", "PP", "PRN", "QP", "ROOT", "UCP", "VCD", "VCP", "VNV", "VP",
                "VPT", "VRD", "VSB" };

        // NO DEP TAGS String[] depTags = new String[] {};

        String[] unmappedPos = { ".$$." };

        String[] unmappedConst = { };

        // NO DEP TAGS String[] unmappedDep = new String[] {};

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "ctb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ctb", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "ctb", constituentTags, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "ctb", unmappedConst, jcas);
        // NO DEP TAGS AssertAnnotations.assertTagset(Dependency.class, null, depTags, jcas);
        // NO DEP TAGS AssertAnnotations.assertTagsetMapping(Dependency.class, null, unmappedDep, jcas);
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

        String[] posMapped = { "VERB", "NOUN", "ADJ", "NOUN", "ADP", "NOUN", "VERB", "ADP", "NOUN",
                "NOUN", "ADJ", "ADP", "NOUN", "CONJ", "NOUN", "PUNCT" };

        String[] posOriginal = { "VBP", "NN", "JJ", "NN", "IN", "NN", "VBP", "IN", "NN", "NN",
                "JJ", "IN", "DTNN", "CC", "DTNN", "PUNC" };

        String[] posTags = { ".$$.", "ADJ_NUM", "CC", "CD", "DT", "DTJJ", "DTJJR", "DTNN", "DTNNP",
                "DTNNPS", "DTNNS", "IN", "JJ", "JJR", "NN", "NNP", "NNPS", "NNS", "NOUN_QUANT",
                "PRP", "PRP$", "PUNC", "RB", "RP", "UH", "VB", "VBD", "VBG", "VBN", "VBP", "VN",
                "WP", "WRB" };

        String[] constituentTags = { "ADJP", "ADVP", "CONJP", "FRAG", "INTJ", "LST", "NAC", "NP",
                "PP", "PRN", "PRT", "ROOT", "S", "SBAR", "SBARQ", "SQ", "UCP", "VP", "WHADVP",
                "WHNP", "WHPP", "X" };

        String[] unmappedPos = { ".$$.", "ADJ_NUM", "NOUN_QUANT", "PRP$" };

        String[] unmappedConst = { "LST" };
        
        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "atb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "atb", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "atb", constituentTags, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "atb", unmappedConst, jcas);
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
            Tree recreation = TreeUtils.createStanfordTree(curRoot);

            // make a tree with simple string-labels
            recreation = recreation.deepCopy(recreation.treeFactory(), StringLabel.factory());

            pennFromRecreatedTree = recreation.pennString();
        }

        assertTrue("The recreated syntax-tree did not match the input syntax-tree.",
                pennOriginal.equals(pennFromRecreatedTree));
    }

    @Test
    public void testModelSharing()
        throws Exception
    {
        // Save share override value (if any was set) and enable sharing for the StanfordParser
        String prop = "dkpro.core.resourceprovider.sharable." + StanfordParser.class.getName();
        String oldValue = System.getProperty(prop);
        System.setProperty(prop, "true");
        
        final List<LoggingEvent> records = new ArrayList<LoggingEvent>();

        // Tell the logger to log everything
        Logger rootLogger = org.apache.log4j.LogManager.getRootLogger();
        final org.apache.log4j.Level oldLevel = rootLogger.getLevel();
        rootLogger.setLevel(org.apache.log4j.Level.ALL);
        Appender appender = (Appender) rootLogger.getAllAppenders().nextElement();
        // Capture output, log only what would have passed the original logging level
        appender.addFilter(new org.apache.log4j.spi.Filter()
        {
            @Override
            public int decide(LoggingEvent event)
            {
                records.add(event);
                return event.getLevel().toInt() >= oldLevel.toInt() ? org.apache.log4j.spi.Filter.NEUTRAL
                        : org.apache.log4j.spi.Filter.DENY;
            }
        });

        try {
            AnalysisEngineDescription pipeline = createEngineDescription(
                    createEngineDescription(StanfordParser.class,
                            StanfordParser.PARAM_WRITE_CONSTITUENT, true,
                            StanfordParser.PARAM_WRITE_DEPENDENCY, false),
                    createEngineDescription(StanfordParser.class,
                            StanfordParser.PARAM_WRITE_CONSTITUENT, false,
                            StanfordParser.PARAM_WRITE_DEPENDENCY, true));
            
            JCas jcas = TestRunner.runTest(pipeline, "en", "This is a test .");
            
            boolean found = false;
            for (LoggingEvent e : records) {
                if (String.valueOf(e.getMessage()).contains("Used resource from cache")) {
                    found = true;
                }
            }
            
            assertTrue("No log message about using the cached resource was found!", found);

            String[] dependencies = { 
                    "[  0,  4]NSUBJ(nsubj) D[0,4](This) G[10,14](test)",
                    "[  5,  7]COP(cop) D[5,7](is) G[10,14](test)",
                    "[  8,  9]DET(det) D[8,9](a) G[10,14](test)",
                    "[ 10, 14]ROOT(root) D[10,14](test) G[10,14](test)" };
            
            AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        }
        finally {
            if (oldLevel != null) {
                rootLogger.setLevel(oldLevel);
                appender.clearFilters();
            }
            
            if (oldValue != null) {
                System.setProperty(prop, oldValue);
            }
            else {
                System.clearProperty(prop);
            }
        }
    }

    private JCas runTestWithPosTagger(String aLanguage, String aVariant, String aText,
            Object... aExtraParams)
        throws Exception
    {
        AggregateBuilder aggregate = new AggregateBuilder();
        
        aggregate.add(createEngineDescription(StanfordPosTagger.class));
                
        Object[] params = new Object[] {
                StanfordParser.PARAM_VARIANT, aVariant,
                StanfordParser.PARAM_PRINT_TAGSET, true,
                StanfordParser.PARAM_WRITE_CONSTITUENT, true,
                StanfordParser.PARAM_WRITE_DEPENDENCY, true,
                StanfordParser.PARAM_WRITE_PENN_TREE, true,
                StanfordParser.PARAM_READ_POS, true,
                StanfordParser.PARAM_WRITE_POS, false};
        params = ArrayUtils.addAll(params, aExtraParams);
        aggregate.add(createEngineDescription(StanfordParser.class, params));

        return TestRunner.runTest(aggregate.createAggregateDescription(), aLanguage, aText);
    }

    private JCas runTest(String aLanguage, String aVariant, String aText, Object... aExtraParams)
        throws Exception
    {
        AggregateBuilder aggregate = new AggregateBuilder();
        
        Object[] params = new Object[] {
                StanfordParser.PARAM_VARIANT, aVariant,
                StanfordParser.PARAM_PRINT_TAGSET, true,
                StanfordParser.PARAM_WRITE_CONSTITUENT, true,
                StanfordParser.PARAM_WRITE_DEPENDENCY, true,
                StanfordParser.PARAM_WRITE_PENN_TREE, true,
                StanfordParser.PARAM_WRITE_POS, true};
        params = ArrayUtils.addAll(params, aExtraParams);
        aggregate.add(createEngineDescription(StanfordParser.class, params));

        return TestRunner.runTest(aggregate.createAggregateDescription(), aLanguage, aText);
    }
    
    private JCas runTest(String aLanguage, String aVariant, String[] aTokens)
        throws Exception
    {
        // setup English
        AnalysisEngineDescription parser = createEngineDescription(StanfordParser.class,
                StanfordParser.PARAM_VARIANT, aVariant,
                StanfordParser.PARAM_PRINT_TAGSET, true,
                StanfordParser.PARAM_WRITE_CONSTITUENT, true,
                StanfordParser.PARAM_WRITE_DEPENDENCY, true,
                StanfordParser.PARAM_WRITE_PENN_TREE, true,
                StanfordParser.PARAM_WRITE_POS, true,
                StanfordParser.PARAM_WRITE_PENN_TREE, true,
                StanfordParser.PARAM_QUOTE_BEGIN, new String[] { "‘" },
                StanfordParser.PARAM_QUOTE_END, new String[] { "’" });

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
