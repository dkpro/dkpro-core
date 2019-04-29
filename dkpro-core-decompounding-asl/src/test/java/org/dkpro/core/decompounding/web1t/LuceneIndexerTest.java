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
package org.dkpro.core.decompounding.web1t;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.dkpro.core.decompounding.web1t.Finder;
import org.dkpro.core.decompounding.web1t.LuceneIndexer;
import org.dkpro.core.testing.DkproTestContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class LuceneIndexerTest
{
    private File testOutput;
    private File source;
    private File index;
    private File targetIndex0;
    private File targetIndex1;
    private File jWeb1T;

    @Before
    public void setUp()
        throws Exception
    {
        source = new File("src/test/resources/n-grams");
        jWeb1T = new File("src/test/resources/web1t/de");
        
        testOutput = new File("target/test-output/LuceneIndexerTest");
        index = new File(testOutput, "index");
        targetIndex0 = new File(testOutput, "index/0");
        targetIndex1 = new File(testOutput, "index/1");
        
        // Create folder if not exists
        index.mkdirs();

        // Create index
        LuceneIndexer indexer = new LuceneIndexer(source, index, 2);
        indexer.index();
    }

    @Test
    public void testSearch() throws Exception
    {
        // Check if fields and all documents exists
        try (
                IndexReader ir0 = IndexReader.open(FSDirectory.open(targetIndex0));
                IndexReader ir1 = IndexReader.open(FSDirectory.open(targetIndex1));
        ) {
            assertEquals("Number of documents", 3, ir0.numDocs() + ir1.numDocs());

            Document doc = ir0.document(0);
            assertNotNull("Field: gram", doc.getField("gram"));
            assertNotNull("Field: freq", doc.getField("freq"));
        }
        
        // Search on the index
        try (Finder f = new Finder(index, jWeb1T)) {
            assertEquals(f.find("relax").size(), 3);
            assertEquals(f.find("couch").size(), 1);
            assertEquals(f.find("relax couch").size(), 1);
            assertEquals(f.find("couchdb").size(), 1);
        }
    }

    @Test
    public void testData() throws Exception
    {
        try (Finder f = new Finder(index, jWeb1T)) {
            assertEquals(1, f.find("couch").size());
            assertEquals(100, f.find("couch").get(0).getFreq());
            assertEquals("relax on the couch", f.find("couch").get(0).getGram());
        }
    }

    @After
    public void tearDown()
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
