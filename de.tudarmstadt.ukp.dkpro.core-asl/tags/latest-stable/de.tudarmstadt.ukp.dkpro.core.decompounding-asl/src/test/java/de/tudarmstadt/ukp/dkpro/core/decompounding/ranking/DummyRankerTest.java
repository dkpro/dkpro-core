/*******************************************************************************
 * Copyright 2014
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

package de.tudarmstadt.ukp.dkpro.core.decompounding.ranking;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.DecompoundedWord;
import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.DecompoundingTree;
import de.tudarmstadt.ukp.dkpro.core.decompounding.trie.ValueNode;

public class DummyRankerTest
{

    @Test
    public void testRankTree()
        throws IOException
    {
        DummyRanker ranker = new DummyRanker();

        DecompoundedWord s1 = DecompoundedWord.createFromString("Aktionsplan");
        DecompoundedWord s2 = DecompoundedWord.createFromString("Akt+ion(s)+plan");
        DecompoundedWord s3 = DecompoundedWord.createFromString("Aktion(s)+plan");

        DecompoundingTree tree = new DecompoundingTree(s1);
        tree.getRoot().addChild(new ValueNode<DecompoundedWord>(s2));
        tree.getRoot().addChild(new ValueNode<DecompoundedWord>(s3));

        DecompoundedWord result = ranker.highestRank(tree);
        Assert.assertEquals(s3, result);
    }

}
