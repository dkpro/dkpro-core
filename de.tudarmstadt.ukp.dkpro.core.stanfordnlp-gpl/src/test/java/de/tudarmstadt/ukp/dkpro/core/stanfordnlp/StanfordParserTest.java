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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.TreeUtils;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;
import edu.stanford.nlp.ling.StringLabel;
import edu.stanford.nlp.trees.Tree;

/**
 * @author Oliver Ferschke
 * @author Niklas Jakob
 * @author Richard Eckart de Castilho
 */
public class StanfordParserTest
{
    // TODO Maybe test link to parents (not tested by syntax tree recreation)

    @Test
    public void testGermanPcfg()
        throws Exception
    {
        JCas jcas = runTest("de", "pcfg", "Wir brauchen ein sehr kompliziertes Beispiel , welches "
                + "möglichst viele Konstituenten und Dependenzen beinhaltet .");

        String[] constituentMapped = { "NP 13,111", "NP 55,100", "ROOT 0,113", "S 0,113",
                "S 47,111", "X 17,35", "X 71,100" };

        String[] constituentOriginal = { "AP 17,35", "CNP 71,100", "NP 13,111", "NP 55,100",
                "ROOT 0,113", "S 0,113", "S 47,111" };

        String[] synFunc = {};

        String[] posOriginal = { "PPER", "VVFIN", "ART", "ADV", "ADJA", "NN", "$,", "PRELS", "ADV",
                "PIDAT", "NN", "KON", "NN", "VVFIN", "$." };

        String[] posMapped = { "PR", "V", "ART", "ADV", "ADJ", "NN", "PUNC", "PR", "ADV", "PR",
                "NN", "CONJ", "NN", "V", "PUNC" };

        String[] dependencies = {/** No dependencies for German */ };

        String pennTree = "(ROOT (S (PPER Wir) (VVFIN brauchen) (NP (ART ein) (AP (ADV sehr) "
                + "(ADJA kompliziertes)) (NN Beispiel) ($, ,) (S (PRELS welches) (NP "
                + "(ADV möglichst) (PIDAT viele) (CNP (NN Konstituenten) (KON und) "
                + "(NN Dependenzen))) (VVFIN beinhaltet))) ($. .)))";

        String[] posTags = { "$*LRB*", "$,", "$.", "-", ".$$.", "ADJA", "ADJD", "ADV", "APPO",
                "APPR", "APPRART", "APZR", "ART", "CARD", "FM", "ITJ", "KOKOM", "KON", "KOUI",
                "KOUS", "NE", "NN", "PDAT", "PDS", "PIAT", "PIDAT", "PIS", "PPER", "PPOSAT",
                "PPOSS", "PRELAT", "PRELS", "PRF", "PROAV", "PTKA", "PTKANT", "PTKNEG", "PTKVZ",
                "PTKZU", "PWAT", "PWAV", "PWS", "TRUNC", "VAFIN", "VAIMP", "VAINF", "VAPP",
                "VMFIN", "VMINF", "VMPP", "VVFIN", "VVIMP", "VVINF", "VVIZU", "VVPP", "XY" };

        String[] constituentTags = { "AA", "AP", "AVP", "CAC", "CAP", "CAVP", "CCP", "CH", "CNP",
                "CO", "CPP", "CS", "CVP", "CVZ", "DL", "ISU", "MPN", "MTA", "NM", "NP", "NUR",
                "PP", "QL", "ROOT", "S", "VP", "VZ" };

        String[] unmappedPos = { "$*LRB*", "-", ".$$." };

        String[] unmappedConst = { "NUR" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertSyntacticFunction(synFunc, select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "stts", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "stts", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "negra", constituentTags, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "negra", unmappedConst, jcas);
    }

    @Test
    public void testGermanFactored()
        throws Exception
    {
        JCas jcas = runTest("de", "factored",
                "Wir brauchen ein sehr kompliziertes Beispiel , welches "
                        + "möglichst viele Konstituenten und Dependenzen beinhaltet .");

        String[] constituentMapped = { "NP 13,111", "NP 55,100", "ROOT 0,113", "S 0,113",
                "S 47,111", "X 17,35", "X 55,70", "X 71,100" };

        String[] constituentOriginal = { "AP 17,35", "AP 55,70", "CNP 71,100", "NP 13,111",
                "NP 55,100", "ROOT 0,113", "S 0,113", "S 47,111" };

        String[] posOriginal = { "PPER", "VVFIN", "ART", "ADV", "ADJA", "NN", "$,", "PRELS", "ADV",
                "PIDAT", "NN", "KON", "NN", "VVFIN", "$." };

        String[] posMapped = { "PR", "V", "ART", "ADV", "ADJ", "NN", "PUNC", "PR", "ADV", "PR",
                "NN", "CONJ", "NN", "V", "PUNC" };

        String[] dependencies = { /** No dependencies for German */ };

        String pennTree = "(ROOT (S (PPER Wir) (VVFIN brauchen) (NP (ART ein) (AP "
                + "(ADV sehr) (ADJA kompliziertes)) (NN Beispiel) ($, ,) (S (PRELS welches) "
                + "(NP (AP (ADV möglichst) (PIDAT viele)) (CNP (NN Konstituenten) (KON und) "
                + "(NN Dependenzen))) (VVFIN beinhaltet))) ($. .)))";

        String[] posTags = { "$*LRB*", "$,", "$.", "-", ".$$.", "ADJA", "ADJD", "ADV", "APPO",
                "APPR", "APPRART", "APZR", "ART", "CARD", "FM", "ITJ", "KOKOM", "KON", "KOUI",
                "KOUS", "NE", "NN", "PDAT", "PDS", "PIAT", "PIDAT", "PIS", "PPER", "PPOSAT",
                "PPOSS", "PRELAT", "PRELS", "PRF", "PROAV", "PTKA", "PTKANT", "PTKNEG", "PTKVZ",
                "PTKZU", "PWAT", "PWAV", "PWS", "TRUNC", "VAFIN", "VAIMP", "VAINF", "VAPP",
                "VMFIN", "VMINF", "VMPP", "VVFIN", "VVIMP", "VVINF", "VVIZU", "VVPP", "XY" };

        String[] constituentTags = { "AA", "AP", "AVP", "CAC", "CAP", "CAVP", "CCP", "CH", "CNP",
                "CO", "CPP", "CS", "CVP", "CVZ", "DL", "ISU", "MPN", "MTA", "NM", "NP", "NUR",
                "PP", "QL", "ROOT", "S", "VP", "VZ" };

        String[] unmappedPos = { "$*LRB*", "-", ".$$." };

        String[] unmappedConst = { "NUR" };

        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "stts", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "stts", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "negra", constituentTags, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "negra", unmappedConst, jcas);
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

        String[] posMapped = { "PR", "V", "ART", "ADV", "V", "NN", "NN", "PUNC", "ART", "V", "PP",
                "ADJ", "NN", "CONJ", "NN", "PP", "ADJ", "PUNC" };

        String[] posOriginal = { "PRP", "VBP", "DT", "RB", "VBN", "NN", "NN", ",", "WDT", "VBZ",
                "IN", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };

        String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) "
                + "(VBN complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP (WDT which)) "
                + "(S (VP (VBZ contains) (PP (IN as) (NP (JJ many) (NNS constituents) (CC and) "
                + "(NNS dependencies))) (PP (IN as) (ADJP (JJ possible)))))))) (. .)))";

