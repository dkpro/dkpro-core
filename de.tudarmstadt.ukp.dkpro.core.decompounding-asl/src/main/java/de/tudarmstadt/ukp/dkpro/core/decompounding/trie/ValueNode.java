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
 * @author <a href="mailto:je.haase@googlemail.com">Jens Haase</a>
 * 
 * @param <V>
 *            The value object class
 */
public class ValueNode<V>
{
    private V value;
    private List<ValueNode<V>> children = new ArrayList<ValueNode<V>>();

    public ValueNode(V aValue)
    {
        value = aValue;
    }

    /**
     * Returns the value object of this node
     * 
     * @return the value.
     */
    public V getValue()
    {
        return value;
    }

    /**
     * Sets the value object of this node
     * 
     * @param aValue
     *            the value.
     */
    public void setValue(V aValue)
    {
        value = aValue;
    }

    /**
     * Returns a children for this node
     * 
     * @return the children.
     */
    public List<ValueNode<V>> getChildren()
    {
        return children;
    }

    /**
     * Returns all children's values
     * 
     * @return the children's values.
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
     *            the children.
     */
    public void setChildren(List<ValueNode<V>> aChildren)
    {
        children = aChildren;
    }

    /**
     * Adds a child object to this node
     * 
     * @param node
     *            the child object.
     */
    public void addChild(ValueNode<V> node)
    {
        children.add(node);
    }

    /**
     * Checks if this node has children
     * 
     * @return if this node has children
     */
    public boolean hasChildren()
    {
        return children.size() > 0;
    }

    /**
     * Checks if the node is a leaf node
     * 
     * @return if the node is a leaf node
     */
    public boolean isLeaf()
    {
        return !hasChildren();
    }
}
