/*******************************************************************************
 * Copyright 2010
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectSingle;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.ArrayUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.languagetool.LanguageToolSegmenter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.TreeUtils;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import edu.stanford.nlp.ling.StringLabel;
import edu.stanford.nlp.trees.Tree;

/**
 * @author Oliver Ferschke
 * @author Niklas Jakob
 * @author Richard Eckart de Castilho
 */
@SuppressWarnings("unused")
public class StanfordParserTest
{
    static final String documentEnglish = "We need a very complicated example sentence, which "
            + "contains as many constituents and dependencies as possible.";

    // TODO Maybe test link to parents (not tested by syntax tree recreation)

    @Test
    public void testGermanPcfg()
        throws Exception
    {
        JCas jcas = runTest("de", "pcfg", "Wir brauchen ein sehr kompliziertes Beispiel, welches "
                + "möglichst viele Konstituenten und Dependenzen beinhaltet.");

        String[] constituentMapped = new String[] { "NP 13,110", "NP 64,99", "ROOT 0,111",
                "S 0,111", "S 46,110", "X 17,35", "X 70,99" };

        String[] constituentOriginal = new String[] { "AP 17,35", "CNP 70,99", "NP 13,110",
                "NP 64,99", "ROOT 0,111", "S 0,111", "S 46,110" };

        String[] synFunc = new String[] { "OA 13,110", "SB 64,99" };

        String[] posOriginal = new String[] { "PPER", "VVFIN", "ART", "ADV", "ADJA", "NN", "$,",
                "PRELS", "ADV", "PIDAT", "NN", "KON", "NN", "VVFIN", "$." };

        String[] posMapped = new String[] { "PR", "V", "ART", "ADV", "ADJ", "NN", "PUNC", "PR",
                "ADV", "PR", "NN", "CONJ", "NN", "V", "PUNC" };

        String[] dependencies = new String[] {/** No dependencies for German */
        };

        String pennTree = "(ROOT (S (PPER-SB Wir) (VVFIN brauchen) (NP-OA (ART ein) (AP "
                + "(ADV sehr) (ADJA kompliziertes)) (NN Beispiel) ($, ,) (S (PRELS-SB welches) "
                + "(ADV möglichst) (NP-SB (PIDAT viele) (CNP (NN Konstituenten) (KON und) "
                + "(NN Dependenzen))) (VVFIN beinhaltet))) ($. .)))";

        String[] posTags = new String[] { "$*LRB*", "$,", "$.", "-", ".$$.", "ADJA", "ADJD", "ADV",
                "APPO", "APPR", "APPRART", "APZR", "ART", "CARD", "FM", "ITJ", "KOKOM", "KON",
                "KOUI", "KOUS", "NE", "NN", "PDAT", "PDS", "PIAT", "PIDAT", "PIS", "PPER",
                "PPOSAT", "PPOSS", "PRELAT", "PRELS", "PRF", "PROAV", "PTKA", "PTKANT", "PTKNEG",
                "PTKVZ", "PTKZU", "PWAT", "PWAV", "PWS", "TRUNC", "VAFIN", "VAIMP", "VAINF",
                "VAPP", "VMFIN", "VMINF", "VMPP", "VVFIN", "VVIMP", "VVINF", "VVIZU", "VVPP", "XY" };

        String[] constituentTags = new String[] { "AA", "AP", "AVP", "CAC", "CAP", "CAVP", "CCP",
                "CH", "CNP", "CO", "CPP", "CS", "CVP", "CVZ", "DL", "ISU", "MPN", "MTA", "NM",
                "NP", "NUR", "PP", "QL", "ROOT", "S", "VP", "VZ" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertSyntacticFunction(synFunc, select(jcas, Constituent.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "stts", posTags, jcas);
        AssertAnnotations.assertTagset(Constituent.class, null, constituentTags, jcas);
    }

    @Test
    public void testGermanFactored()
        throws Exception
    {
        JCas jcas = runTest("de", "factored",
                "Wir brauchen ein sehr kompliziertes Beispiel, welches "
                        + "möglichst viele Konstituenten und Dependenzen beinhaltet.");

        String[] constituentMapped = new String[] { "NP 13,110", "NP 54,99", "ROOT 0,111",
                "S 0,111", "S 46,110", "X 17,35", "X 54,69", "X 70,99" };

        String[] constituentOriginal = new String[] { "AP 17,35", "AP 54,69", "CNP 70,99",
                "NP 13,110", "NP 54,99", "ROOT 0,111", "S 0,111", "S 46,110" };

        String[] posOriginal = new String[] { "PPER", "VVFIN", "ART", "ADV", "ADJA", "NN", "$,",
                "PRELS", "ADV", "PIDAT", "NN", "KON", "NN", "VVFIN", "$." };

        String[] posMapped = new String[] { "PR", "V", "ART", "ADV", "ADJ", "NN", "PUNC", "PR",
                "ADV", "PR", "NN", "CONJ", "NN", "V", "PUNC" };

        String[] dependencies = new String[] {/** No dependencies for German */
        };

        String pennTree = "(ROOT (S (PPER-SB Wir) (VVFIN brauchen) (NP-OA (ART ein) (AP "
                + "(ADV sehr) (ADJA kompliziertes)) (NN Beispiel) ($, ,) (S (PRELS-SB welches) "
                + "(NP-DA (AP (ADV möglichst) (PIDAT viele)) (CNP (NN Konstituenten) (KON und) "
                + "(NN Dependenzen))) (VVFIN beinhaltet))) ($. .)))";

        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
    }

    @Test
    public void testEnglishPcfg()
        throws Exception
    {
        JCas jcas = runTest("en", "pcfg", documentEnglish);

        String[] constituentMapped = new String[] { "ROOT 0,110", "S 0,110", "NP 0,2", "VP 3,109",
                "NP 8,109", "NP 8,43", "ADJP 10,26", "SBAR 45,109", "WHNP 45,50", "VP 51,109",
                "S 51,109", "PP 60,97", "NP 63,97", "PP 98,109", "ADJP 101,109" };

        String[] constituentOriginal = new String[] { "ROOT 0,110", "S 0,110", "NP 0,2",
                "VP 3,109", "NP 8,109", "NP 8,43", "ADJP 10,26", "SBAR 45,109", "WHNP 45,50",
                "VP 51,109", "S 51,109", "PP 60,97", "NP 63,97", "PP 98,109", "ADJP 101,109" };

        String[] dependencies = new String[] { 
                "ADVMOD(advmod)[10,14] D(very)[10,14] G(complicated)[15,26]",
                "AMOD(amod)[15,26] D(complicated)[15,26] G(sentence)[35,43]",
                "AMOD(amod)[63,67] D(many)[63,67] G(constituents)[68,80]",
                "CC(cc)[81,84] D(and)[81,84] G(constituents)[68,80]",
                "CONJ(conj)[85,97] D(dependencies)[85,97] G(constituents)[68,80]",
                "DET(det)[8,9] D(a)[8,9] G(sentence)[35,43]",
                "DOBJ(dobj)[35,43] D(sentence)[35,43] G(need)[3,7]",
                "NN(nn)[27,34] D(example)[27,34] G(sentence)[35,43]",
                "NSUBJ(nsubj)[0,2] D(We)[0,2] G(need)[3,7]",
                "NSUBJ(nsubj)[45,50] D(which)[45,50] G(contains)[51,59]",
                "POBJ(pobj)[101,109] D(possible)[101,109] G(as)[98,100]",
                "POBJ(pobj)[68,80] D(constituents)[68,80] G(as)[60,62]",
                "PREP(prep)[60,62] D(as)[60,62] G(contains)[51,59]",
                "PREP(prep)[98,100] D(as)[98,100] G(contains)[51,59]",
                "RCMOD(rcmod)[51,59] D(contains)[51,59] G(sentence)[35,43]" };

        String[] lemma = new String[] { "we", "need", "a", "very", "complicate", "example",
                "sentence", ",", "which", "contain", "as", "many", "constituent", "and",
                "dependency", "as", "possible", "." };

        String[] posMapped = new String[] { "PR", "V", "ART", "ADV", "V", "NN", "NN", "PUNC",
                "ART", "V", "PP", "ADJ", "NN", "CONJ", "NN", "PP", "ADJ", "PUNC" };

        String[] posOriginal = new String[] { "PRP", "VBP", "DT", "RB", "VBN", "NN", "NN", ",",
                "WDT", "VBZ", "IN", "JJ", "NNS", "CC", "NNS", "IN", "JJ", "." };

        String pennTree = "(ROOT (S (NP (PRP We)) (VP (VBP need) (NP (NP (DT a) (ADJP (RB very) "
                + "(VBN complicated)) (NN example) (NN sentence)) (, ,) (SBAR (WHNP (WDT which)) "
                + "(S (VP (VBZ contains) (PP (IN as) (NP (JJ many) (NNS constituents) (CC and) "
                + "(NNS dependencies))) (PP (IN as) (ADJP (JJ possible)))))))) (. .)))";

        String[] posTags = new String[] { "#", "$", "''", ",", "-LRB-", "-RRB-", ".", ".$$.", ":",
                "CC", "CD", "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP",
                "NNPS", "NNS", "PDT", "POS", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM", "TO",
                "UH", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };

        String[] constituentTags = new String[] { "ADJP", "ADVP", "CONJP", "FRAG", "INTJ", "LST",
                "NAC", "NP", "NX", "PP", "PRN", "PRT", "QP", "ROOT", "RRC", "S", "SBAR", "SBARQ",
                "SINV", "SQ", "UCP", "VP", "WHADJP", "WHADVP", "WHNP", "WHPP", "X" };

        String[] depTags = new String[] { "acomp", "advcl", "advmod", "agent", "amod", "appos",
                "arg", "attr", "aux", "auxpass", "cc", "ccomp", "comp", "conj", "cop", "csubj",
                "csubjpass", "dep", "det", "discourse", "dobj", "expl", "goeswith", "gov",
                "infmod", "iobj", "mark", "mod", "mwe", "neg", "nn", "npadvmod", "nsubj",
                "nsubjpass", "num", "number", "obj", "parataxis", "partmod", "pcomp", "pobj",
                "poss", "possessive", "preconj", "pred", "predet", "prep", "prt", "punct",
                "quantmod", "rcmod", "ref", "rel", "sdep", "subj", "tmod", "xcomp", "xsubj" };

        AssertAnnotations.assertLemma(lemma, select(jcas, Lemma.class));
        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", posTags, jcas);
        AssertAnnotations.assertTagset(Constituent.class, null, constituentTags, jcas);
        AssertAnnotations.assertTagset(Dependency.class, null, depTags, jcas);
    }

    @Test
    public void testEnglishFactored()
        throws Exception
    {
        JCas jcas = runTest("en", "factored", documentEnglish);

        String[] constituentMapped = new String[] { "ADJP 10,26", "ADJP 101,109", "ADJP 60,67",
                "NP 0,2", "NP 60,97", "NP 8,109", "NP 8,43", "PP 98,109", "ROOT 0,110", "S 0,110",
                "S 51,109", "SBAR 45,109", "VP 3,109", "VP 51,109", "WHNP 45,50" };

        String[] constituentOriginal = new String[] { "ADJP 10,26", "ADJP 101,109", "ADJP 60,67",
                "NP 0,2", "NP 60,97", "NP 8,109", "NP 8,43", "PP 98,109", "ROOT 0,110", "S 0,110",
                "S 51,109", "SBAR 45,109", "VP 3,109", "VP 51,109", "WHNP 45,50" };

        String[] dependencies = new String[] { 
                "ADVMOD(advmod)[10,14] D(very)[10,14] G(complicated)[15,26]",
                "ADVMOD(advmod)[60,62] D(as)[60,62] G(many)[63,67]",
                "AMOD(amod)[15,26] D(complicated)[15,26] G(sentence)[35,43]",
                "AMOD(amod)[63,67] D(many)[63,67] G(constituents)[68,80]",
                "CC(cc)[81,84] D(and)[81,84] G(constituents)[68,80]",
                "CONJ(conj)[85,97] D(dependencies)[85,97] G(constituents)[68,80]",
                "DET(det)[8,9] D(a)[8,9] G(sentence)[35,43]",
                "DOBJ(dobj)[35,43] D(sentence)[35,43] G(need)[3,7]",
                "DOBJ(dobj)[68,80] D(constituents)[68,80] G(contains)[51,59]",
                "NN(nn)[27,34] D(example)[27,34] G(sentence)[35,43]",
                "NSUBJ(nsubj)[0,2] D(We)[0,2] G(need)[3,7]",
                "NSUBJ(nsubj)[45,50] D(which)[45,50] G(contains)[51,59]",
                "POBJ(pobj)[101,109] D(possible)[101,109] G(as)[98,100]",
                "PREP(prep)[98,100] D(as)[98,100] G(contains)[51,59]",
                "RCMOD(rcmod)[51,59] D(contains)[51,59] G(sentence)[35,43]" };

        String[] lemma = new String[] { "we", "need", "a", "very", "complicate", "example",
                "sentence", ",", "which", "contain", "as", "many", "constituent", "and",
                "dependency", "as", "possible", "." };

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

        String[] unmappedPos = new String[] { "#", "$", "''", "-LRB-", "-RRB-", ".$$.", "``" };

        AssertAnnotations.assertLemma(lemma, select(jcas, Lemma.class));
        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ptb", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Constituent.class, null, constituentTags, jcas);
    }

    /**
     * This test uses simple double quotes.
     */
    @Test
    public void testEnglishFactoredDirectSpeech()
        throws Exception
    {
        JCas jcas = runTest("en", "factored",
                "\"It's cold outside,\" he said, \"and it's starting to rain.\"");

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

    @Test
    public void testFrenchFactored()
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

        JCas jcas = runTest("fr", "factored", "Nous avons besoin d'une phrase par exemple très "
                + "compliqué, qui contient des constituants que de nombreuses dépendances et que "
                + "possible.");

        String[] constituentMapped = new String[] { "NP 18,30", "NP 59,62", "NP 72,88",
                "NP 93,118", "ROOT 0,135", "X 0,135", "X 0,17", "X 0,42", "X 119,134", "X 122,134",
                "X 126,134", "X 31,42", "X 43,57", "X 5,17", "X 59,88", "X 63,71", "X 89,134" };

        String[] constituentOriginal = new String[] { "AP 126,134", "COORD 119,134", "MWADV 31,42",
                "MWV 5,17", "NP 18,30", "NP 59,62", "NP 72,88", "NP 93,118", "ROOT 0,135",
                "SENT 0,135", "Srel 59,88", "Ssub 122,134", "Ssub 89,134", "VN 0,17", "VN 43,57",
                "VN 63,71", "VPinf 0,42" };

        String[] dependencies = new String[] {/** No dependencies for French */
        };

        String[] posMapped = new String[] { "PR", "V", "N", "ART", "N", "PP", "N", "ADV", "V",
                "PUNC", "PR", "V", "ART", "N", "CONJ", "ART", "ADJ", "N", "CONJ", "CONJ", "ADJ",
                "PUNC" };

        String[] posOriginal = new String[] { "CL", "V", "N", "D", "N", "P", "N", "ADV", "V",
                "PUNC", "PRO", "V", "D", "N", "C", "D", "A", "N", "C", "C", "A", "PUNC" };

        String pennTree = "(ROOT (SENT (VPinf (VN (CL Nous) (MWV (V avons) (N besoin))) "
                + "(NP (D d'une) (N phrase)) (MWADV (P par) (N exemple))) (VN (ADV très) "
                + "(V compliqué)) (PUNC ,) (Srel (NP (PRO qui)) (VN (V contient)) (NP (D des) "
                + "(N constituants))) (Ssub (C que) (NP (D de) (A nombreuses) (N dépendances)) "
                + "(COORD (C et) (Ssub (C que) (AP (A possible))))) (PUNC .)))";

        String[] posTags = new String[] { ".$$.", "A", "ADV", "C", "CL", "D", "ET", "I", "N", "P",
                "PREF", "PRO", "PUNC", "V" };

        String[] constituentTags = new String[] { "AP", "AdP", "COORD", "MWA", "MWADV", "MWC",
                "MWCL", "MWD", "MWET", "MWI", "MWN", "MWP", "MWPRO", "MWV", "NP", "PP", "ROOT",
                "SENT", "Sint", "Srel", "Ssub", "VN", "VPinf", "VPpart" };

        String[] unmappedPos = new String[] { ".$$." };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "ftb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ftb", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Constituent.class, null, constituentTags, jcas);
    }

    // @Ignore("Dependency types not yet mapped")
    @Test
    public void testChineseFactored()
        throws Exception
    {
        JCas jcas = runTest("zh", "factored", "我们需要一个非常复杂的句子例如其中包含许多成分和尽可能的依赖。");

        String[] constituentMapped = new String[] { "ADJP 6,10", "ADJP 8,10", "ADVP 13,15",
                "ADVP 24,27", "ADVP 6,8", "NP 0,2", "NP 11,13", "NP 15,17", "NP 19,23", "NP 21,23",
                "NP 4,13", "QP 19,21", "QP 4,6", "ROOT 0,31", "VP 17,23", "VP 17,30", "VP 2,13",
                "VP 24,30", "VP 28,30", "X 0,13", "X 0,31", "X 13,30", "X 24,28", "X 6,11" };

        String[] constituentOriginal = new String[] { "ADJP 6,10", "ADJP 8,10", "ADVP 13,15",
                "ADVP 24,27", "ADVP 6,8", "DNP 6,11", "DVP 24,28", "IP 0,13", "IP 0,31",
                "IP 13,30", "NP 0,2", "NP 11,13", "NP 15,17", "NP 19,23", "NP 21,23", "NP 4,13",
                "QP 19,21", "QP 4,6", "ROOT 0,31", "VP 17,23", "VP 17,30", "VP 2,13", "VP 24,30",
                "VP 28,30" };

        String[] dependencies = new String[] { 
                "ADVMOD(advmod)[13,15] D(例如)[13,15] G(包含)[17,19]",
                "ADVMOD(advmod)[6,8] D(非常)[6,8] G(复杂)[8,10]",
                "CC(cc)[23,24] D(和)[23,24] G(包含)[17,19]",
                "CONJ(conj)[28,30] D(依赖)[28,30] G(包含)[17,19]",
                "DEP(dep)[17,19] D(包含)[17,19] G(需要)[2,4]",
                "DOBJ(dobj)[11,13] D(句子)[11,13] G(需要)[2,4]",
                "DOBJ(dobj)[21,23] D(成分)[21,23] G(包含)[17,19]",
                "Dependency(assm)[10,11] D(的)[10,11] G(复杂)[8,10]",
                "Dependency(assmod)[8,10] D(复杂)[8,10] G(句子)[11,13]",
                "Dependency(dvpm)[27,28] D(的)[27,28] G(尽可能)[24,27]",
                "Dependency(dvpmod)[24,27] D(尽可能)[24,27] G(依赖)[28,30]",
                "Dependency(nummod)[19,21] D(许多)[19,21] G(成分)[21,23]",
                "Dependency(nummod)[4,6] D(一个)[4,6] G(句子)[11,13]",
                "NSUBJ(nsubj)[0,2] D(我们)[0,2] G(需要)[2,4]",
                "NSUBJ(nsubj)[15,17] D(其中)[15,17] G(包含)[17,19]" };

        String[] posMapped = new String[] { "PR", "V", "CARD", "ADJ", "O", "O", "NN", "ADJ", "NN",
                "V", "CARD", "NN", "CONJ", "ADJ", "O", "V", "PUNC" };

        String[] posOriginal = new String[] { "PN", "VV", "CD", "AD", "JJ", "DEG", "NN", "AD",
                "NN", "VV", "CD", "NN", "CC", "AD", "DEV", "VV", "PU" };

        String pennTree = "(ROOT (IP (IP (NP (PN 我们)) (VP (VV 需要) (NP (QP (CD 一个)) (DNP "
                + "(ADJP (ADVP (AD 非常)) (ADJP (JJ 复杂))) (DEG 的)) (NP (NN 句子))))) (IP (ADVP "
                + "(AD 例如)) (NP (NN 其中)) (VP (VP (VV 包含) (NP (QP (CD 许多)) (NP (NN 成分)))) "
                + "(CC 和) (VP (DVP (ADVP (AD 尽可能)) (DEV 的)) (VP (VV 依赖))))) (PU 。)))";

        String[] posTags = new String[] { ".$$.", "AD", "AS", "BA", "CC", "CD", "CS", "DEC", "DEG",
                "DER", "DEV", "DT", "ETC", "FRAG", "FW", "IJ", "JJ", "LB", "LC", "M", "MSP", "NN",
                "NR", "NT", "OD", "ON", "P", "PN", "PU", "SB", "SP", "URL", "VA", "VC", "VE", "VV",
                "X" };

        String[] constituentTags = new String[] { "ADJP", "ADVP", "CLP", "CP", "DFL", "DNP", "DP",
                "DVP", "FLR", "INC", "INTJ", "IP", "LCP", "LST", "NP", "PP", "PRN", "QP", "ROOT",
                "UCP", "VCD", "VCP", "VNV", "VP", "VPT", "VRD", "VSB", "WHPP" };

        String[] unmappedPos = new String[] { ".$$.", "AS", "BA", "CS", "DEC", "DEG", "DER", "DEV",
                "ETC", "FRAG", "FW", "IJ", "JJ", "LB", "LC", "M", "MSP", "OD", "ON", "SB", "SP",
                "URL", "VC", "X" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertPennTree(pennTree, selectSingle(jcas, PennTree.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "ctb", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "ctb", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Constituent.class, null, constituentTags, jcas);
    }

    @Ignore("Currently fails an assertion in StanfordAnnotator:188 - need to investigate")
    @Test
    public void testArabicFactored()
        throws Exception
    {
        JCas jcas = runTest("ar", "factored",
                "نحن بحاجة إلى مثال على جملة معقدة جدا، والتي تحتوي على مكونات مثل العديد من والتبعيات وقت ممكن.");

        String[] constituentMapped = new String[] { "NP 0,1", "ROOT 0,1" };

        String[] constituentOriginal = new String[] { "NP 0,1", "ROOT 0,1" };

        String[] dependencies = new String[] {};

        String[] posMapped = new String[] { "POS", "POS" };

        String[] posOriginal = new String[] { "NN", "NN" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertConstituents(constituentMapped, constituentOriginal,
                select(jcas, Constituent.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
    }

    /**
     * This tests whether a complete syntax tree can be recreated from the annotations without any
     * loss. Consequently, all links to children should be correct. (This makes no assertions about
     * the parent-links, because they are not used for the recreation)
     *
     * @throws Exception
     */
    @Test
    public void testEnglishSyntaxTreeReconstruction()
        throws Exception
    {
        JCas jcas = runTest("en", "factored", documentEnglish);

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

    /**
     * Setup CAS to test parser for the English language (is only called once if an English test is
     * run)
     */
    private JCas runTest(String aLanguage, String aVariant, String aText, Object... aExtraParams)
        throws Exception
    {
        AnalysisEngineDescription segmenter;

        if ("zh".equals(aLanguage)) {
            segmenter = createEngineDescription(LanguageToolSegmenter.class);
        }
        else {
            segmenter = createEngineDescription(StanfordSegmenter.class);
        }
        // setup English
        Object[] params = new Object[] {
                StanfordParser.PARAM_VARIANT, aVariant,
                StanfordParser.PARAM_PRINT_TAGSET, true,
                StanfordParser.PARAM_WRITE_CONSTITUENT, true,
                StanfordParser.PARAM_WRITE_DEPENDENCY, true,
                StanfordParser.PARAM_WRITE_PENN_TREE, true,
                StanfordParser.PARAM_WRITE_POS, true};
        params = ArrayUtils.addAll(params, aExtraParams);
        AnalysisEngineDescription parser = createEngineDescription(StanfordParser.class, params);

        AnalysisEngine engine = createEngine(createEngineDescription(segmenter, parser));

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage(aLanguage);
        jcas.setDocumentText(aText);
        engine.process(jcas);

        return jcas;
    }

    /**
     * Setup CAS to test parser for the English language (is only called once if an English test is
     * run)
     */
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
}
