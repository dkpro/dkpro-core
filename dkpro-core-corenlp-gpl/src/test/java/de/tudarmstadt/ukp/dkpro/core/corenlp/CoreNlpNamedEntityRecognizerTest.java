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
package de.tudarmstadt.ukp.dkpro.core.corenlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

/**
 */
public class CoreNlpNamedEntityRecognizerTest
{
	@Test
	public void testEnglish()
		throws Exception
	{
		Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

        JCas jcas = runTest("en", null, "IBM where John works is in Germany .");

        String[] ne = {
                "[  0,  3]Organization(ORGANIZATION) (IBM)",
                "[ 10, 14]Person(PERSON) (John)",
                "[ 27, 34]Location(LOCATION) (Germany)" };

        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
	}

   @Test
    public void test3classCaselessEnglish()
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

        JCas jcas = runTest("en", "all.3class.caseless.distsim.crf", "ibm where john works is in germany .");

        String[] ne = {
                "[  0,  3]Organization(ORGANIZATION) (ibm)",
                "[ 10, 14]Person(PERSON) (john)",
                "[ 27, 34]Location(LOCATION) (germany)" };

        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
    }

   @Test
   public void testNoWiki3classCaselessEnglish()
       throws Exception
   {
       Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

       JCas jcas = runTest("en", "nowiki.3class.caseless.distsim.crf", "ibm where john works is in germany .");

       String[] ne = {
               "[  0,  3]Organization(ORGANIZATION) (ibm)",
               "[ 10, 14]Person(PERSON) (john)",
               "[ 27, 34]Location(LOCATION) (germany)" };

       AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
   }


   @Test
    public void test4classEnglish()
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

        JCas jcas = runTest("en", "conll.4class.distsim.crf", "IBM where John works is in Germany .");

        String[] ne = {
                "[  0,  3]Organization(ORGANIZATION) (IBM)",
                "[ 10, 14]Person(PERSON) (John)",
                "[ 27, 34]Location(LOCATION) (Germany)" };

        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
    }


   @Test
   public void test4classCaselessEnglish()
       throws Exception
   {
       Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

       JCas jcas = runTest("en", "conll.4class.caseless.distsim.crf", "ibm where john works is in germany .");

       String[] ne = {
               "[  0,  3]Organization(ORGANIZATION) (ibm)",
               "[ 10, 14]Person(PERSON) (john)",
               "[ 27, 34]Location(LOCATION) (germany)" };

       AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
   }

   @Test
   public void test4classCaselessMixedEnglish()
       throws Exception
   {
       Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

       JCas jcas = runTest("en", "conll.4class.caseless.distsim.crf", "IBM where john works is in Germany .");

       String[] ne = {
               "[  0,  3]Organization(ORGANIZATION) (IBM)",
               "[ 10, 14]Person(PERSON) (john)",
               "[ 27, 34]Location(LOCATION) (Germany)" };

       AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
   }

   @Test
   public void test7classEnglish()
       throws Exception
   {
       Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

       JCas jcas = runTest("en", "muc.7class.distsim.crf", "IBM where John works is in Germany .");

       String[] ne = {
               "[  0,  3]Organization(ORGANIZATION) (IBM)",
               "[ 10, 14]Person(PERSON) (John)",
               "[ 27, 34]Location(LOCATION) (Germany)" };

       AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
   }

	@Test
	public void testGerman()
		throws Exception
	{
		Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

        JCas jcas = runTest("de", null, "Markus arbeitet seit 10 Jahren bei SAP in Deutschland .");

        String[] ne = {
                "[  0,  6]Person(I-PER) (Markus)",
                "[ 21, 23]NamedEntity(NUMBER) (10)",
                "[ 35, 38]Organization(I-ORG) (SAP)",
                "[ 42, 53]Location(I-LOC) (Deutschland)" };

        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
	}

   @Test
    public void testHgcGerman()
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

        JCas jcas = runTest("de", "hgc_175m_600.crf", "Markus arbeitet seit 10 Jahren bei SAP in Deutschland .");

        String[] ne = {
                "[  0,  6]Person(I-PER) (Markus)",
                "[ 21, 23]NamedEntity(NUMBER) (10)",
                "[ 35, 38]Organization(I-ORG) (SAP)",
                "[ 42, 53]Location(I-LOC) (Deutschland)" };

        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
    }

   @Test
   public void testSpanish()
       throws Exception
   {
       Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

       JCas jcas = runTest("es", null, "Hace 10 años Markus trabaja en SAP en Alemania .");

       String[] ne = {
               "[  5,  7]NamedEntity(NUMBER) (10)",
               "[ 13, 19]Person(PERS) (Markus)",
               "[ 31, 34]Organization(ORG) (SAP)",
               "[ 38, 46]Location(LUG) (Alemania)" };

       AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
   }

	@Test(expected = AnalysisEngineProcessException.class)
	public void testMissingModel() throws Exception
	{
        runTest("xx", null, "Xec xena Xeo .");
	}

    private JCas runTest(String language, String variant, String testDocument)
        throws Exception
    {
        AnalysisEngine engine = createEngine(CoreNlpNamedEntityRecognizer.class,
                CoreNlpNamedEntityRecognizer.PARAM_VARIANT, variant,
                CoreNlpNamedEntityRecognizer.PARAM_PRINT_TAGSET, true);

        return TestRunner.runTest(engine, language, testDocument);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
