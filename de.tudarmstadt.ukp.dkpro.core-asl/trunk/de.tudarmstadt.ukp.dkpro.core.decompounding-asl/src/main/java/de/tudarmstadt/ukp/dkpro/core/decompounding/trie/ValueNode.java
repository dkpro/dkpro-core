/*******************************************************************************
 * Copyright 2010
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
 *******************************************************************************/

package de.tudarmstadt.ukp.dkpro.core.decompounding.trie;

import java.util.ArrayList;
import java.util.List;

/**
 * A tree node with a value object
 * 
 * @author Jens Haase <je.haase@googlemail.com>
 * 
 * @param <V>
 *            The value object class
 */
public class ValueNode<V>
{

	V value;
	List<ValueNode<V>> children = new ArrayList<ValueNode<V>>();

	public ValueNode()
	{
		this(null);
	}

	public ValueNode(V aValue)
	{
		value = aValue;
	}

	/**
	 * Returns the value object of this node
	 * 
	 * @return
	 */
	public V getValue()
	{
		return value;
	}

	/**
	 * Sets the value object of this node
	 * 
	 * @param aValue
	 */
	public void setValue(V aValue)
	{
		value = aValue;
	}

	/**
	 * Returns a children for this node
	 * 
	 * @return
	 */
	public List<ValueNode<V>> getChildren()
	{
		return children;
	}

	/**
	 * Returns all Children values
	 * 
	 * @return
	 */
	public List<V> getChildrenValues()
	{
		List<ValueNode<V>> values = getChildren();
		List<V> result = new ArrayList<V>();

		for (ValueNode<V> value : values) {
			result.add(value.getValue());
		}

		return result;
	}

	/**
	 * Sets the children for this node
	 * 
	 * @param aChildren
	 */
	public void setChildren(List<ValueNode<V>> aChildren)
	{
		children = aChildren;
	}

	/**
	 * Adds a child object to this node
	 * 
	 * @param node
	 */
	public void addChild(ValueNode<V> node)
	{
		children.add(node);
	}

	/**
	 * Checks if this node has children
	 * 
	 * @return
	 */
	public boolean hasChildren()
	{
		return children.size() > 0;
	}

	/**
	 * Checks if the node is a leaf node
	 * 
	 * @return
	 */
	public boolean isLeaf()
	{
		return !hasChildren();
	}
}