        String[] posTags = { "#", "$", "''", ",", "-LRB-", "-RRB-", ".", ".$$.", ":", "CC", "CD",
                "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP", "NNPS", "NNS",
                "PDT", "POS", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM", "TO", "UH", "VB",
                "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };

        String[] constituentTags = { "ADJP", "ADVP", "CONJP", "FRAG", "INTJ", "LST", "NAC", "NP",
                "NX", "PP", "PRN", "PRT", "QP", "ROOT", "RRC", "S", "SBAR", "SBARQ", "SINV", "SQ",
                "UCP", "VP", "WHADJP", "WHADVP", "WHNP", "WHPP", "X" };

        String[] depTags = { "acomp", "advcl", "advmod", "agent", "amod", "appos", "arg", "aux",
                "auxpass", "cc", "ccomp", "comp", "conj", "cop", "csubj", "csubjpass", "dep",
                "det", "discourse", "dobj", "expl", "goeswith", "gov", "iobj", "mark", "mod",
                "mwe", "neg", "nn", "npadvmod", "nsubj", "nsubjpass", "num", "number", "obj",
                "parataxis", "pcomp", "pobj", "poss", "possessive", "preconj", "pred", "predet",
                "prep", "prt", "punct", "quantmod", "rcmod", "ref", "rel", "sdep", "subj", "tmod",
                "vmod", "xcomp" };

        String[] unmappedPos = { "#", "$", "''", "-LRB-", "-RRB-", ".$$.", "``" };

        String[] unmappedConst = {};

        String[] unmappedDep = { "gov" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "ptb", constituentTags, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "ptb", unmappedConst, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "stanford341", depTags, jcas);
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

        String[] posMapped = { "PR", "V", "ART", "ADV", "V", "NN", "NN", "PUNC", "ART", "V", "PP",
                "ADJ", "NN", "CONJ", "NN", "PP", "ADJ", "PUNC" };

        String[] posOriginal = { "PRP", "VBP", "DT", "RB", "VBN", "NN", "NN", ",", "WDT", "VBZ",
                "IN", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };

        String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) "
                + "(VBN complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP (WDT which)) "
                + "(S (VP (VBZ contains) (PP (IN as) (NP (JJ many) (NNS constituents) (CC and) "
                + "(NNS dependencies))) (PP (IN as) (ADJP (JJ possible)))))))) (. .)))";

