/*
 * Copyright 2017
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
 */
package de.tudarmstadt.ukp.dkpro.core.io.penntree;

import static de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils.convertPennTree;
import static de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils.parsePennTree;
import static de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils.selectDfs;
import static de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils.toPennTree;
import static de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils.toPrettyPennTree;
import static de.tudarmstadt.ukp.dkpro.core.io.penntree.PennTreeUtils.toText;
import static org.apache.uima.fit.util.JCasUtil.selectSingle;
import static org.junit.Assert.assertEquals;

import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

/**
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
	
	@Test
	public void testPrettySerialize()
	{
	    String tree = "(ROOT\n" + 
	            "  (S\n" + 
	            "    (S\n" + 
	            "      (NP\n" + 
	            "        (NP (DT The) (JJS strongest) (NN rain))\n" + 
	            "        (VP\n" + 
	            "          (ADVP (RB ever))\n" + 
	            "          (VBN recorded)\n" + 
	            "          (PP (IN in)\n" + 
	            "            (NP (NNP India)))))\n" + 
	            "      (VP\n" + 
	            "        (VP (VBD shut)\n" + 
	            "          (PRT (RP down))\n" + 
	            "          (NP\n" + 
	            "            (NP (DT the) (JJ financial) (NN hub))\n" + 
	            "            (PP (IN of)\n" + 
	            "              (NP (NNP Mumbai)))))\n" + 
	            "        (, ,)\n" + 
	            "        (VP (VBD snapped)\n" + 
	            "          (NP (NN communication) (NNS lines)))\n" + 
	            "        (, ,)\n" + 
	            "        (VP (VBD closed)\n" + 
	            "          (NP (NNS airports)))\n" + 
	            "        (CC and)\n" + 
	            "        (VP (VBD forced)\n" + 
	            "          (NP\n" + 
	            "            (NP (NNS thousands))\n" + 
	            "            (PP (IN of)\n" + 
	            "              (NP (NNS people))))\n" + 
	            "          (S\n" + 
	            "            (VP (TO to)\n" + 
	            "              (VP\n" + 
	            "                (VP (VB sleep)\n" + 
	            "                  (PP (IN in)\n" + 
	            "                    (NP (PRP$ their) (NNS offices))))\n" + 
	            "                (CC or)\n" + 
	            "                (VP (VB walk)\n" + 
	            "                  (NP (NN home))\n" + 
	            "                  (PP (IN during)\n" + 
	            "                    (NP (DT the) (NN night))))))))))\n" + 
	            "    (, ,)\n" + 
	            "    (NP (NNS officials))\n" + 
	            "    (VP (VBD said)\n" + 
	            "      (NP-TMP (NN today)))\n" + 
	            "    (. .)))";
	    
	    PennTreeNode n = parsePennTree(tree);
	    String actual = toPrettyPennTree(n);
	    
        assertEquals(tree, actual);
	}
	
	private static void doTest(String aBracket)
	{
		String expected = aBracket;

		PennTreeNode n = parsePennTree(expected);

		String actual = n.toString();

		assertEquals(expected, actual);
	}

	@Test
	@Ignore("No asserts yet!")
	public void testSelectDfs()
	{
		PennTreeNode n = parsePennTree(
				"(ROOT (S (NP (PRP It)) (VP (VBZ is) (PP (IN for) (NP (NP (DT this) (NN reason)) " +
				"(SBAR (IN that) (S (NP (NN deconstruction)) (VP (VP (VBZ remains) (NP (NP " +
				"(DT a) (JJ fundamental) (NN threat)) (PP (TO to) (NP (NNP Marxism))))) (, ,) " +
				"(CC and) (VP (PP (IN by) (NP (NP (NN implication)) (PP (TO to) (NP (JJ other) " +
				"(ADJP (JJ culturalist) (CC and) (JJ contextualizing)) " +
				"(NNS approaches)))))))))))) (. .)))");
		System.out.println(selectDfs(n, 1));
		System.out.println(selectDfs(n, 2));
		System.out.println(selectDfs(n, 3));
		System.out.println(selectDfs(n, 4));
		System.out.println(selectDfs(n, 5));
		System.out.println(selectDfs(n, 6));
		System.out.println(selectDfs(n, 7));
		System.out.println(selectDfs(n, 8));
		System.out.println(selectDfs(n, 9));
		System.out.println(selectDfs(n, 10));
		System.out.println(selectDfs(n, 11));
		System.out.println(selectDfs(n, 12));
	}

    @Test
    public void testFromUimaConversion()
        throws Exception
    {
        String documentEnglish =
                "It is for this reason that deconstruction remains a ( fundamental ) threat to " +
                "Marxism , and by implication to other culturalist and contextualizing " +
                "approaches .";

        String pennTree = "(ROOT (S (S (NP (PRP It)) (VP (VBZ is) (PP (IN for) (NP (DT this) "
                + "(NN reason))) (SBAR (IN that) (S (NP (NN deconstruction)) (VP (VBZ remains) "
                + "(NP (NP (DT a) (PRN (-LRB- -LRB-) (NN fundamental) (-RRB- -RRB-)) (NN threat)) "
                + "(PP (TO to) (NP (NNP Marxism))))))))) (, ,) (CC and) (S (PP (IN by) (NP "
                + "(NN implication))) (PP (TO to) (NP (NP (JJ other) (NN culturalist)) (CC and) "
                + "(NP (VBG contextualizing) (NNS approaches))))) (. .)))";

        PennTreeToJCasConverter converter = new PennTreeToJCasConverter(null, null);
        converter.setInternTags(true);
        converter.setWriteTracesToText(false);
        converter.setCreatePosTags(true);
        converter.setRootLabel("ROOT");
        
        JCas jcas = JCasFactory.createJCas();
        
        StringBuilder text = new StringBuilder();
        converter.convertPennTree(jcas, text, PennTreeUtils.parsePennTree(pennTree));
        jcas.setDocumentText(text.toString());
        
        ROOT root = selectSingle(jcas, ROOT.class);
        PennTreeNode r = convertPennTree(root);

        assertEquals(documentEnglish.trim(), toText(r).trim());
        AssertAnnotations.assertPennTree(pennTree, toPennTree(r));
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
