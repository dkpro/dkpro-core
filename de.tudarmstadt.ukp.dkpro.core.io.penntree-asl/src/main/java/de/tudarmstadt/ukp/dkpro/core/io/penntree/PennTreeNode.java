package de.tudarmstadt.ukp.dkpro.core.io.penntree;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Richard Eckart de Castilho
 */
public class PennTreeNode
{
	private String label;
	private List<PennTreeNode> children = new ArrayList<PennTreeNode>();
	
	public String getLabel()
	{
		return label;
	}

	public void setLabel(String aLabel)
	{
		label = aLabel;
	}

	public List<PennTreeNode> getChildren()
	{
		return children;
	}

	public void setChildren(List<PennTreeNode> aChildren)
	{
		children = aChildren;
	}
	
	public void addChild(PennTreeNode aNode)
	{
		children.add(aNode);
	}
	
	public boolean isTerminal()
	{
		return children.isEmpty();
	}

	@Override
	public String toString()
	{
		return PennTreeUtils.toPennTree(this);
	}
}
