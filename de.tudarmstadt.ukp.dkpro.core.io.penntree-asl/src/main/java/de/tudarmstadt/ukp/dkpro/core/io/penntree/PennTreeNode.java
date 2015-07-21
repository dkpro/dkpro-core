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

import java.util.ArrayList;
import java.util.List;

/**
 */
public class PennTreeNode
{
    private PennTreeNode parent;
	private String label;
	private List<PennTreeNode> children = new ArrayList<PennTreeNode>();
	
	public PennTreeNode getParent()
    {
        return parent;
    }

    public void setParent(PennTreeNode aParent)
    {
        parent = aParent;
    }

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
	    aNode.setParent(this);
		children.add(aNode);
	}
	
	public boolean isPreTerminal()
	{
	    return children.size() == 1 && children.get(0).isTerminal();
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
