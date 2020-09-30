/*
 * Copyright 2007-2019
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.hunpos.HunPosTagger;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.AssumeResource;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.TagsetDescriptionStripper;
import org.dkpro.core.testing.TestRunner;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;

public class MateParserTest
{
    @Test
    public void testGerman()
        throws Exception
    {
        JCas jcas = runTest("de", "Wir brauchen ein sehr kompliziertes Beispiel , welches "
                + "möglichst viele Konstituenten und Dependenzen beinhaltet .");

        String[] dependencies = {
                "[  0,  3]Dependency(SB,basic) D[0,3](Wir) G[4,12](brauchen)",
                "[  4, 12]ROOT(--,basic) D[4,12](brauchen) G[4,12](brauchen)",
                "[ 13, 16]Dependency(NK,basic) D[13,16](ein) G[36,44](Beispiel)",
                "[ 17, 21]Dependency(MO,basic) D[17,21](sehr) G[22,35](kompliziertes)",
                "[ 22, 35]Dependency(NK,basic) D[22,35](kompliziertes) G[36,44](Beispiel)",
                "[ 36, 44]DOBJ(OA,basic) D[36,44](Beispiel) G[4,12](brauchen)",
                "[ 45, 46]Dependency(--,basic) D[45,46](,) G[36,44](Beispiel)",
                "[ 47, 54]Dependency(SB,basic) D[47,54](welches) G[101,111](beinhaltet)",
                "[ 55, 64]Dependency(MO,basic) D[55,64](möglichst) G[65,70](viele)",
                "[ 65, 70]Dependency(NK,basic) D[65,70](viele) G[71,84](Konstituenten)",
                "[ 71, 84]DOBJ(OA,basic) D[71,84](Konstituenten) G[101,111](beinhaltet)",
                "[ 85, 88]Dependency(CD,basic) D[85,88](und) G[71,84](Konstituenten)",
                "[ 89,100]CONJ(CJ,basic) D[89,100](Dependenzen) G[85,88](und)",
                "[101,111]Dependency(RC,basic) D[101,111](beinhaltet) G[36,44](Beispiel)",
                "[112,113]Dependency(--,basic) D[112,113](.) G[101,111](beinhaltet)" };

        String[] posTags = { "$(", "$,", "$.", "ADJA", "ADJD", "ADV", "APPO", "APPR", "APPRART",
                "APZR", "ART", "CARD", "END", "FM", "ITJ", "KOKOM", "KON", "KOUI", "KOUS", "MID",
                "NE", "NN", "NNE", "PDAT", "PDS", "PIAT", "PIS", "PPER", "PPOSAT", "PPOSS",
                "PRELAT", "PRELS", "PRF", "PROAV", "PTKA", "PTKANT", "PTKNEG", "PTKVZ", "PTKZU",
                "PWAT", "PWAV", "PWS", "STPOS", "STR", "TRUNC", "VAFIN", "VAIMP", "VAINF", "VAPP",
                "VMFIN", "VMINF", "VMPP", "VVFIN", "VVIMP", "VVINF", "VVIZU", "VVPP", "XY" };

        String[] dependencyTags = { "--", "AC", "ADC", "AG", "AMS", "APP", "AVC", "CC", "CD", "CJ",
                "CM", "CP", "CVC", "DA", "DM", "END", "EP", "JU", "MNR", "MO", "NG", "NK", "NMC",
                "OA", "OA2", "OC", "OG", "OP", "PAR", "PD", "PG", "PH", "PM", "PNC", "RC", "RE",
                "RS", "SB", "SBP", "SP", "SVP", "UC", "VO" };

        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "stts", posTags, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "negra", dependencyTags, jcas);
    }

    @Test
    public void testEnglish()
        throws Exception
    {
        JCas jcas = runTest("en", "We need a very complicated example sentence , which " +
                "contains as many constituents and dependencies as possible .");

        String[] dependencies = {
                "[  0,  2]Dependency(SBJ,basic) D[0,2](We) G[3,7](need)",
                "[  3,  7]ROOT(ROOT,basic) D[3,7](need) G[3,7](need)",
                "[  8,  9]Dependency(NMOD,basic) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]Dependency(AMOD,basic) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]Dependency(NMOD,basic) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]Dependency(NMOD,basic) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]Dependency(OBJ,basic) D[35,43](sentence) G[3,7](need)",
                "[ 44, 45]Dependency(P,basic) D[44,45](,) G[35,43](sentence)",
                "[ 46, 51]Dependency(SBJ,basic) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]Dependency(NMOD,basic) D[52,60](contains) G[35,43](sentence)",
                "[ 61, 63]Dependency(AMOD,basic) D[61,63](as) G[64,68](many)",
                "[ 64, 68]Dependency(NMOD,basic) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]Dependency(OBJ,basic) D[69,81](constituents) G[52,60](contains)",
                "[ 82, 85]Dependency(COORD,basic) D[82,85](and) G[69,81](constituents)",
                "[ 86, 98]Dependency(CONJ,basic) D[86,98](dependencies) G[82,85](and)",
                "[ 99,101]Dependency(NMOD,basic) D[99,101](as) G[69,81](constituents)",
                "[102,110]Dependency(PMOD,basic) D[102,110](possible) G[99,101](as)",
                "[111,112]Dependency(P,basic) D[111,112](.) G[3,7](need)" };

        String[] posTags = { "#", "$", "''", "(", ")", ",", ".", ":", "CC", "CD", "DT", "END",
                "EX", "FW", "HYPH", "IN", "JJ", "JJR", "JJS", "LS", "MD", "MID", "NIL", "NN",
                "NNP", "NNPS", "NNS", "PDT", "POS", "PRF", "PRP", "PRP$", "RB", "RBR", "RBS", "RP",
                "STPOS", "STR", "SYM", "TO", "UH", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT",
                "WP", "WP$", "WRB", "``" };

        String[] dependencyTags = { "ADV", "ADV-GAP", "AMOD", "AMOD-GAP", "APPO", "BNF", "CONJ",
                "COORD", "DEP", "DEP-GAP", "DIR", "DIR-GAP", "DIR-OPRD", "DIR-PRD", "DTV",
                "DTV-GAP", "END", "EXT", "EXT-GAP", "EXTR", "EXTR-GAP", "GAP-LGS", "GAP-LOC",
                "GAP-LOC-PRD", "GAP-MNR", "GAP-NMOD", "GAP-OBJ", "GAP-OPRD", "GAP-PMOD", "GAP-PRD",
                "GAP-PRP", "GAP-PUT", "GAP-SBJ", "GAP-SUB", "GAP-TMP", "GAP-VC", "HMOD", "HYPH",
                "IM", "LGS", "LOC", "LOC-MNR", "LOC-OPRD", "LOC-PRD", "LOC-TMP", "MNR", "MNR-PRD",
                "MNR-TMP", "NAME", "NMOD", "OBJ", "OPRD", "P", "PMOD", "POSTHON", "PRD", "PRD-PRP",
                "PRD-TMP", "PRN", "PRP", "PRT", "PUT", "ROOT", "SBJ", "SUB", "SUFFIX", "TITLE",
                "TMP", "VC", "VOC" };

        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "ptb", posTags, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "conll2008", dependencyTags, jcas);
    }

    @Test
    public void testFrench()
        throws Exception
    {
        JCas jcas = runTest("fr", "Nous avons besoin d'une phrase par exemple très "
                + "compliqué, qui contient des constituants que de nombreuses dépendances et que "
                + "possible .");

        String[] dependencies = {
                "[  0,  4]Dependency(suj,basic) D[0,4](Nous) G[5,10](avons)",
                "[  5, 10]ROOT(root,basic) D[5,10](avons) G[5,10](avons)",
                "[ 11, 17]Dependency(obj,basic) D[11,17](besoin) G[5,10](avons)",
                "[ 18, 23]Dependency(mod,basic) D[18,23](d'une) G[5,10](avons)",
                "[ 24, 30]Dependency(obj,basic) D[24,30](phrase) G[18,23](d'une)",
                "[ 31, 34]Dependency(dep,basic) D[31,34](par) G[24,30](phrase)",
                "[ 35, 42]Dependency(obj,basic) D[35,42](exemple) G[31,34](par)",
                "[ 43, 47]Dependency(mod,basic) D[43,47](très) G[48,58](compliqué,)",
                "[ 48, 58]Dependency(mod,basic) D[48,58](compliqué,) G[35,42](exemple)",
                "[ 59, 62]Dependency(suj,basic) D[59,62](qui) G[63,71](contient)",
                "[ 63, 71]Dependency(mod_rel,basic) D[63,71](contient) G[24,30](phrase)",
                "[ 72, 75]Dependency(det,basic) D[72,75](des) G[76,88](constituants)",
                "[ 76, 88]Dependency(obj,basic) D[76,88](constituants) G[63,71](contient)",
                "[ 89, 92]Dependency(dep,basic) D[89,92](que) G[76,88](constituants)",
                "[ 93, 95]Dependency(det,basic) D[93,95](de) G[107,118](dépendances)",
                "[ 96,106]Dependency(mod,basic) D[96,106](nombreuses) G[107,118](dépendances)",
                "[107,118]Dependency(obj,basic) D[107,118](dépendances) G[89,92](que)",
                "[119,121]Dependency(coord,basic) D[119,121](et) G[89,92](que)",
                "[122,125]Dependency(dep_coord,basic) D[122,125](que) G[119,121](et)",
                "[126,134]Dependency(obj,basic) D[126,134](possible) G[122,125](que)",
                "[135,136]Dependency(ponct,basic) D[135,136](.) G[5,10](avons)" };

        String[] posMapped = { "POS_PRON", "POS_VERB", "POS_NOUN", "POS_ADP", "POS_NOUN", "POS_ADP",
                "POS_NOUN", "POS_ADV", "POS_ADJ", "POS_PRON", "POS_VERB", "POS_DET", "POS_NOUN",
                "POS_CONJ", "POS_DET", "POS_ADJ", "POS_NOUN", "POS_CONJ", "POS_CONJ", "POS_ADJ",
                "POS_PUNCT" };

        String[] posOriginal = { "CLS", "V", "NC", "P", "NC", "P", "NC", "ADV", "ADJ",
                "PROREL", "V", "DET", "NC", "CS", "DET", "ADJ", "NC", "CC", "CS", "ADJ", "PONCT" };

        String[] posTags = { "ADJ", "ADJWH", "ADV", "ADVWH",
                "CC", "CLO", "CLR", "CLS", "CS", "DET", "DETWH", "END", "ET", "I", "MID", "NC",
                "NPP", "P", "P+D", "P+PRO", "PONCT", "PREF", "PRO", "PROREL", "PROWH", "STPOS",
                "STR", "V", "VIMP", "VINF", "VPP", "VPR", "VS" };

        String[] depTags = { "END", "a_obj", "aff", "arg", "ato", "ats", "aux_caus", "aux_pass",
                "aux_tps", "comp", "coord", "de_obj", "dep", "dep_coord", "det", "missinghead",
                "mod", "mod_rel", "obj", "obj1", "p_obj", "ponct", "root", "suj" };

        String[] unmappedPos = { "END", "MID", "STPOS", "STR" };

        String[] unmappedDep = { "END", "comp", "missinghead", "obj1", "root" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));

        AssertAnnotations.assertTagset(POS.class, "melt", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "melt", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "ftb", depTags, jcas);
        AssertAnnotations.assertTagsetMapping(Dependency.class, "ftb", unmappedDep, jcas);
    }

    @Test
    public void testFarsi()
        throws Exception
    {
        JCas jcas = runTest(
                "fa",
                "parsper",
                "ما به عنوان مثال جمله بسیار پیچیده، که شامل به عنوان بسیاری از مولفه ها و وابستگی ها که ممکن است نیاز دارید .");

        String[] dependencies = {
                "[  0,  2]Dependency(nsubj,basic) D[0,2](ما) G[39,43](شامل)",
                "[  3,  5]Dependency(prep,basic) D[3,5](به) G[39,43](شامل)",
                "[  6, 11]Dependency(pobj,basic) D[6,11](عنوان) G[3,5](به)",
                "[ 12, 16]Dependency(pobj,basic) D[12,16](مثال) G[3,5](به)",
                "[ 17, 21]Dependency(pobj,basic) D[17,21](جمله) G[3,5](به)",
                "[ 22, 27]Dependency(advmod,basic) D[22,27](بسیار) G[28,35](پیچیده،)",
                "[ 28, 35]Dependency(acomp,basic) D[28,35](پیچیده،) G[39,43](شامل)",
                "[ 36, 38]Dependency(rel,basic) D[36,38](که) G[39,43](شامل)",
                "[ 39, 43]ROOT(root,basic) D[39,43](شامل) G[39,43](شامل)",
                "[ 44, 46]Dependency(prep,basic) D[44,46](به) G[39,43](شامل)",
                "[ 47, 52]Dependency(pobj,basic) D[47,52](عنوان) G[44,46](به)",
                "[ 53, 59]Dependency(pobj,basic) D[53,59](بسیاری) G[47,52](عنوان)",
                "[ 60, 62]Dependency(prep,basic) D[60,62](از) G[53,59](بسیاری)",
                "[ 63, 68]Dependency(pobj,basic) D[63,68](مولفه) G[60,62](از)",
                "[ 69, 71]Dependency(pobj,basic) D[69,71](ها) G[60,62](از)",
                "[ 72, 73]Dependency(cc,basic) D[72,73](و) G[53,59](بسیاری)",
                "[ 74, 81]Dependency(pobj,basic) D[74,81](وابستگی) G[88,92](ممکن)",
                "[ 82, 84]Dependency(pobj,basic) D[82,84](ها) G[74,81](وابستگی)",
                "[ 85, 87]Dependency(complm,basic) D[85,87](که) G[88,92](ممکن)",
                "[ 88, 92]Dependency(conj,basic) D[88,92](ممکن) G[53,59](بسیاری)",
                "[ 93, 96]Dependency(pobj,basic) D[93,96](است) G[44,46](به)",
                "[ 97,101]Dependency(pobj,basic) D[97,101](نیاز) G[44,46](به)",
                "[102,107]Dependency(pobj,basic) D[102,107](دارید) G[44,46](به)",
                "[108,109]Dependency(punct,basic) D[108,109](.) G[39,43](شامل)" };

        String[] posTags = { "ADJ", "ADV", "CLITIC", "CON", "DELM", "DET", "END", "FW", "INT",
                "MID", "N", "NUM", "P", "PREV", "PRO", "STPOS", "STR", "V" };

        String[] depTags = { "END", "acc", "acomp", "acomp/pc", "advcl", "advcl/cop", "advcl/pc",
                "advmod", "advmod/pc", "amod", "amod/cop", "amod/pc", "appos", "appos/pc", "aux",
                "auxpass", "cc", "ccomp", "ccomp/cop", "ccomp/pc", "ccomp/pc/cop", "ccomp\\cpobj",
                "ccomp\\nsubj", "ccomp\\pobj", "ccomp\\poss", "complm", "conj", "conj/cop",
                "conj/pc", "conj\\pobj", "conj\\poss", "cop", "cpobj", "cpobj/pc", "cprep", "dep",
                "dep-top", "dep-voc", "dep/pc", "det", "dobj", "dobj/acc", "dobj/pc", "fw", "lvc",
                "lvc/pc", "mark", "mwe", "mwe/pc", "neg", "nn", "nn/cop", "npadvmod", "nsubj",
                "nsubj/pc", "nsubjpass", "nsubjpass/pc", "num", "number", "parataxis",
                "parataxis/cop", "parataxis/pc", "pobj", "pobj/cop", "pobj/pc", "poss", "poss/acc",
                "poss/cop", "poss/pc", "preconj", "predet", "prep", "prep/det", "prep/pc",
                "prep/pobj", "prt", "punct", "quantmod", "rcmod", "rcmod/cop", "rcmod/pc",
                "rcmod\\amod", "rcmod\\pobj", "rcmod\\poss", "rel", "root", "root/cop", "root/pc",
                "root\\conj", "root\\pobj", "root\\poss", "tmod", "xcomp" };

        String[] unmappedPos = { };
        
        String[] origPos = { "PRO", "P", "N_SING", "N_SING", "N_SING", "ADV", "ADJ", "CON", "ADJ",
                "P", "N_SING", "ADJ", "P", "N_SING", "N_SING", "CON", "N_SING", "N_PL", "CON",
                "ADJ", "V_COP", "N_SING", "V_PRS", "DELM" };

        String[] mappedPos = { "POS_PRON", "POS_ADP", "POS_NOUN", "POS_NOUN", "POS_NOUN", "POS_ADV",
                "POS_ADJ", "POS_CONJ", "POS_ADJ", "POS_ADP", "POS_NOUN", "POS_ADJ", "POS_ADP",
                "POS_NOUN", "POS_NOUN", "POS_CONJ", "POS_NOUN", "POS_NOUN", "POS_CONJ", "POS_ADJ",
                "POS_VERB", "POS_NOUN", "POS_VERB", "POS_PUNCT" };

        AssertAnnotations.assertPOS(mappedPos, origPos, JCasUtil.select(jcas, POS.class));
        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(POS.class, "upc-reduced", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "upc-reduced", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "updt", depTags, jcas);
        // FIXME AssertAnnotations.assertTagsetMapping(Dependency.class, "ftb", new String[] {},
        // jcas);
    }
    
    private JCas runTest(String aLanguage, String aVariant, String aText)
        throws Exception
    {
        AssumeResource.assumeResource(MateSemanticRoleLabeler.class, "parser", aLanguage, aVariant);
        
        AnalysisEngineDescription engine = getEngines(aLanguage, aVariant);

        if (aLanguage.startsWith("dummy-")) {
            aLanguage = aLanguage.substring("dummy-".length());
        }

        return TestRunner.runTest(engine, aLanguage, aText);
    }

    public static AnalysisEngineDescription getEngines(String aLanguage, String aVariant)
        throws ResourceInitializationException
    {
        List<AnalysisEngineDescription> engines = new ArrayList<AnalysisEngineDescription>();

        if ("fa".equals(aLanguage) || "sv".equals(aLanguage)) {
            Assume.assumeFalse("HunPos currently hangs indefinitely on Windows: Issue #1099",
                    System.getProperty("os.name").toLowerCase(Locale.US).contains("win"));
            Assume.assumeTrue("HunPos does not run on OS X Catalina or higher",
                    System.getProperty("os.name").toLowerCase(Locale.US).contains("mac") &&
                    !System.getProperty("os.version").matches("10\\.([0-9]|1[0-4]).*"));
            engines.add(createEngineDescription(HunPosTagger.class));
        }
        else {
            engines.add(createEngineDescription(MatePosTagger.class));
        }

        engines.add(createEngineDescription(TagsetDescriptionStripper.class));

        engines.add(createEngineDescription(MateParser.class, 
                MateParser.PARAM_VARIANT, aVariant,
                MateParser.PARAM_PRINT_TAGSET, true));

        return createEngineDescription(engines
                .toArray(new AnalysisEngineDescription[engines.size()]));
    }
    
    private JCas runTest(String aLanguage, String aText)
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() >= 2000000000);

        AssumeResource.assumeResource(MateSemanticRoleLabeler.class, "parser", aLanguage, null);
        
        AnalysisEngineDescription aggregate = createEngineDescription(
                createEngineDescription(MatePosTagger.class),
                createEngineDescription(MateParser.class));

        return TestRunner.runTest(aggregate, aLanguage, aText);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
