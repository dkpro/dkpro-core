package de.tudarmstadt.ukp.dkpro.core.frequency.web1t;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class JWeb1TIndexerTest
{

    private static String indexFile1 = "src/test/resources/web1t/index-1gms";
    private static String indexFile2 = "src/test/resources/web1t/index-2gms";
    
    @Before
    public void before() {
        new File(indexFile1).delete();
        new File(indexFile2).delete();
    }
    
    @Test
    public void jweb1TIndexerTest() throws Exception {
        
        
        JWeb1TIndexer indexer = new JWeb1TIndexer("src/test/resources/web1t/", 2);
        indexer.create();
        
        assertTrue(new File(indexFile1).exists());
        assertTrue(new File(indexFile2).exists());
    }
}
