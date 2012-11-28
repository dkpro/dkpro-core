package de.tudarmstadt.ukp.dkpro.core.io.penntree;

import java.util.Iterator;
import java.util.Stack;
import java.util.StringTokenizer;

import org.apache.commons.lang.mutable.MutableInt;

/**
 * @author Richard Eckart de Castilho
 */
public class PennTreeUtils
{
	public static PennTreeNode selectDfs(PennTreeNode aNode, int aIndex)
	{
		return dfs(aIndex, new MutableInt(0), aNode);
	}
	
	private static PennTreeNode dfs(int aTarget, MutableInt aIndex, PennTreeNode aNode)
	{
		if (aTarget == aIndex.intValue()) {
			return aNode;
		}
		
		for (PennTreeNode n : aNode.getChildren()) {
			aIndex.increment();
			PennTreeNode r = dfs(aTarget, aIndex, n);
			if (r != null) {
				return r;
			}
		}
		
		return null;
	}

	public static PennTreeNode parsePennTree(String aTree)
	{
		StringTokenizer st = new StringTokenizer(aTree, "() ", true);
		
		PennTreeNode root = null;
		Stack<PennTreeNode> stack = new Stack<PennTreeNode>();
		boolean seenLabel = false;
		
		while (st.hasMoreTokens()) {
			String t = st.nextToken().trim();
			if (t.length() == 0) {
				// Skip
			}
			else if ("(".equals(t)) {
				PennTreeNode n = new PennTreeNode();
				stack.push(n);
				if (root == null) {
					root = n;
				}
				seenLabel = false;
			}
			else if (")".equals(t)) {
				PennTreeNode n = stack.pop();
				if (!stack.isEmpty()) {
					PennTreeNode p = stack.peek();
					p.addChild(n);
				}
			}
			else if (seenLabel) {
				// If the node has two labels, its a leaf, add a new terminal node then.
				PennTreeNode p = stack.peek();
				PennTreeNode n = new PennTreeNode();
				n.setLabel(t);
				p.addChild(n);
			}
			else {
				PennTreeNode n = stack.peek();
				n.setLabel(t);
				seenLabel = true;
			}
		}
		
		return root;
	}
	
	public static String toPennTree(PennTreeNode aNode)
	{
		StringBuilder sb = new StringBuilder();
		toPennTree(sb, aNode);
		return sb.toString().trim();
	}
	
	private static void toPennTree(StringBuilder aSb, PennTreeNode aNode)
	{
		if (!aNode.isTerminal()) {
			aSb.append('(');
		}
		
		aSb.append(aNode.getLabel());
		
		if (!aNode.isTerminal()) {
			aSb.append(' ');
			Iterator<PennTreeNode> i = aNode.getChildren().iterator();
			while (i.hasNext()) {
				toPennTree(aSb, i.next());
				if (i.hasNext()) {
					aSb.append(' ');
				}
			}
			aSb.append(')');
		}
	}
}
