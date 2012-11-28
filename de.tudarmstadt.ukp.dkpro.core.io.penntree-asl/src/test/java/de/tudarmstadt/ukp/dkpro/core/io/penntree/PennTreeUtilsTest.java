package de.tudarmstadt.ukp.dkpro.core.io.penntree;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
}
