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
package de.tudarmstadt.ukp.dkpro.core.corenlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.AssumeResource;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import edu.stanford.nlp.dcoref.Constants;

public class CoreNlpCoreferenceResolverTest
{
	@Test
	public void test()
		throws Exception
	{
	    JCas jcas = runTest("en", "John bought a car. He is very happy with it.");

		String[][] ref = { 
		        { "a car", "it" },
		        { "John", "He" } };
		
        AssertAnnotations.assertCoreference(ref, select(jcas, CoreferenceChain.class));
	}

    @Test
    public void testDictionarySieve()
        throws Exception
    {
        JCas jcas = runTest("en", "John joined Google in 2012. He is doing research for the company.",
                Constants.SIEVEPASSES + ",CorefDictionaryMatch");

        String[][] ref = { 
                { "Google", "the company" },
                { "John", "He" }, 
                { "2012" } };
        
        AssertAnnotations.assertCoreference(ref, select(jcas, CoreferenceChain.class));
    }

    @Test
    public void testTriggerReparse()
        throws Exception
    {
        JCas jcas = runTest("en", "'Let's go! I want to see the Don', he said.");

        String[][] ref = {
                { "'Let's go" },
                { "the Don'", "he" },
                { "I" },
                { "'Let's" } };

        String[] pennTree = { 
                "(ROOT (FRAG (NP (NP ('' ') (NNP Let) (POS 's)) (NN go)) (. !)))", 
                "(ROOT (S (S (NP (PRP I)) (VP (VBP want) (S (VP (TO to) (VP (VB see) (NP (DT the) "
                + "(NX (NNP Don) (POS ')))))))) (, ,) (NP (PRP he)) (VP (VBD said)) (. .)))"
        };

        AssertAnnotations.assertCoreference(ref, select(jcas, CoreferenceChain.class));
        AssertAnnotations.assertPennTree(pennTree, select(jcas, PennTree.class));
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

        String[][] ref = {
                { "Other major domestic initiatives in his presidency" },
                { "his presidency" },
                { "his" },
                { "the Patient Protection and Affordable Care Act, often referred to as \"Obamacare\"; the Dodd–Frank Wall Street Reform and Consumer Protection Act; the Don't Ask" },
                { "the Patient Protection and Affordable Care Act" },
                { "the Patient Protection" },
                { "Affordable Care Act" },
                { "\"Obamacare\"; the Dodd–Frank Wall Street Reform and Consumer Protection Act;" },
                { "the Dodd" },
                { "Frank Wall Street Reform and Consumer Protection Act" },
                { "Frank Wall Street Reform" },
                { "Consumer Protection Act" },
                { "Repeal Act of 2010; the Budget Control Act of 2011; and the American Taxpayer Relief Act of 2012" },
                { "2010" },
                { "the Budget Control Act of 2011" },
                { "the American Taxpayer Relief Act of 2012" },
                { "2011" },
                { "2012" } };

        String[] pennTree = { 
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
        AssumeResource.assumeResource(CoreNlpCoreferenceResolver.class,
                "de/tudarmstadt/ukp/dkpro/core/stanfordnlp", "coref", aLanguage, "default");
        
        // Coreference resolution requires the parser and the NER to run before
        AnalysisEngine engine = createEngine(createEngineDescription(
                createEngineDescription(CoreNlpSegmenter.class),
                createEngineDescription(CoreNlpPosTagger.class),
                createEngineDescription(CoreNlpLemmatizer.class),
                createEngineDescription(CoreNlpParser.class,
                        CoreNlpParser.PARAM_WRITE_CONSTITUENT, true,
                        CoreNlpParser.PARAM_WRITE_DEPENDENCY, true,
                        CoreNlpParser.PARAM_WRITE_PENN_TREE, true),
                createEngineDescription(CoreNlpNamedEntityRecognizer.class),
                createEngineDescription(CoreNlpCoreferenceResolver.class,
                        CoreNlpCoreferenceResolver.PARAM_SIEVES, aSieves)));

        // Set up a simple example
        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage(aLanguage);
        jcas.setDocumentText(aText);
        engine.process(jcas);

        return jcas;
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
