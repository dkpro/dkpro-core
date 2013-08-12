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
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;
import de.tudarmstadt.ukp.dkpro.core.testing.dumper.DependencyDumper;

/**
 * @author Oliver Ferschke
 * @author Richard Eckart de Castilho
 */
public class MaltParserTest
{
	static final String documentEnglish = "We need a very complicated example sentence , which " +
			"contains as many constituents and dependencies as possible .";

	@Test
	public void testEnglishDependenciesDefault()
		throws Exception
	{
		JCas jcas = runTest("en", null, documentEnglish);

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

        String[] depTags = new String[] { "ROOT", "abbrev", "acomp", "advcl", "advmod", "amod",
                "appos", "attr", "aux", "auxpass", "cc", "ccomp", "complm", "conj", "cop", "csubj",
                "csubjpass", "dep", "det", "dobj", "expl", "infmod", "iobj", "mark", "measure",
                "neg", "nn", "nsubj", "nsubjpass", "null", "num", "number", "parataxis", "partmod",
                "pcomp", "pobj", "poss", "possessive", "preconj", "pred", "predet", "prep", "prt",
                "punct", "purpcl", "quantmod", "rcmod", "rel", "tmod", "xcomp" };

        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(Dependency.class, null, depTags, jcas);
	}

	@Test
	public void testEnglishDependenciesLinear()
		throws Exception
	{
		JCas jcas = runTest("en", "linear", documentEnglish);

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

        String[] depTags = new String[] { "ROOT", "abbrev", "acomp", "advcl", "advmod", "amod",
                "appos", "attr", "aux", "auxpass", "cc", "ccomp", "complm", "conj", "cop", "csubj",
                "csubjpass", "dep", "det", "dobj", "expl", "infmod", "iobj", "mark", "measure",
                "neg", "nn", "nsubj", "nsubjpass", "null", "num", "number", "parataxis", "partmod",
                "pcomp", "pobj", "poss", "possessive", "preconj", "pred", "predet", "prep", "prt",
                "punct", "purpcl", "quantmod", "rcmod", "rel", "tmod", "xcomp" };

        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(Dependency.class, null, depTags, jcas);
	}

	@Test
	public void testEnglishDependenciesPoly()
		throws Exception
	{
		JCas jcas = runTest("en", "poly", documentEnglish);

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

        String[] depTags = new String[] { "ROOT", "abbrev", "acomp", "advcl", "advmod", "amod",
                "appos", "attr", "aux", "auxpass", "cc", "ccomp", "complm", "conj", "cop", "csubj",
                "csubjpass", "dep", "det", "dobj", "expl", "infmod", "iobj", "mark", "measure",
                "neg", "nn", "nsubj", "nsubjpass", "null", "num", "number", "parataxis", "partmod",
                "pcomp", "pobj", "poss", "possessive", "preconj", "pred", "predet", "prep", "prt",
                "punct", "purpcl", "quantmod", "rcmod", "rel", "tmod", "xcomp" };

        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(Dependency.class, null, depTags, jcas);
	}