        String[] posTags = { "#", "$", "''", ",", "-LRB-", "-RRB-", ".", ".$$.", ":", "CC", "CD",
                "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP", "NNPS", "NNS",
                "PDT", "POS", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM", "TO", "UH", "VB",
                "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };

        String[] constituentTags = { "ADJP", "ADVP", "CONJP", "FRAG", "INTJ", "LST", "NAC", "NP",
                "NX", "PP", "PRN", "PRT", "QP", "ROOT", "RRC", "S", "SBAR", "SBARQ", "SINV", "SQ",
                "UCP", "VP", "WHADJP", "WHADVP", "WHNP", "WHPP", "X" };

        String[] depTags = { "acomp", "advcl", "advmod", "agent", "amod", "appos", "arg", "aux",
                "auxpass", "cc", "ccomp", "comp", "conj", "cop", "csubj", "csubjpass", "dep",
                "det", "discourse", "dobj", "expl", "goeswith", "gov", "iobj", "mark", "mod",
                "mwe", "neg", "nn", "npadvmod", "nsubj", "nsubjpass", "num", "number", "obj",
                "parataxis", "pcomp", "pobj", "poss", "possessive", "preconj", "pred", "predet",
                "prep", "prt", "punct", "quantmod", "rcmod", "ref", "rel", "sdep", "subj", "tmod",
                "vmod", "xcomp" };

        String[] unmappedPos = { "#", "$", "''", "-LRB-", "-RRB-", ".$$.", "``" };

        String[] unmappedConst = {};

        String[] unmappedDep = { "gov" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "ptb", constituentTags, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "ptb", unmappedConst, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "stanford341", depTags, jcas);
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

        String[] posMapped = new String[] { "PR", "V", "ART", "ADV", "V", "NN", "NN", "PUNC",
                "ART", "V", "ADV", "ADJ", "NN", "CONJ", "NN", "PP", "ADJ", "PUNC" };

        String[] posOriginal = new String[] { "PRP", "VBP", "DT", "RB", "VBN", "NN", "NN", ",",
                "WDT", "VBZ", "RB", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };

        String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) "
                + "(VBN complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP (WDT which)) "
                + "(S (VP (VBZ contains) (NP (ADJP (RB as) (JJ many)) (NNS constituents) (CC and) "
                + "(NNS dependencies)) (PP (IN as) (ADJP (JJ possible)))))))) (. .)))";

