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
 **/

package org.dkpro.core.decompounding.ranking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.core.decompounding.splitter.DecompoundedWord;
import org.dkpro.core.decompounding.splitter.DecompoundingTree;
import org.dkpro.core.decompounding.trie.ValueNode;
import org.dkpro.core.decompounding.web1t.Finder;
import org.dkpro.core.decompounding.web1t.LuceneIndexer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ProbabilityBasedTest
{

    static File source = new File("src/test/resources/ranking/n-grams");
    static File index = new File("target/test/index");
    static File jWeb1T = new File("src/test/resources/web1t/de");

    @BeforeAll
    public static void createIndex() throws Exception
    {
        index.mkdirs();

        LuceneIndexer indexer = new LuceneIndexer(source, index);
        indexer.index();
    }

    @Test
    public void testRankList() throws IOException
    {
        CompoundProbabilityRanker ranker = new CompoundProbabilityRanker(new Finder(index, jWeb1T));

        List<DecompoundedWord> list = new ArrayList<DecompoundedWord>();
        DecompoundedWord s1 = DecompoundedWord.createFromString("Aktionsplan");
        list.add(s1);
        DecompoundedWord s2 = DecompoundedWord.createFromString("Akt+ion(s)+plan");
        list.add(s2);
        DecompoundedWord s3 = DecompoundedWord.createFromString("Aktion(s)+plan");
        list.add(s3);

        List<DecompoundedWord> result = ranker.rank(list);
        assertEquals(s1, result.get(0));

        assertEquals(s1, ranker.highestRank(list));
    }

    @Test
    public void testRankTree() throws IOException
    {
        CompoundProbabilityRanker ranker = new CompoundProbabilityRanker(new Finder(index, jWeb1T));

        DecompoundedWord s1 = DecompoundedWord.createFromString("Aktionsplan");
        DecompoundedWord s2 = DecompoundedWord.createFromString("Akt+ion(s)+plan");
        DecompoundedWord s3 = DecompoundedWord.createFromString("Aktion(s)+plan");

        DecompoundingTree tree = new DecompoundingTree(s1);
        tree.getRoot().addChild(new ValueNode<DecompoundedWord>(s2));
        tree.getRoot().addChild(new ValueNode<DecompoundedWord>(s3));

        DecompoundedWord result = ranker.highestRank(tree);
        assertEquals(s1, result);
    }

    @AfterAll
    public static void tearDown() throws Exception
    {
        // Delete index again
        for (File f : index.listFiles()) {
            for (File _f : f.listFiles()) {
                _f.delete();
            }
            f.delete();
        }

        index.delete();
    }
}
