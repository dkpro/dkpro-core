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
