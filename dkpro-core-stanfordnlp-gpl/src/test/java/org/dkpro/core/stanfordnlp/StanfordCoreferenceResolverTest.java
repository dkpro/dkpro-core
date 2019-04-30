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
package org.dkpro.core.stanfordnlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.AssumeResource;
import org.dkpro.core.testing.DkproTestContext;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import edu.stanford.nlp.dcoref.Constants;

/**
 */
public class StanfordCoreferenceResolverTest
{
    @Test
    public void test()
        throws Exception
    {
        JCas jcas = runTest("en", "John bought a car. He is very happy with it.");

        String[][] ref = { 
                { "John", "He" }, 
                { "a car", "it" } };
        
        AssertAnnotations.assertCoreference(ref, select(jcas, CoreferenceChain.class));
    }

    // https://github.com/dkpro/dkpro-core/issues/582
    //    Jan 22, 2015 5:11:54 PM edu.stanford.nlp.dcoref.Document findSpeaker
    //    WARNING: Cannot find node in dependency for word rally
    //    Jan 22, 2015 5:11:54 PM edu.stanford.nlp.dcoref.Document findSpeaker
    //    WARNING: Cannot find node in dependency for word told
    @Test
    public void test2()
        throws Exception
    {
        final List<LogRecord> records = new ArrayList<LogRecord>();
        ConsoleHandler handler = (ConsoleHandler) LogManager.getLogManager().getLogger("")
                .getHandlers()[0];
        java.util.logging.Level oldLevel = handler.getLevel();
        handler.setLevel(Level.ALL);
        handler.setFilter(new Filter()
        {
            @Override
            public boolean isLoggable(LogRecord record)
            {
                records.add(record);
                return false;
            }
        });

        try {
            JCas jcas = runTest("en",
                    "\" We cannot forgive this war , \" Miyako Fuji , 20 , one of the rally 's "
                            + "organisers told Jiji news agency .");

            String[][] ref = { 
                    { "Jiji" }, 
                    { "We" },
                    { "this war" },
                    { "Miyako Fuji , 20 , one of the rally 's organisers" },
                    { "Miyako Fuji , 20" }, 
                    { "Miyako Fuji", "20" }, 
                    { "one of the rally 's organisers" },
                    { "Jiji news agency" } };

            for (LogRecord r : records) {
                assertFalse(r.getMessage().contains("Cannot find node in dependency for word"));
            }

            AssertAnnotations.assertCoreference(ref, select(jcas, CoreferenceChain.class));
        }
        finally {
            if (oldLevel != null) {
                handler.setLevel(oldLevel);
                handler.setFilter(null);
            }
        }
    }

    @Test
    public void testDictionarySieve()
        throws Exception
    {
        JCas jcas = runTest("en", "John joined Google in 2012. He is doing research for the company.",
                Constants.SIEVEPASSES + ",CorefDictionaryMatch");

        String[][] ref = new String[][] { 
                { "John", "He" }, 
                { "Google", "the company" },
                { "2012" } };
        
        AssertAnnotations.assertCoreference(ref, select(jcas, CoreferenceChain.class));
    }

    @Test
    public void testTriggerReparse()
        throws Exception
    {
        JCas jcas = runTest("en", "'Let's go! I want to see the Don', he said.");

        String[][] ref = {
                { "'s", "I" },
                { "the Don'", "he" } };

        String[] pennTree = { 
                "(ROOT (S (`` ') (VP (VB Let) (S (NP (PRP 's)) (VP (VB go)))) (. !)))", 
                "(ROOT (S (S (NP (PRP I)) (VP (VBP want) (S (VP (TO to) (VP (VB see) (NP (DT the) "
                + "(NNPS Don) (POS '))))))) (, ,) (NP (PRP he)) (VP (VBD said)) (. .)))"
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
                "Other major domestic initiatives in his presidency include the Patient " + 
                "Protection and Affordable Care Act, often referred to as \"Obamacare\"; the " + 
                "Dodd–Frank Wall Street Reform and Consumer Protection Act; the Don't Ask, " + 
                "Don't Tell Repeal Act of 2010; the Budget Control Act of 2011; and the " +
                "American Taxpayer Relief Act of 2012.");

        String[][] ref = {
                { "Other major domestic initiatives in his presidency" },
                { "his presidency" },
                { "his" },
                { "the Patient Protection and Affordable Care Act, often referred to as "
                        + "\"Obamacare\"; the Dodd–Frank Wall Street Reform and Consumer "
                        + "Protection Act; the Don't Ask" },
                { "the Patient Protection and Affordable Care Act" },
                { "the Patient Protection" },
                { "Affordable Care Act" },
                { "\"Obamacare\"; the Dodd–Frank Wall Street Reform and Consumer Protection Act;" },
                { "the Dodd" },
                { "Frank Wall Street Reform and Consumer Protection Act" },
                { "Frank Wall Street Reform" },
                { "Consumer Protection Act" },
                { "Repeal Act of 2010; the Budget Control Act of 2011; and the American "
                        + "Taxpayer Relief Act of 2012" },
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
                + "(PP (IN of) (NP (CD 2012)))))))))) (. .)))" };

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
        AssumeResource.assumeResource(StanfordCoreferenceResolver.class, "coref", aLanguage,
                "default");
        
        // Coreference resolution requires the parser and the NER to run before
        AnalysisEngine engine = createEngine(createEngineDescription(
                createEngineDescription(StanfordSegmenter.class),
                createEngineDescription(StanfordParser.class,
                        StanfordParser.PARAM_WRITE_CONSTITUENT, true,
                        StanfordParser.PARAM_WRITE_DEPENDENCY, true,
                        StanfordParser.PARAM_WRITE_PENN_TREE, true,
                        StanfordParser.PARAM_WRITE_POS, true),
                createEngineDescription(StanfordLemmatizer.class),
                createEngineDescription(StanfordNamedEntityRecognizer.class),
                createEngineDescription(StanfordCoreferenceResolver.class,
                        StanfordCoreferenceResolver.PARAM_SIEVES, aSieves)));

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
