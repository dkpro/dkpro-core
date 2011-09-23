package de.tudarmstadt.ukp.dkpro.core.frequency.util;

import de.tudarmstadt.ukp.dkpro.core.frequency.web1t.JWeb1TIndexer;

public class CreateTestIndexesWeb1T
{
    public static void main(String[] args) throws Exception
    {
        JWeb1TIndexer indexer = new JWeb1TIndexer("src/test/resources/web1t/", 2);
        indexer.create();
    }
}