        String[] posTags = new String[] { "#", "$", "''", ",", "-LRB-", "-RRB-", ".", ".$$.", ":",
                "CC", "CD", "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP",
                "NNPS", "NNS", "PDT", "POS", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM", "TO",
                "UH", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };

        String[] constituentTags = new String[] { "ADJP", "ADVP", "CONJP", "FRAG", "INTJ", "LST",
                "NAC", "NP", "NX", "PP", "PRN", "PRT", "QP", "ROOT", "RRC", "S", "SBAR", "SBARQ",
                "SINV", "SQ", "UCP", "VP", "WHADJP", "WHADVP", "WHNP", "WHPP", "X" };

        String[] depTags = new String[] { "acomp", "advcl", "advmod", "agent", "amod", "appos",
                "arg", "aux", "auxpass", "cc", "ccomp", "comp", "conj", "cop", "csubj",
                "csubjpass", "dep", "det", "discourse", "dobj", "expl", "goeswith", "gov", "iobj",
                "mark", "mod", "mwe", "neg", "nn", "npadvmod", "nsubj", "nsubjpass", "num",
                "number", "obj", "parataxis", "pcomp", "pobj", "poss", "possessive", "preconj",
                "pred", "predet", "prep", "prt", "punct", "quantmod", "rcmod", "ref", "rel",
                "sdep", "subj", "tmod", "vmod", "xcomp" };

        String[] unmappedPos = new String[] { "#", "$", "''", "-LRB-", "-RRB-", ".$$.", "``" };

        String[] unmappedConst = new String[] {};

        String[] unmappedDep = new String[] { "gov" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "ptb", constituentTags, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "ptb", unmappedConst, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "stanford341", depTags, jcas);
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

        String[] posMapped = { "PR", "V", "ART", "ADV", "V", "NN", "NN", "PUNC", "ART", "V", "ADV",
                "ADJ", "NN", "CONJ", "NN", "PP", "ADJ", "PUNC" };

        String[] posOriginal = { "PRP", "VBP", "DT", "RB", "VBN", "NN", "NN", ",", "WDT", "VBZ",
                "RB", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };

