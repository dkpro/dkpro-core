/*******************************************************************************
 * Copyright 2011
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

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import edu.stanford.nlp.dcoref.Constants;

/**
 * @author Richard Eckart de Castilho
 */
public class StanfordCoreferenceResolverTest
{
	@Test
	public void test()
		throws Exception
	{
	    JCas jcas = runTest("en", "John bought a car. He is very happy with it.");

		String[][] ref = new String[][] {
		        new String[] { "John", "He" },
		        new String[] { "a car", "it" }
		};
		
        AssertAnnotations.assertCoreference(ref, select(jcas, CoreferenceChain.class));
	}

   @Test
    public void testDictionarySieve()
        throws Exception
    {
        JCas jcas = runTest("en", "John joined Google in 2012. He is doing research for the company.",
                Constants.SIEVEPASSES + ",CorefDictionaryMatch");

        String[][] ref = new String[][] {
                new String[] { "John", "He" },
                new String[] { "Google", "the company" },
                new String[] { "2012" }
        };
        
        AssertAnnotations.assertCoreference(ref, select(jcas, CoreferenceChain.class));
    }

    @Test
    public void testTriggerReparse()
        throws Exception
    {
        JCas jcas = runTest("en", "'Let's go! I want to see the Don', he said.");

        String[][] ref = new String[][] {
                new String[] { "'s", "I" },
                new String[] { "the Don'", "he" }
        };

        String[] pennTree = new String[] { 
                "(ROOT (S (LST (: ')) (VP (VB Let) (NP (PRP 's)) (VP (VB go))) (. !)))", 
                "(ROOT (S (S (NP (PRP I)) (VP (VBP want) (S (VP (TO to) (VP (VB see) (NP " + 
                "(DT the) (NX (NNP Don) (POS ')))))))) (, ,) (NP (PRP he)) (VP (VBD said)) (. .)))"
        };

        AssertAnnotations.assertPennTree(pennTree, select(jcas, PennTree.class));
        AssertAnnotations.assertCoreference(ref, select(jcas, CoreferenceChain.class));
    }

    @Test
    @Ignore("Disabled due to side effects on parser unit tests. See issue 175")
    public void testTriggerReparse1()
        throws Exception
    {
        JCas jcas = runTest("en", 
                "Other major domestic initiatives in his presidency include the Patient Protection and " + 
                "Affordable Care Act, often referred to as \"Obamacare\"; the Dodd–Frank Wall Street Reform and " + 
                "Consumer Protection Act; the Don't Ask, Don't Tell Repeal Act of 2010; the Budget Control " + 
                "Act of 2011; and the American Taxpayer Relief Act of 2012.");

        String[][] ref = new String[][] {
                new String[] { "Other major domestic initiatives in his presidency" },
                new String[] { "his presidency" },
                new String[] { "his" },
                new String[] { "the Patient Protection and Affordable Care Act, often referred to as \"Obamacare\"; the Dodd–Frank Wall Street Reform and Consumer Protection Act; the Don't Ask" },
                new String[] { "the Patient Protection and Affordable Care Act" },
                new String[] { "the Patient Protection" },
                new String[] { "Affordable Care Act" },
                new String[] { "\"Obamacare\"; the Dodd–Frank Wall Street Reform and Consumer Protection Act;" },
                new String[] { "the Dodd" },
                new String[] { "Frank Wall Street Reform and Consumer Protection Act" },
                new String[] { "Frank Wall Street Reform" },
                new String[] { "Consumer Protection Act" },
                new String[] { "Repeal Act of 2010; the Budget Control Act of 2011; and the American Taxpayer Relief Act of 2012" },
                new String[] { "2010" },
                new String[] { "the Budget Control Act of 2011" },
                new String[] { "the American Taxpayer Relief Act of 2012" },
                new String[] { "2011" },
                new String[] { "2012" },
        };

        String[] pennTree = new String[] { 
            "(ROOT (S (NP (NP (JJ Other) (JJ major) (JJ domestic) (NNS initiatives)) (PP (IN in) "
            + "(NP (PRP$ his) (NN presidency)))) (VP (VBP include) (SBAR (S (NP (NP (DT the) "
            + "(NNP Patient) (NNP Protection) (CC and) (NNP Affordable) (NNP Care) (NNP Act)) "
            + "(, ,) (VP (ADVP (RB often)) (VBN referred) (PP (TO to) (SBAR (IN as) (S (NP "
            + "(`` \") (NP (NNP Obamacare)) ('' \") (PRN (: ;) (S (NP (DT the) (NNP Dodd)) (VP "
            + "(VBP –) (NP (NP (NNP Frank) (NNP Wall) (NNP Street) (NNP Reform)) (CC and) (NP "
            + "(NNP Consumer) (NNP Protection) (NNP Act))))) (: ;))) (DT the) (VP (VBP Do) "
            + "(RB n't) (VP (VB Ask))))))) (, ,)) (VP (VBP Do) (RB n't) (VP (VB Tell) (NP (NP "
            + "(NP (NN Repeal) (NNP Act)) (PP (IN of) (NP (CD 2010)))) (: ;) (NP (NP (DT the) "
            + "(NNP Budget) (NNP Control) (NNP Act)) (PP (IN of) (NP (CD 2011)))) (: ;) "
            + "(CC and) (NP (NP (DT the) (NNP American) (NNP Taxpayer) (NNP Relief) (NNP Act)) "
            + "(PP (IN of) (NP (CD 2012)))))))))) (. .)))"
        };

        AssertAnnotations.assertPennTree(pennTree, select(jcas, PennTree.class));
        AssertAnnotations.assertCoreference(ref, select(jcas, CoreferenceChain.class));
    }

    private JCas runTest(String aLanguage, String aText)
            throws Exception
    {
        return runTest(aLanguage, aText, Constants.SIEVEPASSES);
    }
    

    private JCas runTest(String aLanguage, String aText, String aSieves)
        throws Exception
    {
        // Coreference resolution requires the parser and the NER to run before
        AnalysisEngine engine = createEngine(createEngineDescription(
                createEngineDescription(StanfordSegmenter.class),
                createEngineDescription(StanfordParser.class,
                        StanfordParser.PARAM_WRITE_CONSTITUENT, true,
                        StanfordParser.PARAM_WRITE_DEPENDENCY, true,
                        StanfordParser.PARAM_WRITE_PENN_TREE, true,
                        StanfordParser.PARAM_WRITE_POS, true),
                createEngineDescription(
                        StanfordNamedEntityRecognizer.class),
                createEngineDescription(StanfordCoreferenceResolver.class,
                        StanfordCoreferenceResolver.PARAM_SIEVES, aSieves)));

        // Set up a simple example
        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage(aLanguage);
        jcas.setDocumentText(aText);
        engine.process(jcas);

        return jcas;
    }
}
