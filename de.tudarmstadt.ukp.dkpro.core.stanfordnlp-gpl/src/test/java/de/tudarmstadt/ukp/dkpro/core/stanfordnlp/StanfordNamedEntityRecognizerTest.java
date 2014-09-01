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
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

/**
 * @author Oliver Ferschke
 */
public class StanfordNamedEntityRecognizerTest
{
	@Test
	public void testEnglish()
		throws Exception
	{
		Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

        JCas jcas = runTest("en", null, "IBM where John works is in Germany .");

        String[] original = { "LOCATION 'Germany'", "ORGANIZATION 'IBM'", "PERSON 'John'" };

        String[] mapped = { "Location 'Germany'", "Organization 'IBM'", "Person 'John'" };

        AssertAnnotations.assertNamedEntity(mapped, original, select(jcas, NamedEntity.class));
	}

	@Test
	public void testGerman()
		throws Exception
	{
		Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 1000000000);

        JCas jcas = runTest("de", null, "Markus arbeitet seit 10 Jahren bei SAP in Deutschland .");

        String[] original = { "I-LOC 'Deutschland'", "I-ORG 'SAP'", "I-PER 'Markus'" };

        String[] mapped = { "Location 'Deutschland'", "Organization 'SAP'", "Person 'Markus'" };

        AssertAnnotations.assertNamedEntity(mapped, original, select(jcas, NamedEntity.class));
	}

	@Test(expected = AnalysisEngineProcessException.class)
	public void testMissingModel() throws Exception
	{
        runTest("xx", null, "Xec xena Xeo .");
	}
	
    private JCas runTest(String language, String variant, String testDocument)
        throws Exception
    {
        AnalysisEngine engine = createEngine(StanfordNamedEntityRecognizer.class,
                StanfordNamedEntityRecognizer.PARAM_VARIANT, variant,
                StanfordNamedEntityRecognizer.PARAM_PRINT_TAGSET, true);

        return TestRunner.runTest(engine, language, testDocument);
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}
