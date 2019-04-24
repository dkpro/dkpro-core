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
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.AssumeResource;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.TestRunner;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;

/**
 */
public class StanfordNamedEntityRecognizerTest
{
    @Test
    public void testDutchFremeNer() throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

        JCas jcas = runTest("nl", "freme-wikiner", "10 jaar Markus werkzaam bij SAP in Duitsland .");

        String[] ne = {
                "[  8, 14]Person(I-PER) (Markus)",
                "[ 28, 31]Organization(I-ORG) (SAP)",
                "[ 35, 44]Location(I-LOC) (Duitsland)" };

        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
    }
    
    @Test
    public void testEnglish() throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

        JCas jcas = runTest("en", null, "IBM where John Miller works is in Germany .");

        String[] ne = {
                "[  0,  3]Organization(ORGANIZATION) (IBM)",
                "[ 10, 21]Person(PERSON) (John Miller)", 
                "[ 34, 41]Location(LOCATION) (Germany)" };

        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
    }

    @Test
    public void testEnglishAdjacent()
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

        JCas jcas = runTest("en", null, "Jake John called late at night .");

        String[] ne = {
                "[  0,  9]Person(PERSON) (Jake John)" };

        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
    }

    @Test
    public void testEnglishFremeNer()
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

        JCas jcas = runTest("en", "freme-wikiner", "IBM where John Miller works is in Germany .");

        String[] ne = {
                "[  0,  3]Organization(I-ORG) (IBM)", 
                "[ 10, 21]Person(I-PER) (John Miller)", 
                "[ 34, 41]Location(I-LOC) (Germany)" };

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
    public void testNoWiki3classCaselessEnglish() throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

        JCas jcas = runTest("en", "nowiki.3class.caseless.distsim.crf",
                "ibm where john works is in germany .");

        String[] ne = { "[  0,  3]Organization(ORGANIZATION) (ibm)",
                "[ 10, 14]Person(PERSON) (john)", "[ 27, 34]Location(LOCATION) (germany)" };

        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
    }

    @Test
    public void test4classEnglish() throws Exception
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
    public void test4classCaselessEnglish() throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

        JCas jcas = runTest("en", "conll.4class.caseless.distsim.crf",
                "ibm where john works is in germany .");

        String[] ne = { "[  0,  3]Organization(ORGANIZATION) (ibm)",
                "[ 10, 14]Person(PERSON) (john)", "[ 27, 34]Location(LOCATION) (germany)" };

        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
    }

    @Test
    public void test4classCaselessMixedEnglish() throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

        JCas jcas = runTest("en", "conll.4class.caseless.distsim.crf",
                "IBM where john works is in Germany .");

        String[] ne = { 
                "[  0,  3]Organization(ORGANIZATION) (IBM)",
                "[ 10, 14]Person(PERSON) (john)", 
                "[ 27, 34]Location(LOCATION) (Germany)" };

        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
    }

    @Test
    public void test7classEnglish() throws Exception
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
    public void testEnglishWithNEInLastToken()
            throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

        JCas jcas = runTest("en", null, "IBM where John works is in Germany");

        String[] ne = {
                "[  0,  3]Organization(ORGANIZATION) (IBM)",
                "[ 10, 14]Person(PERSON) (John)",
                "[ 27, 34]Location(LOCATION) (Germany)" };

        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
    }

    @Test
    public void testGerman() throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

        JCas jcas = runTest("de", null, "Markus arbeitet seit 10 Jahren bei SAP in Deutschland .");

        String[] ne = {
                "[  0,  6]Person(PERSON) (Markus)",
                "[ 35, 38]Organization(ORGANIZATION) (SAP)",
                "[ 42, 53]Location(LOCATION) (Deutschland)" };

        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
    }

    @Test
    public void testGermanNemgp()
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

        JCas jcas = runTest("de", "nemgp", "Markus arbeitet seit 10 Jahren bei SAP in Deutschland .");

        String[] ne = {
                "[  0,  6]Person(PER) (Markus)",
                "[ 35, 38]Organization(ORG) (SAP)",
                "[ 42, 53]Location(LOC) (Deutschland)" };

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
                "[ 35, 38]Organization(I-ORG) (SAP)",
                "[ 42, 53]Location(I-LOC) (Deutschland)" };

        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
    }
    
    @Test
    public void testFrenchFremeNer() throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

        JCas jcas = runTest("fr", "freme-wikiner", "Il y a 10 ans Markus travaille dans SAP en Allemagne .");

        String[] ne = {
                "[ 14, 20]Person(I-PER) (Markus)",
                "[ 36, 39]Organization(I-ORG) (SAP)",
                "[ 43, 52]Location(I-LOC) (Allemagne)" };

        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
    }

    @Test
    public void testItalianFremeNer() throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

        JCas jcas = runTest("it", "freme-wikiner", "10 anni fa Markus lavora in SAP in Germania .");

        String[] ne = {
                "[ 11, 17]Person(I-PER) (Markus)",
                "[ 28, 31]Organization(I-ORG) (SAP)",
                "[ 35, 43]Location(I-LOC) (Germania)" };

        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
    }
    
    @Test
    public void testRussianFremeNer() throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

        JCas jcas = runTest("ru", "freme-wikiner", "10 лет Маркус работал в SAP в Германии .");

        String[] ne = {
                "[  7, 13]Person(I-PER) (Маркус)",
                "[ 24, 27]Organization(I-ORG) (SAP)",
                "[ 30, 38]Location(I-LOC) (Германии)" };

        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
    }
    
    @Test
    public void testSpanish()
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

        JCas jcas = runTest("es", null, "Hace 10 años Markus trabaja en SAP en Alemania .");

        String[] ne = {
                "[ 13, 19]Person(PERS) (Markus)",
                "[ 31, 34]Organization(ORG) (SAP)",
                "[ 38, 46]Location(LUG) (Alemania)" };

        AssertAnnotations.assertNamedEntity(ne, select(jcas, NamedEntity.class));
    }
   
    @Test
    public void testSpanishFremeNer() throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

        JCas jcas = runTest("es", "freme-wikiner", "Hace 10 años Markus trabaja en SAP en Alemania .");

        String[] ne = {
                "[ 13, 19]Person(I-PER) (Markus)",
                "[ 31, 34]NamedEntity(I-MISC) (SAP)",
                "[ 38, 46]Location(I-LOC) (Alemania)" };

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
        AssumeResource.assumeResource(StanfordNamedEntityRecognizer.class, "ner", language,
                variant);

        AnalysisEngine engine = createEngine(StanfordNamedEntityRecognizer.class,
                StanfordNamedEntityRecognizer.PARAM_VARIANT, variant,
                StanfordNamedEntityRecognizer.PARAM_PRINT_TAGSET, true);

        return TestRunner.runTest(engine, language, testDocument);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