	@Test
	public void testGermanDependencies()
		throws Exception
	{
		checkModel("de", "linear");

		String documentGerman = "Wir brauchen ein sehr kompliziertes Beispiel , welches möglichst "
				+ "viele Konstituenten und Dependenzen beinhaltet .";

		JCas jcas = runTest("de", "linear", documentGerman);

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

        String[] depTags = new String[] { "-PUNCT-", "-UNKNOWN-", "ADV", "APP", "ATTR", "AUX",
                "AVZ", "CJ", "DET", "EXPL", "GMOD", "GRAD", "KOM", "KON", "KONJ", "NEB", "OBJA",
                "OBJC", "OBJD", "OBJG", "OBJI", "OBJP", "PAR", "PART", "PN", "PP", "PRED", "REL",
                "ROOT", "S", "SUBJ", "SUBJC", "ZEIT", "gmod-app", "koord" };

        AssertAnnotations.assertDependencies(dependencies, JCasUtil.select(jcas, Dependency.class));
        AssertAnnotations.assertTagset(Dependency.class, null, depTags, jcas);
	}

//	@Ignore("Unfortunately we have no part-of-speech tagger for Swedish")
//	@Test
//	public void testSwedishDependencies()
//		throws Exception
//	{
//		String document = "Vi behöver en mycket komplicerad exempel meningen som innehåller lika " +
//				"många beståndsdelar och beroenden som möjligt.";
//		String model = "sv-linear";
//
//		AnalysisEngineDescription pipeline = getPipeline(model);
//
//		JCas jcas = JCasFactory.createJCas();
//		jcas.setDocumentText(document);
//		jcas.setDocumentLanguage("sv");
//
//		runPipeline(jcas, pipeline);
//
//		Set<String> expected = new HashSet<String>();
//		expected.add("SS 0,2,3,10");
//		expected.add("SS 3,10,21,32");
//		expected.add("TA 21,32,41,49");
//		expected.add("ET 50,53,54,64");
//		expected.add("DT 76,89,70,75");
//		expected.add("ET 76,89,90,93");
//		expected.add("DT 108,115,104,107");
//		expected.add("ET 108,115,115,116");
//		expected.add("PA 94,103,108,115");
//		expected.add("AA 90,93,94,103");
//		expected.add("PA 54,64,76,89");
//		expected.add("DT 70,75,65,69");
//		expected.add("SP 41,49,50,53");
//		expected.add("HD 21,32,33,40");
//		expected.add("DT 21,32,14,20");
//
//		assertDependencies(jcas, expected);
//	}
//
//	@Ignore("The tags produced by our French TreeTagger model are different form the ones that " +
//			"the pre-trained MaltParser model expects. Also the input format in our MaltParser " +
//			"class is currently hardcoded to the format used by the English pre-trained model. " +
//			"For the French model the 5th column of the input format should contain fine-grained " +
//			"tags. See http://www.maltparser.org/mco/french_parser/fremalt.html")
//	@Test
//	public void testFrenchDependencies()
//		throws Exception
//	{
//		String document = "Nous avons besoin d'une phrase par exemple très compliqué, qui " +
//				"contient des constituants que de nombreuses dépendances et que possible.";
//		String model = "fr-linear";
//
//		AnalysisEngineDescription pipeline = getPipeline(model);
//
//		JCas jcas = JCasFactory.createJCas();
//		jcas.setDocumentText(document);
//		jcas.setDocumentLanguage("fr");
//
//		runPipeline(jcas, pipeline);
//
//		Set<String> expected = new HashSet<String>();
//		expected.add("DEP 24,30,31,34");
//		expected.add("DEP 43,47,57,58");
//		expected.add("MOD 59,62,72,75");
//		expected.add("MOD 107,118,119,121");
//		expected.add("MOD 107,118,126,134");
//		expected.add("MOD 107,118,134,135");
//		expected.add("MOD 107,118,122,125");
//		expected.add("MOD 76,88,89,92");
//		expected.add("MOD 59,62,63,71");
//		expected.add("DEP 43,47,48,57");
//
//		assertDependencies(jcas, expected);
//	}
//

	private JCas runTest(String aLanguage, String aVariant, String aText)
		throws Exception
	{
		AnalysisEngineDescription engine = createEngineDescription(
				createEngineDescription(OpenNlpPosTagger.class),
				createEngineDescription(MaltParser.class,
						MaltParser.PARAM_VARIANT, aVariant,
						MaltParser.PARAM_PRINT_TAGSET, true),
				createEngineDescription(DependencyDumper.class));

		return TestRunner.runTest(engine, aLanguage, aText);
	}

	private void checkModel(String aLanguage, String aVariant)
	{
		Assume.assumeTrue(getClass().getResource(
				"/de/tudarmstadt/ukp/dkpro/core/maltparser/lib/parser-" + aLanguage
						+ "-"+aVariant+".mco") != null);
	}

	@Rule public TestName testName = new TestName();
	@Before
	public void printSeparator()
	{
		System.out.println("\n=== "+testName.getMethodName()+" =====================");
	}
}
