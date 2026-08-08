/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dkpro.core.decompounding.ranking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.dkpro.core.decompounding.splitter.DecompoundedWord;
import org.dkpro.core.decompounding.splitter.DecompoundingTree;
import org.dkpro.core.decompounding.trie.ValueNode;
import org.junit.jupiter.api.Test;

public class DummyRankerTest
{

    @Test
    public void testRankTree() throws IOException
    {
        DummyRanker ranker = new DummyRanker();

        DecompoundedWord s1 = DecompoundedWord.createFromString("Aktionsplan");
        DecompoundedWord s2 = DecompoundedWord.createFromString("Akt+ion(s)+plan");
        DecompoundedWord s3 = DecompoundedWord.createFromString("Aktion(s)+plan");

        DecompoundingTree tree = new DecompoundingTree(s1);
        tree.getRoot().addChild(new ValueNode<DecompoundedWord>(s2));
        tree.getRoot().addChild(new ValueNode<DecompoundedWord>(s3));

        DecompoundedWord result = ranker.highestRank(tree);
        assertEquals(s3, result);
    }
}
