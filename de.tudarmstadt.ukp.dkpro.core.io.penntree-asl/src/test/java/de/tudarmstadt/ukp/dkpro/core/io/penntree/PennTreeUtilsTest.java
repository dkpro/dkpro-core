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
package de.tudarmstadt.ukp.dkpro.core.io.penntree;

import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.util.JCasUtil.selectSingle;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpParser;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;

/**
 * @author Richard Eckart de Castilho
 */
public class PennTreeUtilsTest
{
	@Test
	public void testParseSerialize()
	{
		doTest("(S (NP a) (VP b) (PUNC .))");
		doTest("(ROOT (S (NP (PRP It)) (VP (VBZ is) (PP (IN for) (NP (NP (DT this) (NN reason)) " +
				"(SBAR (IN that) (S (NP (NN deconstruction)) (VP (VP (VBZ remains) (NP (NP " +
				"(DT a) (JJ fundamental) (NN threat)) (PP (TO to) (NP (NNP Marxism))))) (, ,) " +
				"(CC and) (VP (PP (IN by) (NP (NP (NN implication)) (PP (TO to) (NP (JJ other) " +
				"(ADJP (JJ culturalist) (CC and) (JJ contextualizing)) " +
				"(NNS approaches)))))))))))) (. .)))");
	}

	private static void doTest(String aBracket) 
	{
		String expected = aBracket;

		PennTreeNode n = PennTreeUtils.parsePennTree(expected);

		String actual = n.toString();

		assertEquals(expected, actual);
	}
	
	@Test
	public void testSelectDfs()
	{
		PennTreeNode n = PennTreeUtils.parsePennTree(
				"(ROOT (S (NP (PRP It)) (VP (VBZ is) (PP (IN for) (NP (NP (DT this) (NN reason)) " +
				"(SBAR (IN that) (S (NP (NN deconstruction)) (VP (VP (VBZ remains) (NP (NP " +
				"(DT a) (JJ fundamental) (NN threat)) (PP (TO to) (NP (NNP Marxism))))) (, ,) " +
				"(CC and) (VP (PP (IN by) (NP (NP (NN implication)) (PP (TO to) (NP (JJ other) " +
				"(ADJP (JJ culturalist) (CC and) (JJ contextualizing)) " +
				"(NNS approaches)))))))))))) (. .)))");
		System.out.println(PennTreeUtils.selectDfs(n, 1));
		System.out.println(PennTreeUtils.selectDfs(n, 2));
		System.out.println(PennTreeUtils.selectDfs(n, 3));
		System.out.println(PennTreeUtils.selectDfs(n, 4));
		System.out.println(PennTreeUtils.selectDfs(n, 5));
		System.out.println(PennTreeUtils.selectDfs(n, 6));
		System.out.println(PennTreeUtils.selectDfs(n, 7));
		System.out.println(PennTreeUtils.selectDfs(n, 8));
		System.out.println(PennTreeUtils.selectDfs(n, 9));
		System.out.println(PennTreeUtils.selectDfs(n, 10));
		System.out.println(PennTreeUtils.selectDfs(n, 11));
		System.out.println(PennTreeUtils.selectDfs(n, 12));
	}
	
    @Test
    public void testFromUimaConversion()
        throws Exception
    {
        String documentEnglish = 
                "It is for this reason that deconstruction remains a ( fundamental ) threat to " +
                "Marxism , and by implication to other culturalist and contextualizing " +
                "approaches .";
        
        String pennTree = "(ROOT (S (S (NP (PRP It)) (VP (VBZ is) (PP (IN for) (NP (DT this) " +
        		"(NN reason))) (SBAR (IN that) (S (NP (NN deconstruction)) (VP (VBZ remains) " +
        		"(NP (NP (DT a) (-LRB- -LRB-) (NN fundamental) (-RRB- -RRB-) (NN threat)) (PP " +
        		"(TO to) (NP (NNP Marxism))))))))) (, ,) (CC and) (S (PP (IN by) (NP " +
        		"(NN implication))) (PP (TO to) (NP (NP (JJ other) (NN culturalist)) (CC and) " +
        		"(NP (VBG contextualizing) (NNS approaches))))) (. .)))";
        
        JCas jcas = runTest("en", "chunking", documentEnglish);
        
        ROOT root = selectSingle(jcas, ROOT.class);
        PennTreeNode r = PennTreeUtils.convertPennTree(root);
        
        assertEquals(documentEnglish.trim(), PennTreeUtils.toText(r).trim());
        AssertAnnotations.assertPennTree(pennTree, PennTreeUtils.toPennTree(r));
    }

	
	/**
     * Setup CAS to test parser for the English language (is only called once if
     * an English test is run)
     */
    private JCas runTest(String aLanguage, String aVariant, String aText)
        throws Exception
    {
        AnalysisEngineDescription segmenter = createPrimitiveDescription(OpenNlpSegmenter.class);

        // setup English
        AnalysisEngineDescription parser = createPrimitiveDescription(OpenNlpParser.class,
                OpenNlpParser.PARAM_VARIANT, aVariant,
                OpenNlpParser.PARAM_PRINT_TAGSET, true,
                OpenNlpParser.PARAM_CREATE_PENN_TREE_STRING, true);

        AnalysisEngineDescription aggregate = createAggregateDescription(segmenter, parser);
        
        AnalysisEngine engine = createPrimitive(aggregate);
        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage(aLanguage);
        jcas.setDocumentText(aText);
        engine.process(jcas);
        
        return jcas;
    }
    
    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }

}
