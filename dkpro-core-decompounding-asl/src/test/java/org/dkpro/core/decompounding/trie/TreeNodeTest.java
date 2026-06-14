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
package org.dkpro.core.decompounding.trie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TreeNodeTest
{

    @Test
    public void testLeaf()
    {
        KeyValueNode<String, Integer> root = new KeyValueNode<String, Integer>("root", 1);

        assertTrue(root.isLeaf());
        assertFalse(root.hasChildren());
    }

    @Test
    public void testAdd()
    {
        KeyValueNode<String, Integer> root = new KeyValueNode<String, Integer>("root", 1);
        KeyValueNode<String, Integer> child = new KeyValueNode<String, Integer>("child", 1);

        root.addChild(child);
        assertFalse(root.isLeaf());
        assertTrue(root.hasChildren());
        assertTrue(child.isLeaf());
        assertFalse(child.hasChildren());

        assertTrue(root.hasChild("child"));
        assertEquals(child, root.getChild("child"));
    }
}
