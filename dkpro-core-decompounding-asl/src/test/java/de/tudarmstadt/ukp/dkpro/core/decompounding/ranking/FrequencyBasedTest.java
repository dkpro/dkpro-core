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
package de.tudarmstadt.ukp.dkpro.core.decompounding.ranking;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dkpro.core.testing.DkproTestContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.DecompoundedWord;
import de.tudarmstadt.ukp.dkpro.core.decompounding.splitter.DecompoundingTree;
import de.tudarmstadt.ukp.dkpro.core.decompounding.trie.ValueNode;
import de.tudarmstadt.ukp.dkpro.core.decompounding.web1t.Finder;
import de.tudarmstadt.ukp.dkpro.core.decompounding.web1t.LuceneIndexer;

public class FrequencyBasedTest
{
    private static File source;
    private static File jWeb1T;
    
    private static File testOutput;
    private static File index;

    @BeforeClass
    public static void createIndex()
        throws Exception
    {
        source = new File("src/test/resources/ranking/n-grams");
        jWeb1T = new File("src/test/resources/web1t/de");
        
        testOutput = new File("target/test-output/FrequencyBasedTest");
        index = new File(testOutput, "index");
        
        index.mkdirs();

        LuceneIndexer indexer = new LuceneIndexer(source, index);
        indexer.index();
    }

    @Test
    public void testRankList()
        throws IOException
    {
        try (Finder finder = new Finder(index, jWeb1T)) {
            FrequencyGeometricMeanRanker ranker = new FrequencyGeometricMeanRanker(finder);

            List<DecompoundedWord> list = new ArrayList<DecompoundedWord>();
            DecompoundedWord s1 = DecompoundedWord.createFromString("Aktionsplan");
            list.add(s1);
            DecompoundedWord s2 = DecompoundedWord.createFromString("Akt+ion(s)+plan");
            list.add(s2);
            DecompoundedWord s3 = DecompoundedWord.createFromString("Aktion(s)+plan");
            list.add(s3);

            List<DecompoundedWord> result = ranker.rank(list);
            assertEquals(s3, result.get(0));

            assertEquals(s3, ranker.highestRank(list));

            list.clear();
            s1 = DecompoundedWord.createFromString("einfuhr+zoll");
            s2 = DecompoundedWord.createFromString("ein+fuhr+zoll");
            list.add(s1);
            list.add(s2);
        }
    }

    @Test
    public void testRankTree()
        throws IOException
    {
        try (Finder finder = new Finder(index, jWeb1T)) {
            FrequencyGeometricMeanRanker ranker = new FrequencyGeometricMeanRanker(finder);

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

    @AfterClass
    public static void tearDown()
        throws Exception
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
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