        String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) "
                + "(VBN complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP (WDT which)) "
                + "(S (VP (VBZ contains) (NP (QP (RB as) (JJ many)) (NNS constituents) (CC and) "
                + "(NNS dependencies)) (PP (IN as) (ADJP (JJ possible)))))))) (. .)))";

        String[] posTags = { "#", "$", "''", ",", "-LRB-", "-RRB-", ".", ".$$.", ":", "CC", "CD",
                "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP", "NNPS", "NNS",
                "PDT", "POS", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM", "TO", "UH", "VB",
                "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };

        String[] constituentTags = { "ADJP", "ADVP", "CONJP", "FRAG", "INTJ", "LST", "NAC", "NP",
                "NX", "PP", "PRN", "PRT", "QP", "ROOT", "RRC", "S", "SBAR", "SBARQ", "SINV", "SQ",
                "UCP", "VP", "WHADJP", "WHADVP", "WHNP", "WHPP", "X" };

        String[] depTags = { "acomp", "advcl", "advmod", "agent", "amod", "appos", "arg", "aux",
                "auxpass", "cc", "ccomp", "comp", "conj", "cop", "csubj", "csubjpass", "dep",
                "det", "discourse", "dobj", "expl", "goeswith", "gov", "iobj", "mark", "mod",
                "mwe", "neg", "nn", "npadvmod", "nsubj", "nsubjpass", "num", "number", "obj",
                "parataxis", "pcomp", "pobj", "poss", "possessive", "preconj", "pred", "predet",
                "prep", "prt", "punct", "quantmod", "rcmod", "ref", "rel", "sdep", "subj", "tmod",
                "vmod", "xcomp" };

        String[] unmappedPos = { "#", "$", "''", "-LRB-", "-RRB-", ".$$.", "``" };

        String[] unmappedConst = {};

        String[] unmappedDep = { "gov" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "ptb", constituentTags, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "ptb", unmappedConst, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "stanford341", depTags, jcas);
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

        String[] posMapped = { "PR", "V", "ART", "ADV", "ADJ", "NN", "NN", "PUNC", "ART", "V",
                "PP", "ADJ", "NN", "CONJ", "NN", "PP", "ADJ", "PUNC" };

        String[] posOriginal = { "PRP", "VBP", "DT", "RB", "JJ", "NN", "NN", ",", "WDT", "VBZ",
                "IN", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };

        String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) "
                + "(JJ complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP (WDT which)) "
                + "(S (VP (VBZ contains) (PP (IN as) (NP (NP (JJ many) (NNS constituents) "
                + "(CC and) (NNS dependencies)) (PP (IN as) (ADJP (JJ possible)))))))))) (. .)))";

        String[] posTags = { "#", "$", "''", ",", "-LRB-", "-RRB-", ".", ".$$.", ":", "CC", "CD",
                "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP", "NNPS", "NNS",
                "PDT", "POS", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM", "TO", "UH", "VB",
                "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };

        String[] constituentTags = { "ADJP", "ADVP", "CONJP", "FRAG", "INTJ", "LST", "NAC", "NP",
                "NX", "PP", "PRN", "PRT", "QP", "ROOT", "RRC", "S", "SBAR", "SBARQ", "SINV", "SQ",
                "UCP", "VP", "WHADJP", "WHADVP", "WHNP", "WHPP", "X" };

        String[] depTags = { "acomp", "advcl", "advmod", "agent", "amod", "appos", "arg", "aux",
                "auxpass", "cc", "ccomp", "comp", "conj", "cop", "csubj", "csubjpass", "dep",
                "det", "discourse", "dobj", "expl", "goeswith", "gov", "iobj", "mark", "mod",
                "mwe", "neg", "nn", "npadvmod", "nsubj", "nsubjpass", "num", "number", "obj",
                "parataxis", "pcomp", "pobj", "poss", "possessive", "preconj", "pred", "predet",
                "prep", "prt", "punct", "quantmod", "rcmod", "ref", "rel", "sdep", "subj", "tmod",
                "vmod", "xcomp" };

        String[] unmappedPos = { "#", "$", "''", "-LRB-", "-RRB-", ".$$.", "``" };

        String[] unmappedConst = {};

        String[] unmappedDep = { "gov" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "ptb", constituentTags, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "ptb", unmappedConst, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "stanford341", depTags, jcas);
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

        String[] posMapped = { "PR", "V", "ART", "ADV", "V", "NN", "NN", "PUNC", "ART", "V", "ADV",
                "ADJ", "NN", "CONJ", "NN", "PP", "ADJ", "PUNC" };

        String[] posOriginal = { "PRP", "VBP", "DT", "RB", "VBN", "NN", "NN", ",", "WDT", "VBZ",
                "RB", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };

        String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) "
                + "(VBN complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP (WDT which)) "
                + "(S (VP (VBZ contains) (NP (QP (RB as) (JJ many)) (NNS constituents) (CC and) "
                + "(NNS dependencies)) (PP (IN as) (ADJP (JJ possible)))))))) (. .)))";

        String[] posTags = { "#", "$", "''", ",", "-LRB-", "-RRB-", ".", ".$$.", ":", "CC", "CD",
                "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP", "NNPS", "NNS",
                "PDT", "POS", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM", "TO", "UH", "VB",
                "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };

        String[] constituentTags = { "ADJP", "ADVP", "CONJP", "FRAG", "INTJ", "LST", "NAC", "NP",
                "NX", "PP", "PRN", "PRT", "QP", "ROOT", "RRC", "S", "SBAR", "SBARQ", "SINV", "SQ",
                "UCP", "VP", "WHADJP", "WHADVP", "WHNP", "WHPP", "X" };

        String[] depTags = { "acomp", "advcl", "advmod", "agent", "amod", "appos", "arg", "aux",
                "auxpass", "cc", "ccomp", "comp", "conj", "cop", "csubj", "csubjpass", "dep",
                "det", "discourse", "dobj", "expl", "goeswith", "gov", "iobj", "mark", "mod",
                "mwe", "neg", "nn", "npadvmod", "nsubj", "nsubjpass", "num", "number", "obj",
                "parataxis", "pcomp", "pobj", "poss", "possessive", "preconj", "pred", "predet",
                "prep", "prt", "punct", "quantmod", "rcmod", "ref", "rel", "sdep", "subj", "tmod",
                "vmod", "xcomp" };

        String[] unmappedPos = { "#", "$", "''", "-LRB-", "-RRB-", ".$$.", "``" };

        String[] unmappedConst = {};

        String[] unmappedDep = { "gov" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Constituent.class, "ptb", constituentTags, jcas);
        AssertAnnotations.assertTagsetMapping(Constituent.class, "ptb", unmappedConst, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "stanford341", depTags, jcas);
        AssertAnnotations.assertTagsetMapping(Dependency.class, "stanford341", unmappedDep, jcas);
    }

    /**
     * This test uses simple double quotes.
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

    /**
     * Tests the parser reading pre-existing POS tags
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

        String[] constituentMapped = { "NP 11,48", "NP 21,48", "NP 61,64", "NP 74,90", "NP 95,120",
                "PP 18,48", "ROOT 0,138", "X 0,138", "X 0,58", "X 121,136", "X 124,136",
                "X 128,136", "X 32,43", "X 32,48", "X 61,90", "X 65,73", "X 91,136" };

        String[] constituentOriginal = { "AP 128,136", "AdP 32,48", "COORD 121,136", "MWADV 32,43",
                "NP 11,48", "NP 21,48", "NP 61,64", "NP 74,90", "NP 95,120", "PP 18,48",
                "ROOT 0,138", "SENT 0,138", "Srel 61,90", "Ssub 124,136", "Ssub 91,136", "VN 0,58",
                "VN 65,73" };

        String[] dependencies = {/** No dependencies for French */ };

        String[] posMapped = { "PR", "V", "NN", "PP", "ART", "NN", "PP", "N", "ADV", "V", "PUNC",
                "PR", "V", "ART", "NN", "CONJ", "ART", "ADJ", "NN", "CONJ", "CONJ", "ADJ", "PUNC" };

        String[] posOriginal = { "CLS", "V", "NC", "P", "DET", "NC", "P", "N", "ADV", "VPP",
                "PUNC", "PROREL", "V", "DET", "NC", "CS", "DET", "ADJ", "NC", "CC", "CS", "ADJ",
                "PUNC" };

        String pennTree = "(ROOT (SENT (VN (CLS Nous) (V avons) (NP (NC besoin) (PP (P d') (NP "
                + "(DET une) (NC phrase) (AdP (MWADV (P par) (N exemple)) (ADV très))))) "
                + "(VPP compliqué)) (PUNC ,) (Srel (NP (PROREL qui)) (VN (V contient)) (NP "
                + "(DET des) (NC constituants))) (Ssub (CS que) (NP (DET de) (ADJ nombreuses) "
                + "(NC dépendances)) (COORD (CC et) (Ssub (CS que) (AP (ADJ possible))))) "
                + "(PUNC .)))";

        String[] posTags = { ".$$.", "A", "ADJ", "ADJWH", "ADV", "ADVWH", "C", "CC", "CL", "CLO",
                "CLR", "CLS", "CS", "DET", "DETWH", "ET", "I", "N", "NC", "NPP", "P", "PREF",
                "PRO", "PROREL", "PROWH", "PUNC", "V", "VIMP", "VINF", "VPP", "VPR", "VS" };

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

        String[] constituentMapped = { "NP 0,53", "NP 17,37", "NP 43,53", "PP 14,37", "PP 26,37",
                "PP 38,53", "ROOT 0,55", "X 0,55", "X 29,37" };

        String[] constituentOriginal = { "AP 29,37", "NP 0,53", "NP 17,37", "NP 43,53", "PP 14,37",
                "PP 26,37", "PP 38,53", "ROOT 0,55", "SENT 0,55" };

        String[] posMapped = { "ART", "NN", "PP", "ART", "NN", "PP", "ADJ", "PP", "ART", "NN",
                "PUNC" };

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

        String[] posMapped = { "PR", "V", "CARD", "ADJ", "ADJ", "PRT", "NN", "ADJ", "NN", "V",
                "CARD", "NN", "CONJ", "ADJ", "PRT", "V", "PUNC" };

        String[] posOriginal = { "PN", "VV", "CD", "AD", "JJ", "DEG", "NN", "AD", "NN", "VV", "CD",
                "NN", "CC", "AD", "DEV", "VV", "PU" };

        String pennTree = "(ROOT (IP (IP (NP (PN 我们)) (VP (VV 需要) (NP (QP (CD 一个)) (DNP "
                + "(ADJP (ADVP (AD 非常)) (ADJP (JJ 复杂))) (DEG 的)) (NP (NN 句子))))) (IP (ADVP "
                + "(AD 例如)) (NP (NN 其中)) (VP (VP (VV 包含) (NP (QP (CD 许多)) (NP (NN 成分)))) "
                + "(CC 和) (VP (DVP (ADVP (AD 尽可能)) (DEV 的)) (VP (VV 依赖))))) (PU 。)))";

        String[] posTags = { ".$$.", "AD", "AS", "BA", "CC", "CD", "CS", "DEC", "DEG", "DER",
                "DEV", "DT", "ETC", "FRAG", "FW", "IJ", "JJ", "LB", "LC", "M", "MSP", "NN", "NR",
                "NT", "OD", "ON", "P", "PN", "PU", "SB", "SP", "URL", "VA", "VC", "VE", "VV", "X" };

        String[] constituentTags = { "ADJP", "ADVP", "CLP", "CP", "DFL", "DNP", "DP", "DVP", "FLR",
                "INC", "INTJ", "IP", "LCP", "LST", "NP", "PP", "PRN", "QP", "ROOT", "UCP", "VCD",
                "VCP", "VNV", "VP", "VPT", "VRD", "VSB", "WHPP" };

        // NO DEP TAGS String[] depTags = new String[] {};

        String[] unmappedPos = { ".$$.", "FRAG", "URL" };

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

        String[] posMapped = { "PR", "V", "CARD", "ADJ", "V", "PRT", "NN", "ADJ", "NN", "V",
                "CARD", "NN", "CONJ", "ADJ", "PRT", "NN", "PUNC" };

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

    @Ignore("Currently fails an assertion in StanfordAnnotator:188 - need to investigate")
    @Test
    public void testArabicFactored()
        throws Exception
    {
        JCas jcas = runTest("ar", "factored",
                "نحن بحاجة إلى مثال على جملة معقدة جدا، والتي تحتوي على مكونات مثل العديد من والتبعيات وقت ممكن .");

        String[] constituentMapped = { "NP 0,1", "ROOT 0,1" };

        String[] constituentOriginal = { "NP 0,1", "ROOT 0,1" };

        String[] dependencies = {};

        String[] posMapped = { "POS", "POS" };

        String[] posOriginal = { "NN", "NN" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
    }

    /**
     * This tests whether a complete syntax tree can be recreated from the annotations without any
     * loss. Consequently, all links to children should be correct. (This makes no assertions about
     * the parent-links, because they are not used for the recreation)
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
                            StanfordParser.PARAM_SHARED_MODEL, true,
                            StanfordParser.PARAM_WRITE_CONSTITUENT, true,
                            StanfordParser.PARAM_WRITE_DEPENDENCY, false),
                    createEngineDescription(StanfordParser.class,
                            StanfordParser.PARAM_SHARED_MODEL, true,
                            StanfordParser.PARAM_WRITE_CONSTITUENT, false,
                            StanfordParser.PARAM_WRITE_DEPENDENCY, true));
            
            TestRunner.runTest(pipeline, "en", "This is a test .");
            
            boolean found = false;
            for (LoggingEvent e : records) {
                if (String.valueOf(e.getMessage()).contains("Used resource from cache")) {
                    found = true;
                }
            }
            
            assertTrue("No log message about using the cached resource was found!", found);
        }
        finally {
            if (oldLevel != null) {
                rootLogger.setLevel(oldLevel);
                appender.clearFilters();
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
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
    
    @Before
    public void setupLogging()
    {
        System.setProperty("org.apache.uima.logger.class", "org.apache.uima.util.impl.Log4jLogger_impl");
    }
}
