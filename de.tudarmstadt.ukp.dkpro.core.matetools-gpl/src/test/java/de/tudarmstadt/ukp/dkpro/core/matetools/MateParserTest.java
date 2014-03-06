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
package de.tudarmstadt.ukp.dkpro.core.matetools;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class MateParserTest
{
	@Test
	public void testGerman()
		throws Exception
	{
		JCas jcas = runTest("de", "Wir brauchen ein sehr kompliziertes Beispiel , welches "
				+ "möglichst viele Konstituenten und Dependenzen beinhaltet .");

        String[] dependencies = new String[] {
                "[  0,  3]Dependency(SB) D[0,3](Wir) G[4,12](brauchen)",
                "[ 13, 16]Dependency(NK) D[13,16](ein) G[36,44](Beispiel)",
                "[ 17, 21]Dependency(MO) D[17,21](sehr) G[22,35](kompliziertes)",
                "[ 22, 35]Dependency(NK) D[22,35](kompliziertes) G[36,44](Beispiel)",
                "[ 36, 44]DOBJ(OA) D[36,44](Beispiel) G[4,12](brauchen)",
                "[ 45, 46]Dependency(--) D[45,46](,) G[36,44](Beispiel)",
                "[ 47, 54]Dependency(SB) D[47,54](welches) G[101,111](beinhaltet)",
                "[ 55, 64]Dependency(MO) D[55,64](möglichst) G[65,70](viele)",
                "[ 65, 70]Dependency(NK) D[65,70](viele) G[71,84](Konstituenten)",
                "[ 71, 84]DOBJ(OA) D[71,84](Konstituenten) G[101,111](beinhaltet)",
                "[ 85, 88]Dependency(CD) D[85,88](und) G[71,84](Konstituenten)",
                "[ 89,100]CONJ(CJ) D[89,100](Dependenzen) G[85,88](und)",
                "[101,111]Dependency(RC) D[101,111](beinhaltet) G[36,44](Beispiel)",
                "[112,113]Dependency(--) D[112,113](.) G[101,111](beinhaltet)" };

        String[] posTags = new String[] { "$(", "$,", "$.", "<None>", "<root-POS>", "ADJA", "ADJD",
                "ADV", "APPO", "APPR", "APPRART", "APZR", "ART", "CARD", "END", "FM", "ITJ",
                "KOKOM", "KON", "KOUI", "KOUS", "MID", "NE", "NN", "NNE", "PDAT", "PDS", "PIAT",
                "PIS", "PPER", "PPOSAT", "PPOSS", "PRELAT", "PRELS", "PRF", "PROAV", "PTKA",
                "PTKANT", "PTKNEG", "PTKVZ", "PTKZU", "PWAT", "PWAV", "PWS", "STPOS", "STR",
                "TRUNC", "VAFIN", "VAIMP", "VAINF", "VAPP", "VMFIN", "VMINF", "VMPP", "VVFIN",
                "VVIMP", "VVINF", "VVIZU", "VVPP", "XY" };

        String[] dependencyTags = new String[] { "--", "<None>", "<no-type>", "<root-type>", "AC",
                "ADC", "AG", "AMS", "APP", "AVC", "CC", "CD", "CJ", "CM", "CP", "CVC", "DA", "DM",
                "END", "EP", "JU", "MNR", "MO", "NG", "NK", "NMC", "OA", "OA2", "OC", "OG", "OP",
                "PAR", "PD", "PG", "PH", "PM", "PNC", "RC", "RE", "RS", "SB", "SBP", "SP", "SVP",
                "UC", "VO" };

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

        String[] dependencies = new String[] {
                "[  0,  2]Dependency(SBJ) D[0,2](We) G[3,7](need)",
                "[  8,  9]Dependency(NMOD) D[8,9](a) G[35,43](sentence)",
                "[ 10, 14]Dependency(AMOD) D[10,14](very) G[15,26](complicated)",
                "[ 15, 26]Dependency(NMOD) D[15,26](complicated) G[35,43](sentence)",
                "[ 27, 34]Dependency(NMOD) D[27,34](example) G[35,43](sentence)",
                "[ 35, 43]Dependency(OBJ) D[35,43](sentence) G[3,7](need)",
                "[ 44, 45]Dependency(P) D[44,45](,) G[35,43](sentence)",
                "[ 46, 51]Dependency(SBJ) D[46,51](which) G[52,60](contains)",
                "[ 52, 60]Dependency(NMOD) D[52,60](contains) G[35,43](sentence)",
                "[ 61, 63]Dependency(AMOD) D[61,63](as) G[64,68](many)",
                "[ 64, 68]Dependency(NMOD) D[64,68](many) G[69,81](constituents)",
                "[ 69, 81]Dependency(OBJ) D[69,81](constituents) G[52,60](contains)",
                "[ 82, 85]Dependency(COORD) D[82,85](and) G[69,81](constituents)",
                "[ 86, 98]Dependency(CONJ) D[86,98](dependencies) G[82,85](and)",
                "[ 99,101]Dependency(NMOD) D[99,101](as) G[69,81](constituents)",
                "[102,110]Dependency(PMOD) D[102,110](possible) G[99,101](as)",
                "[111,112]Dependency(P) D[111,112](.) G[3,7](need)" };

        String[] posTags = new String[] { "#", "$", "''", "(", ")", ",", ".", ":", "<None>",
                "<root-POS>", "CC", "CD", "DT", "END", "EX", "FW", "HYPH", "IN", "JJ", "JJR",
                "JJS", "LS", "MD", "MID", "NIL", "NN", "NNP", "NNPS", "NNS", "PDT", "POS", "PRF",
                "PRP", "PRP$", "RB", "RBR", "RBS", "RP", "STPOS", "STR", "SYM", "TO", "UH", "VB",
                "VBD", "VBG", "VBN", "VBP", "VBZ", "WDT", "WP", "WP$", "WRB", "``" };

        String[] dependencyTags = new String[] { "<None>", "<no-type>", "<root-type>", "ADV",
                "ADV-GAP", "AMOD", "AMOD-GAP", "APPO", "BNF", "CONJ", "COORD", "DEP", "DEP-GAP",
                "DIR", "DIR-GAP", "DIR-OPRD", "DIR-PRD", "DTV", "DTV-GAP", "END", "EXT", "EXT-GAP",
                "EXTR", "EXTR-GAP", "GAP-LGS", "GAP-LOC", "GAP-LOC-PRD", "GAP-MNR", "GAP-NMOD",
                "GAP-OBJ", "GAP-OPRD", "GAP-PMOD", "GAP-PRD", "GAP-PRP", "GAP-PUT", "GAP-SBJ",
                "GAP-SUB", "GAP-TMP", "GAP-VC", "HMOD", "HYPH", "IM", "LGS", "LOC", "LOC-MNR",
                "LOC-OPRD", "LOC-PRD", "LOC-TMP", "MNR", "MNR-PRD", "MNR-TMP", "NAME", "NMOD",
                "OBJ", "OPRD", "P", "PMOD", "POSTHON", "PRD", "PRD-PRP", "PRD-TMP", "PRN", "PRP",
                "PRT", "PUT", "ROOT", "SBJ", "SUB", "SUFFIX", "TITLE", "TMP", "VC", "VOC" };

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

        String[] dependencies = new String[] {
                "[  0,  4]Dependency(suj) D[0,4](Nous) G[5,10](avons)",
                "[ 11, 17]Dependency(obj) D[11,17](besoin) G[5,10](avons)",
                "[ 18, 23]Dependency(mod) D[18,23](d'une) G[5,10](avons)",
                "[ 24, 30]Dependency(obj) D[24,30](phrase) G[18,23](d'une)",
                "[ 31, 34]Dependency(dep) D[31,34](par) G[24,30](phrase)",
                "[ 35, 42]Dependency(obj) D[35,42](exemple) G[31,34](par)",
                "[ 43, 47]Dependency(mod) D[43,47](très) G[48,58](compliqué,)",
                "[ 48, 58]Dependency(mod) D[48,58](compliqué,) G[35,42](exemple)",
                "[ 59, 62]Dependency(suj) D[59,62](qui) G[63,71](contient)",
                "[ 63, 71]Dependency(mod_rel) D[63,71](contient) G[24,30](phrase)",
                "[ 72, 75]Dependency(det) D[72,75](des) G[76,88](constituants)",
                "[ 76, 88]Dependency(obj) D[76,88](constituants) G[63,71](contient)",
                "[ 89, 92]Dependency(dep) D[89,92](que) G[76,88](constituants)",
                "[ 93, 95]Dependency(det) D[93,95](de) G[107,118](dépendances)",
                "[ 96,106]Dependency(mod) D[96,106](nombreuses) G[107,118](dépendances)",
                "[107,118]Dependency(obj) D[107,118](dépendances) G[89,92](que)",
                "[119,121]Dependency(coord) D[119,121](et) G[89,92](que)",
                "[122,125]Dependency(dep_coord) D[122,125](que) G[119,121](et)",
                "[126,134]Dependency(obj) D[126,134](possible) G[122,125](que)",
                "[135,136]Dependency(ponct) D[135,136](.) G[5,10](avons)" };

        String[] posMapped = new String[] { "PR", "V", "NN", "PP", "NN", "PP", "NN", "ADV", "ADJ",
                "PR", "V", "ART", "NN", "CONJ", "ART", "ADJ", "NN", "CONJ", "CONJ", "ADJ", "PUNC" };

        String[] posOriginal = new String[] { "CLS", "V", "NC", "P", "NC", "P", "NC", "ADV", "ADJ",
                "PROREL", "V", "DET", "NC", "CS", "DET", "ADJ", "NC", "CC", "CS", "ADJ", "PONCT" };

        String[] posTags = new String[] { "<None>", "<root-POS>", "ADJ", "ADJWH", "ADV", "ADVWH",
                "CC", "CLO", "CLR", "CLS", "CS", "DET", "DETWH", "END", "ET", "I", "MID", "NC",
                "NPP", "P", "P+D", "P+PRO", "PONCT", "PREF", "PRO", "PROREL", "PROWH", "STPOS",
                "STR", "V", "VIMP", "VINF", "VPP", "VPR", "VS" };

        String[] depTags = new String[] { "<None>", "<no-type>", "<root-type>", "END", "a_obj",
                "aff", "arg", "ato", "ats", "aux_caus", "aux_pass", "aux_tps", "comp", "coord",
                "de_obj", "dep", "dep_coord", "det", "missinghead", "mod", "mod_rel", "obj",
                "obj1", "p_obj", "ponct", "root", "suj" };
        
        String[] unmappedPos = new String[] { "<None>", "<root-POS>", "END", "MID", "STPOS", "STR" };
        
        String[] unmappedDep = new String[] { "<None>", "<no-type>", "<root-type>", "END", "comp",
                "missinghead", "obj1", "root" };

        AssertAnnotations.assertPOS(posMapped, posOriginal, select(jcas, POS.class));
        AssertAnnotations.assertDependencies(dependencies, select(jcas, Dependency.class));

        AssertAnnotations.assertTagset(POS.class, "melt", posTags, jcas);
        AssertAnnotations.assertTagsetMapping(POS.class, "melt", unmappedPos, jcas);
        AssertAnnotations.assertTagset(Dependency.class, "ftb", depTags, jcas);
        AssertAnnotations.assertTagsetMapping(Dependency.class, "ftb", unmappedDep, jcas);
    }
    
	private JCas runTest(String aLanguage, String aText)
		throws Exception
	{
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() >= 2000000000);

		AnalysisEngineDescription aggregate = createEngineDescription(
				createEngineDescription(MatePosTagger.class),
				createEngineDescription(MateParser.class));

        return TestRunner.runTest(aggregate, aLanguage, aText);
	}

	@Rule
	public TestName name = new TestName();

	@Before
	public void printSeparator()
	{
		System.out.println("\n=== " + name.getMethodName() + " =====================");
	}
}
