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
package de.tudarmstadt.ukp.dkpro.core.corenlp;

import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertDependencies;
import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertTagset;
import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertTagsetMapping;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.testing.AssumeResource;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class CoreNlpDependencyParserTest
{
    private static final String[] GERMAN_POS_TAGS = { "$,", "$.", "$[", "ADJA", "ADJD", "ADV",
            "APPO", "APPR", "APPRART", "APZR", "ART", "CARD", "FM", "ITJ", "KOKOM", "KON", "KOUI",
            "KOUS", "NE", "NN", "PDAT", "PDS", "PIAT", "PIDAT", "PIS", "PPER", "PPOSAT", "PPOSS",
            "PRELAT", "PRELS", "PRF", "PROAV", "PTKA", "PTKANT", "PTKNEG", "PTKVZ", "PTKZU", "PWAT",
            "PWAV", "PWS", "TRUNC", "VAFIN", "VAIMP", "VAINF", "VAPP", "VMFIN", "VMINF", "VMPP",
            "VVFIN", "VVIMP", "VVINF", "VVIZU", "VVPP", "XY" };
    
    private static final String[] STANFORD_DEPENDENCY_TAGS = { "acomp", "advcl", "advmod", "amod",
            "appos", "aux", "auxpass", "cc", "ccomp", "conj", "cop", "csubj", "csubjpass", "dep",
            "det", "discourse", "dobj", "expl", "iobj", "mark", "mwe", "neg", "nn", "npadvmod",
            "nsubj", "nsubjpass", "num", "number", "parataxis", "pcomp", "pobj", "poss",
            "possessive", "preconj", "predet", "prep", "prt", "punct", "quantmod", "rcmod", "root",
            "tmod", "vmod", "xcomp" };

    private static final String[] UNIVERSAL_DEPENDENCY_TAGS = { "acl", "acl:relcl", "advcl",
            "advmod", "amod", "appos", "aux", "auxpass", "case", "cc", "cc:preconj", "ccomp",
            "compound", "compound:prt", "conj", "cop", "csubj", "csubjpass", "dep", "det",
            "det:predet", "discourse", "dobj", "expl", "iobj", "list", "mark", "mwe", "neg",
            "nmod", "nmod:npmod", "nmod:poss", "nmod:tmod", "nsubj", "nsubjpass", "nummod",
            "parataxis", "punct", "root", "xcomp" };

    private static final String[] PTB_POS_TAGS = { "#", "$", "''", ",", "-LRB-", "-RRB-", ".", ":",
            "CC", "CD", "DT", "EX", "FW", "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP", "NNPS",
            "NNS", "PDT", "POS", "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM", "TO", "UH", "VB",
            "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };

    private static final String[] UNIVERSAL_POS_TAGS = { "ADJ", "ADP", "ADV", "AUX", "CONJ", "DET",
            "INTJ", "NOUN", "NUM", "PART", "PRON", "PROPN", "PUNCT", "SCONJ", "SYM", "VERB", "X" };

    private static final String[] CORENLP34_POS_TAGS = { "A", "ADJ", "ADJWH", "ADV", "ADVWH", "C",
            "CC", "CL", "CLO", "CLR", "CLS", "CS", "DET", "DETWH", "ET", "I", "N", "NC", "NPP", "P",
            "PREF", "PRO", "PROREL", "PROWH", "PUNC", "V", "VIMP", "VINF", "VPP", "VPR", "VS" };

    @Test
    public void testEnglishStanfordDependencies()
        throws Exception
    {
        JCas jcas = runTest("en", "sd", "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String[] dependencies = {
                "[  0,  2]NSUBJ(nsubj,basic) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root,basic) D[3,7](need) G[3,7](need)",
                "[  8,  9]DET(det,basic) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]ADVMOD(advmod,basic) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]AMOD(amod,basic) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]NN(nn,basic) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]DOBJ(dobj,basic) D[35,43](sentence) G[3,7](need)",
                "[ 44, 45]PUNCT(punct,basic) D[44,45](,) G[35,43](sentence)",
                "[ 46, 51]NSUBJ(nsubj,basic) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]RCMOD(rcmod,basic) D[52,60](contains) G[35,43](sentence)",
                "[ 64, 68]AMOD(amod,basic) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]PREP(prep_as,basic) D[69,81](constituents) G[52,60](contains)",
                "[ 86, 98]CONJ(conj_and,basic) D[86,98](dependencies) G[69,81](constituents)",
                "[102,110]PREP(prep_as,basic) D[102,110](possible) G[69,81](constituents)",
                "[111,112]PUNCT(punct,basic) D[111,112](.) G[3,7](need)" };

        String[] unmappedDep = {};

        assertDependencies(dependencies, select(jcas, Dependency.class));
        assertTagset(CoreNlpPosTagger.class, POS.class, "ptb", PTB_POS_TAGS, jcas);
        assertTagset(CoreNlpDependencyParser.class, POS.class, "ptb", PTB_POS_TAGS, jcas);
        assertTagset(CoreNlpDependencyParser.class, Dependency.class, "stanford341",
                STANFORD_DEPENDENCY_TAGS, jcas);
        assertTagsetMapping(Dependency.class, "stanford341", unmappedDep, jcas);
    }

    @Test
    public void testEnglishUniversalDependencies()
        throws Exception
    {
        JCas jcas = runTest("en", "ud", "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String[] dependencies = {
                "[  0,  2]NSUBJ(nsubj,basic) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root,basic) D[3,7](need) G[3,7](need)",
                "[  8,  9]DET(det,basic) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]ADVMOD(advmod,basic) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]AMOD(amod,basic) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]NN(compound,basic) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]DOBJ(dobj,basic) D[35,43](sentence) G[3,7](need)",
                "[ 44, 45]PUNCT(punct,basic) D[44,45](,) G[35,43](sentence)",
                "[ 46, 51]NSUBJ(nsubj,basic) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]Dependency(acl:relcl,basic) D[52,60](contains) G[35,43](sentence)",
                "[ 61, 63]PREP(case,basic) D[61,63](as) G[69,81](constituents)",
                "[ 64, 68]AMOD(amod,basic) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]Dependency(nmod:as,basic) D[69,81](constituents) G[52,60](contains)",
                "[ 82, 85]CC(cc,basic) D[82,85](and) G[69,81](constituents)",
                "[ 86, 98]CONJ(conj:and,basic) D[86,98](dependencies) G[69,81](constituents)",
                "[ 99,101]PREP(case,basic) D[99,101](as) G[102,110](possible)",
                "[102,110]Dependency(acl,basic) D[102,110](possible) G[69,81](constituents)",
                "[111,112]PUNCT(punct,basic) D[111,112](.) G[3,7](need)" };

        String[] unmappedDep = { "acl:relcl", "cc:preconj", "compound:prt", "det:predet",
                "nmod:npmod", "nmod:poss", "nmod:tmod" };

        assertDependencies(dependencies, select(jcas, Dependency.class));
        assertTagset(CoreNlpPosTagger.class, POS.class, "ptb", PTB_POS_TAGS, jcas);
        assertTagset(CoreNlpDependencyParser.class, POS.class, "ptb", PTB_POS_TAGS, jcas);
        assertTagset(CoreNlpDependencyParser.class, Dependency.class, "universal",
                UNIVERSAL_DEPENDENCY_TAGS, jcas);
        assertTagsetMapping(Dependency.class, "universal", unmappedDep, jcas);
    }

    @Test
    public void testEnglishWsjSd()
        throws Exception
    {
        JCas jcas = runTest("en", "wsj-sd", "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String[] dependencies = {
                "[  0,  2]NSUBJ(nsubj,basic) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root,basic) D[3,7](need) G[3,7](need)",
                "[  8,  9]DET(det,basic) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]ADVMOD(advmod,basic) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]AMOD(amod,basic) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]NN(nn,basic) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]DOBJ(dobj,basic) D[35,43](sentence) G[3,7](need)",
                "[ 44, 45]PUNCT(punct,basic) D[44,45](,) G[35,43](sentence)",
                "[ 46, 51]NSUBJ(nsubj,basic) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]RCMOD(rcmod,basic) D[52,60](contains) G[35,43](sentence)",
                "[ 61, 63]PREP(prep,basic) D[61,63](as) G[52,60](contains)",
                "[ 64, 68]AMOD(amod,basic) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]POBJ(pobj,basic) D[69,81](constituents) G[61,63](as)",
                "[ 82, 85]CC(cc,basic) D[82,85](and) G[69,81](constituents)",
                "[ 86, 98]CONJ(conj:and,basic) D[86,98](dependencies) G[69,81](constituents)",
                "[ 99,101]PREP(prep,basic) D[99,101](as) G[69,81](constituents)",
                "[102,110]POBJ(pobj,basic) D[102,110](possible) G[99,101](as)",
                "[111,112]PUNCT(punct,basic) D[111,112](.) G[3,7](need)" };

        String[] unmappedDep = {};

        assertDependencies(dependencies, select(jcas, Dependency.class));
        assertTagset(CoreNlpPosTagger.class, POS.class, "ptb", PTB_POS_TAGS, jcas);
        assertTagset(CoreNlpDependencyParser.class, POS.class, "ptb", PTB_POS_TAGS, jcas);
        assertTagset(CoreNlpDependencyParser.class, Dependency.class, "stanford341",
                STANFORD_DEPENDENCY_TAGS, jcas);
        assertTagsetMapping(Dependency.class, "stanford341", unmappedDep, jcas);
    }


    @Test
    public void testEnglishWsjUd()
        throws Exception
    {
        JCas jcas = runTest("en", "wsj-ud", "We need a very complicated example sentence , which "
                + "contains as many constituents and dependencies as possible .");

        String[] dependencies = {
                "[  0,  2]NSUBJ(nsubj,basic) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root,basic) D[3,7](need) G[3,7](need)",
                "[  8,  9]DET(det,basic) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]ADVMOD(advmod,basic) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]AMOD(amod,basic) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]NN(compound,basic) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]DOBJ(dobj,basic) D[35,43](sentence) G[3,7](need)",
                "[ 44, 45]PUNCT(punct,basic) D[44,45](,) G[35,43](sentence)",
                "[ 46, 51]NSUBJ(nsubj,basic) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]Dependency(acl:relcl,basic) D[52,60](contains) G[35,43](sentence)",
                "[ 61, 63]PREP(case,basic) D[61,63](as) G[69,81](constituents)",
                "[ 64, 68]AMOD(amod,basic) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]Dependency(nmod:as,basic) D[69,81](constituents) G[52,60](contains)",
                "[ 82, 85]CC(cc,basic) D[82,85](and) G[69,81](constituents)",
                "[ 86, 98]CONJ(conj:and,basic) D[86,98](dependencies) G[69,81](constituents)",
                "[ 99,101]PREP(case,basic) D[99,101](as) G[102,110](possible)",
                "[102,110]Dependency(acl,basic) D[102,110](possible) G[69,81](constituents)",
                "[111,112]PUNCT(punct,basic) D[111,112](.) G[3,7](need)" };

        String[] depTags = { "acl", "acl:relcl", "advcl", "advmod", "amod", "appos", "aux",
                "auxpass", "case", "cc", "cc:preconj", "ccomp", "compound", "compound:prt", "conj",
                "cop", "csubj", "csubjpass", "dep", "det", "det:predet", "discourse", "dobj",
                "expl", "iobj", "mark", "mwe", "neg", "nmod", "nmod:npmod", "nmod:poss",
                "nmod:tmod", "nsubj", "nsubjpass", "nummod", "parataxis", "punct", "root",
                "xcomp" };
        
        String[] unmappedDep = { "acl:relcl", "cc:preconj", "compound:prt", "det:predet",
                "nmod:npmod", "nmod:poss", "nmod:tmod" };

        assertDependencies(dependencies, select(jcas, Dependency.class));
        assertTagset(CoreNlpPosTagger.class, POS.class, "ptb", PTB_POS_TAGS, jcas);
        assertTagset(CoreNlpDependencyParser.class, POS.class, "ptb", PTB_POS_TAGS, jcas);
        assertTagset(CoreNlpDependencyParser.class, Dependency.class, "universal", depTags, jcas);
        assertTagsetMapping(Dependency.class, "universal", unmappedDep, jcas);
    }

    @Test
    public void testFrenchUniversalDependencies()
        throws Exception
    {
        JCas jcas = runTest("fr", "ud", "Nous avons besoin d' une phrase par exemple très "
                + "compliqué , qui contient des constituants que de nombreuses dépendances et que "
                + "possible .");

        String[] dependencies = {
                "[  0,  4]ROOT(root,basic) D[0,4](Nous) G[0,4](Nous)",
                "[  5, 10]Dependency(nmod,basic) D[5,10](avons) G[0,4](Nous)",
                "[ 11, 17]DOBJ(dobj,basic) D[11,17](besoin) G[5,10](avons)",
                "[ 18, 20]PREP(case,basic) D[18,20](d') G[25,31](phrase)",
                "[ 21, 24]DET(det,basic) D[21,24](une) G[25,31](phrase)",
                "[ 25, 31]Dependency(nmod:d',basic) D[25,31](phrase) G[11,17](besoin)",
                "[ 32, 35]ADVMOD(advmod,basic) D[32,35](par) G[25,31](phrase)",
                "[ 36, 43]MWE(mwe,basic) D[36,43](exemple) G[32,35](par)",
                "[ 44, 48]ADVMOD(advmod,basic) D[44,48](très) G[49,58](compliqué)",
                "[ 49, 58]AMOD(amod,basic) D[49,58](compliqué) G[36,43](exemple)",
                "[ 59, 60]DEP(dep,basic) D[59,60](,) G[49,58](compliqué)",
                "[ 61, 64]NSUBJ(nsubj,basic) D[61,64](qui) G[65,73](contient)",
                "[ 65, 73]Dependency(acl:relcl,basic) D[65,73](contient) G[59,60](,)",
                "[ 74, 77]DET(det,basic) D[74,77](des) G[78,90](constituants)",
                "[ 78, 90]DOBJ(dobj,basic) D[78,90](constituants) G[65,73](contient)",
                "[ 91, 94]PREP(case,basic) D[91,94](que) G[109,120](dépendances)",
                "[ 95, 97]DET(det,basic) D[95,97](de) G[109,120](dépendances)",
                "[ 98,108]AMOD(amod,basic) D[98,108](nombreuses) G[109,120](dépendances)",
                "[109,120]DEP(dep,basic) D[109,120](dépendances) G[78,90](constituants)",
                "[121,123]CC(cc,basic) D[121,123](et) G[109,120](dépendances)",
                "[124,127]COP(cop,basic) D[124,127](que) G[128,136](possible)",
                "[128,136]CCOMP(ccomp,basic) D[128,136](possible) G[121,123](et)",
                "[137,138]PUNCT(punct,basic) D[137,138](.) G[128,136](possible)" };

        String[] depTags = { "acl", "acl:relcl", "advcl", "advmod", "amod", "appos", "aux",
                "auxpass", "case", "cc", "ccomp", "compound", "conj", "cop", "csubj", "dep", "det",
                "discourse", "dislocated", "dobj", "expl", "foreign", "goeswith", "iobj", "mark",
                "mwe", "name", "neg", "nmod", "nmod:poss", "nsubj", "nsubjpass", "nummod",
                "parataxis", "punct", "remnant", "reparandum", "root", "vocative", "xcomp" };
        
        String[] unmappedDep = { "acl:relcl", "nmod:poss" };

        assertDependencies(dependencies, select(jcas, Dependency.class));
        assertTagset(CoreNlpPosTagger.class, POS.class, "corenlp34", CORENLP34_POS_TAGS, jcas);
        assertTagset(CoreNlpDependencyParser.class, POS.class, "universal", UNIVERSAL_POS_TAGS,
                jcas);
        assertTagset(CoreNlpDependencyParser.class, Dependency.class, "universal", depTags, jcas);
        assertTagsetMapping(Dependency.class, "universal", unmappedDep, jcas);
    }

    @Test
    public void testGermanUniversalDependencies()
        throws Exception
    {
        JCas jcas = runTest("de", "ud", "Wir brauchen ein sehr kompliziertes Beispiel , welches "
                + "möglichst viele Konstituenten und Dependenzen beinhaltet .");

        String[] dependencies = {
                "[  0,  3]NSUBJ(nsubj,basic) D[0,3](Wir) G[4,12](brauchen)",
                "[  4, 12]ROOT(root,basic) D[4,12](brauchen) G[4,12](brauchen)",
                "[ 13, 16]DET(det,basic) D[13,16](ein) G[36,44](Beispiel)",
                "[ 17, 21]ADVMOD(advmod,basic) D[17,21](sehr) G[22,35](kompliziertes)",
                "[ 22, 35]AMOD(amod,basic) D[22,35](kompliziertes) G[36,44](Beispiel)",
                "[ 36, 44]DOBJ(dobj,basic) D[36,44](Beispiel) G[4,12](brauchen)",
                "[ 45, 46]PUNCT(punct,basic) D[45,46](,) G[4,12](brauchen)",
                "[ 47, 54]NSUBJ(nsubj,basic) D[47,54](welches) G[101,111](beinhaltet)",
                "[ 55, 64]ADVMOD(advmod,basic) D[55,64](möglichst) G[65,70](viele)",
                "[ 65, 70]AMOD(amod,basic) D[65,70](viele) G[71,84](Konstituenten)",
                "[ 71, 84]DOBJ(dobj,basic) D[71,84](Konstituenten) G[101,111](beinhaltet)",
                "[ 85, 88]CC(cc,basic) D[85,88](und) G[71,84](Konstituenten)",
                "[ 89,100]CONJ(conj:und,basic) D[89,100](Dependenzen) G[71,84](Konstituenten)",
                "[101,111]Dependency(acl,basic) D[101,111](beinhaltet) G[4,12](brauchen)",
                "[112,113]PUNCT(punct,basic) D[112,113](.) G[4,12](brauchen)" };

        String[] depTags = { "acl", "advcl", "advmod", "amod", "appos", "aux", "auxpass", "case",
                "cc", "ccomp", "compound", "conj", "cop", "csubj", "csubjpass", "dep", "det",
                "dobj", "expl", "iobj", "mark", "mwe", "name", "neg", "nmod", "nmod:poss", "nsubj",
                "nsubjpass", "nummod", "parataxis", "punct", "root", "xcomp" }; 
        
        String[] unmappedDep = { "nmod:poss" };

        String[] depParserPosTags = { "$,", "$.", "$[", "ADJA", "ADJD", "ADV", "APPO", "APPR",
                "APPRART", "APZR", "ART", "CARD", "FM", "ITJ", "KOKOM", "KON", "KOUI", "KOUS", "NE",
                "NN", "PDAT", "PDS", "PIAT", "PIDAT", "PIS", "PPER", "PPOSAT", "PRELAT", "PRELS",
                "PRF", "PROAV", "PTKA", "PTKANT", "PTKNEG", "PTKVZ", "PTKZU", "PWAT", "PWAV", "PWS",
                "TRUNC", "VAFIN", "VAIMP", "VAINF", "VAPP", "VMFIN", "VMINF", "VVFIN", "VVIMP",
                "VVINF", "VVIZU", "VVPP", "XY" };
        
        assertDependencies(dependencies, select(jcas, Dependency.class));
        assertTagset(CoreNlpPosTagger.class, POS.class, "stts", GERMAN_POS_TAGS, jcas);
        assertTagset(CoreNlpDependencyParser.class, POS.class, "stts", depParserPosTags, jcas);
        assertTagset(CoreNlpDependencyParser.class, Dependency.class, "universal", depTags, jcas);
        assertTagsetMapping(Dependency.class, "universal", unmappedDep, jcas);
    }
    
    @Test
    public void testChineseCtbConllDependencies()
        throws Exception
    {
        JCas jcas = runTest("zh", "ctb-conll", 
                "我们 需要 一个 非常 复杂 的 句子 例如 其中 包含 许多 成分 和 尽可能 的 依赖 。");

        String[] dependencies = {
                "[  0,  2]Dependency(SUB,basic) D[0,2](我们) G[3,5](需要)",
                "[  3,  5]ROOT(root,basic) D[3,5](需要) G[3,5](需要)",
                "[  6,  8]Dependency(AMOD,basic) D[6,8](一个) G[12,14](复杂)",
                "[  9, 11]Dependency(AMOD,basic) D[9,11](非常) G[12,14](复杂)",
                "[ 12, 14]Dependency(DEP,basic) D[12,14](复杂) G[15,16](的)",
                "[ 15, 16]Dependency(NMOD,basic) D[15,16](的) G[17,19](句子)",
                "[ 17, 19]Dependency(OBJ,basic) D[17,19](句子) G[3,5](需要)",
                "[ 20, 22]Dependency(VMOD,basic) D[20,22](例如) G[26,28](包含)",
                "[ 23, 25]Dependency(SUB,basic) D[23,25](其中) G[26,28](包含)",
                "[ 26, 28]Dependency(VMOD,basic) D[26,28](包含) G[3,5](需要)",
                "[ 29, 31]Dependency(NMOD,basic) D[29,31](许多) G[32,34](成分)",
                "[ 32, 34]Dependency(SUB,basic) D[32,34](成分) G[43,45](依赖)",
                "[ 35, 36]Dependency(VMOD,basic) D[35,36](和) G[43,45](依赖)",
                "[ 37, 40]Dependency(DEP,basic) D[37,40](尽可能) G[41,42](的)",
                "[ 41, 42]Dependency(VMOD,basic) D[41,42](的) G[43,45](依赖)",
                "[ 43, 45]Dependency(VMOD,basic) D[43,45](依赖) G[26,28](包含)",
                "[ 46, 47]Dependency(P,basic) D[46,47](。) G[3,5](需要)" };

        String[] depTags = { "AMOD", "DEP", "NMOD", "OBJ", "P", "PMOD", "PRD", "ROOT", "SBAR",
                "SUB", "VC", "VMOD" };
        
        String[] posTags = { "AD", "AS", "BA", "CC", "CD", "CS", "DEC", "DEG", "DER", "DEV", "DT",
                "ETC", "FW", "IJ", "JJ", "LB", "LC", "M", "MSP", "NN", "NR", "NT", "OD", "ON", "P",
                "PN", "PU", "SB", "SP", "URL", "VA", "VC", "VE", "VV", "X" };
        
        String[] unmappedDep = {};

        assertDependencies(dependencies, select(jcas, Dependency.class));
        assertTagset(CoreNlpPosTagger.class, POS.class, "ctb", posTags, jcas);
        // There are some minor differences between the tags produced by the POS tagger and the
        // tags expected by the parser model. We need a better test here that makes these
        // differences
        // more visible and at the same time doesn't fail.
        // AssertAnnotations.assertTagset(CoreNlpDependencyParser.class, POS.class, "ctb", posTags,
        // jcas);
        assertTagset(CoreNlpDependencyParser.class, Dependency.class, "conll", depTags, jcas);
        assertTagsetMapping(CoreNlpDependencyParser.class, Dependency.class, "conll", unmappedDep,
                jcas);
    }

    @Test
    public void testChinesePtbConllDependencies()
        throws Exception
    {
        JCas jcas = runTest("zh", "ptb-conll", 
                "我们 需要 一个 非常 复杂 的 句子 例如 其中 包含 许多 成分 和 尽可能 的 依赖 。");

        // This output is bogus because the tagger we use here produced ctb tags and the model 
        // expects ptb tags. However, I didn't find any pos tagger model for chinese that produces
        // the ptb tags...
        String[] dependencies = {
                "[  0,  2]ROOT(root,basic) D[0,2](我们) G[0,2](我们)",
                "[  3,  5]Dependency(COORD,basic) D[3,5](需要) G[0,2](我们)",
                "[  6,  8]Dependency(COORD,basic) D[6,8](一个) G[3,5](需要)",
                "[  9, 11]Dependency(COORD,basic) D[9,11](非常) G[6,8](一个)",
                "[ 12, 14]Dependency(COORD,basic) D[12,14](复杂) G[9,11](非常)",
                "[ 15, 16]Dependency(COORD,basic) D[15,16](的) G[12,14](复杂)",
                "[ 17, 19]Dependency(COORD,basic) D[17,19](句子) G[15,16](的)",
                "[ 20, 22]Dependency(COORD,basic) D[20,22](例如) G[17,19](句子)",
                "[ 23, 25]Dependency(COORD,basic) D[23,25](其中) G[20,22](例如)",
                "[ 26, 28]Dependency(COORD,basic) D[26,28](包含) G[23,25](其中)",
                "[ 29, 31]Dependency(NMOD,basic) D[29,31](许多) G[32,34](成分)",
                "[ 32, 34]Dependency(VMOD,basic) D[32,34](成分) G[41,42](的)",
                "[ 35, 36]Dependency(COORD,basic) D[35,36](和) G[32,34](成分)",
                "[ 37, 40]Dependency(CONJ,basic) D[37,40](尽可能) G[35,36](和)",
                "[ 41, 42]Dependency(COORD,basic) D[41,42](的) G[26,28](包含)",
                "[ 43, 45]Dependency(NMOD,basic) D[43,45](依赖) G[46,47](。)",
                "[ 46, 47]Dependency(VMOD,basic) D[46,47](。) G[41,42](的)" };

        String[] depTags = { "AMOD", "APPO", "CONJ", "COORD", "DEP", "IM", "NAME", "NMOD", "P",
                "PMOD", "PRN", "PRT", "ROOT", "SUB", "SUFFIX", "VC", "VMOD" };
        
        String[] posTags = { "AD", "AS", "BA", "CC", "CD", "CS", "DEC", "DEG", "DER", "DEV", "DT",
                "ETC", "FW", "IJ", "JJ", "LB", "LC", "M", "MSP", "NN", "NR", "NT", "OD", "ON", "P",
                "PN", "PU", "SB", "SP", "URL", "VA", "VC", "VE", "VV", "X" };
        
        String[] unmappedDep = {};

        assertDependencies(dependencies, select(jcas, Dependency.class));
        assertTagset(CoreNlpPosTagger.class, POS.class, "ctb", posTags, jcas);
        // There are some minor differences between the tags produced by the POS tagger and the
        // tags expected by the parser model. We need a better test here that makes these
        // differences
        // more visible and at the same time doesn't fail.
        // AssertAnnotations.assertTagset(CoreNlpDependencyParser.class, POS.class, "ctb", posTags,
        // jcas);
        assertTagset(CoreNlpDependencyParser.class, Dependency.class, "conll2008", depTags, jcas);
        assertTagsetMapping(CoreNlpDependencyParser.class, Dependency.class, "conll2008",
                unmappedDep, jcas);
    }

    @Test
    public void testChineseUniversalDependencies()
        throws Exception
    {
        JCas jcas = runTest("zh", "ud", 
                "我们 需要 一个 非常 复杂 的 句子 例如 其中 包含 许多 成分 和 尽可能 的 依赖 。");

        String[] dependencies = {
                "[  0,  2]NSUBJ(nsubj,basic) D[0,2](我们) G[3,5](需要)",
                "[  3,  5]ROOT(root,basic) D[3,5](需要) G[3,5](需要)",
                "[  6,  8]DEP(dep,basic) D[6,8](一个) G[17,19](句子)",
                "[  9, 11]ADVMOD(advmod,basic) D[9,11](非常) G[12,14](复杂)",
                "[ 12, 14]AMOD(amod,basic) D[12,14](复杂) G[17,19](句子)",
                "[ 15, 16]MARK(mark,basic) D[15,16](的) G[12,14](复杂)",
                "[ 17, 19]NSUBJ(nsubj,basic) D[17,19](句子) G[26,28](包含)",
                "[ 20, 22]ADVMOD(advmod,basic) D[20,22](例如) G[26,28](包含)",
                "[ 23, 25]NSUBJ(nsubj,basic) D[23,25](其中) G[26,28](包含)",
                "[ 26, 28]CCOMP(ccomp,basic) D[26,28](包含) G[3,5](需要)",
                "[ 29, 31]DEP(dep,basic) D[29,31](许多) G[32,34](成分)",
                "[ 32, 34]CONJ(conj:和,basic) D[32,34](成分) G[43,45](依赖)",
                "[ 35, 36]CC(cc,basic) D[35,36](和) G[43,45](依赖)",
                "[ 37, 40]ADVMOD(advmod:dvp,basic) D[37,40](尽可能) G[43,45](依赖)",
                "[ 41, 42]MARK(mark,basic) D[41,42](的) G[37,40](尽可能)",
                "[ 43, 45]DOBJ(dobj,basic) D[43,45](依赖) G[26,28](包含)",
                "[ 46, 47]PUNCT(punct,basic) D[46,47](。) G[3,5](需要)" };

        String[] depTags = { "acl", "advcl:loc", "advmod", "advmod:dvp", "advmod:loc",
                "advmod:rcomp", "amod", "amod:ordmod", "appos", "aux:asp", "aux:ba", "aux:modal",
                "aux:prtmod", "auxpass", "case", "cc", "ccomp", "compound:nn", "compound:vc",
                "conj", "cop", "dep", "det", "discourse", "dobj", "erased", "etc", "mark",
                "mark:clf", "name", "neg", "nmod", "nmod:assmod", "nmod:poss", "nmod:prep",
                "nmod:range", "nmod:tmod", "nmod:topic", "nsubj", "nsubj:xsubj", "nsubjpass",
                "nummod", "parataxis:prnmod", "punct", "root", "xcomp" };
        
        String[] posTags = { "AD", "AS", "BA", "CC", "CD", "CS", "DEC", "DEG", "DER", "DEV", "DT",
                "ETC", "FW", "IJ", "JJ", "LB", "LC", "M", "MSP", "NN", "NR", "NT", "OD", "ON", "P",
                "PN", "PU", "SB", "SP", "URL", "VA", "VC", "VE", "VV", "X" };
        
        String[] unmappedDep = { "advcl:loc", "advmod:dvp", "advmod:loc", "advmod:rcomp",
                "amod:ordmod", "aux:asp", "aux:ba", "aux:modal", "aux:prtmod", "compound:nn",
                "compound:vc", "erased", "etc", "mark:clf", "nmod:assmod", "nmod:poss", "nmod:prep",
                "nmod:range", "nmod:tmod", "nmod:topic", "nsubj:xsubj", "parataxis:prnmod" };

        assertDependencies(dependencies, select(jcas, Dependency.class));
        assertTagset(CoreNlpPosTagger.class, POS.class, "ctb", posTags, jcas);
        // There are some minor differences between the tags produced by the POS tagger and the
        // tags expected by the parser model. We need a better test here that makes these
        // differences
        // more visible and at the same time doesn't fail.
        // AssertAnnotations.assertTagset(CoreNlpDependencyParser.class, POS.class, "ctb", posTags,
        // jcas);
        assertTagset(CoreNlpDependencyParser.class, Dependency.class, "universal", depTags, jcas);
        assertTagsetMapping(CoreNlpDependencyParser.class, Dependency.class, "universal",
                unmappedDep, jcas);
    }

    @Test
    public void testEnglishPtbConllDependencies()
        throws Exception
    {
        JCas jcas = runTest("en", "ptb-conll",
                "We need a very complicated example sentence , which "
                        + "contains as many constituents and dependencies as possible .");

        String[] dependencies = {
                "[  0,  2]Dependency(VMOD,basic) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(root,basic) D[3,7](need) G[3,7](need)",
                "[  8,  9]Dependency(NMOD,basic) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]Dependency(AMOD,basic) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]Dependency(NMOD,basic) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]Dependency(NMOD,basic) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]Dependency(VMOD,basic) D[35,43](sentence) G[3,7](need)",
                "[ 44, 45]Dependency(P,basic) D[44,45](,) G[35,43](sentence)",
                "[ 46, 51]Dependency(VMOD,basic) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]Dependency(NMOD,basic) D[52,60](contains) G[35,43](sentence)",
                "[ 61, 63]Dependency(VMOD,basic) D[61,63](as) G[52,60](contains)",
                "[ 64, 68]Dependency(NMOD,basic) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]Dependency(PMOD,basic) D[69,81](constituents) G[61,63](as)",
                "[ 82, 85]Dependency(COORD,basic) D[82,85](and) G[69,81](constituents)",
                "[ 86, 98]Dependency(CONJ,basic) D[86,98](dependencies) G[82,85](and)",
                "[ 99,101]Dependency(NMOD,basic) D[99,101](as) G[69,81](constituents)",
                "[102,110]Dependency(PMOD,basic) D[102,110](possible) G[99,101](as)",
                "[111,112]Dependency(P,basic) D[111,112](.) G[3,7](need)" };

        String[] depTags = { "AMOD", "APPO", "CONJ", "COORD", "DEP", "IM", "NAME", "NMOD", "P",
                "PMOD", "PRN", "PRT", "ROOT", "SUB", "SUFFIX", "VC", "VMOD" };

        String[] unmappedDep = {};

        String[] posTags = { "#", "$", "''", "(", ")", ",", ".", ":", "CC", "CD", "DT", "EX", "FW",
                "IN", "JJ", "JJR", "JJS", "LS", "MD", "NN", "NNP", "NNPS", "NNS", "PDT", "POS",
                "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "SYM", "TO", "UH", "VB", "VBD", "VBG",
                "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };

        assertDependencies(dependencies, select(jcas, Dependency.class));
        // There are some minor differences between the tags produced by the POS tagger and the
        // tags expected by the parser model. We need a better test here that makes these
        // differences
        // more visible and at the same time doesn't fail.
        // AssertAnnotations.assertTagset(CoreNlpPosTagger.class, POS.class, "ptb", PTB_POS_TAGS,
        // jcas);
        assertTagset(CoreNlpDependencyParser.class, POS.class, "ptb", posTags, jcas);
        assertTagset(Dependency.class, "conll", depTags, jcas);
        assertTagsetMapping(Dependency.class, "conll", unmappedDep, jcas);
    }
    
    private JCas runTest(String aLanguage, String aVariant, String aText, Object... aExtraParams)
        throws Exception
    {
        AssumeResource.assumeResource(CoreNlpDependencyParser.class, "depparser", aLanguage,
                aVariant);
        
        AggregateBuilder aggregate = new AggregateBuilder();
        
        aggregate.add(createEngineDescription(CoreNlpPosTagger.class));
        Object[] params = new Object[] {
                CoreNlpDependencyParser.PARAM_VARIANT, aVariant,
                CoreNlpDependencyParser.PARAM_PRINT_TAGSET, true};
        params = ArrayUtils.addAll(params, aExtraParams);
        aggregate.add(createEngineDescription(CoreNlpDependencyParser.class, params));

        return TestRunner.runTest(aggregate.createAggregateDescription(), aLanguage, aText);
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
