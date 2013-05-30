package de.tudarmstadt.ukp.dkpro.core.decompounding.web1t;
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


import java.io.File;

import junit.framework.Assert;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class LuceneIndexerTest
{

	File source = new File("src/test/resources/n-grams");
	File index = new File("target/test/index");
	File targetIndex0 = new File("target/test/index/0");
	File targetIndex1 = new File("target/test/index/1");
	File jWeb1T = new File("src/test/resources/web1t/de");

	@Before
	public void setUp()
		throws Exception
	{
		// Create folder if not exists
		index.mkdirs();

		// Create index
		LuceneIndexer indexer = new LuceneIndexer(source, index, 2);
		indexer.index();
	}

	@Test
	public void testSearch()
		throws Exception
	{
		// Check if fields and all documents exists
		IndexReader ir0 = IndexReader.open(FSDirectory.open(targetIndex0));
		IndexReader ir1 = IndexReader.open(FSDirectory.open(targetIndex1));
		Assert.assertEquals("Number of documents", 3, ir0.numDocs() + ir1.numDocs());

		Document doc = ir0.document(0);
		Assert.assertNotNull("Field: gram", doc.getField("gram"));
		Assert.assertNotNull("Field: freq", doc.getField("freq"));
		ir0.close();
		ir1.close();

		// Search on the index
		Finder f = new Finder(index, jWeb1T);

		Assert.assertEquals(f.find("relax").size(), 3);
		Assert.assertEquals(f.find("couch").size(), 1);
		Assert.assertEquals(f.find("relax couch").size(), 1);
		Assert.assertEquals(f.find("couchdb").size(), 1);
	}

	@Ignore
	@Test
	public void testData()
		throws Exception
	{
		Finder f = new Finder(index, jWeb1T);

		Assert.assertEquals(1, f.find("couch").size());
		Assert.assertEquals(100, f.find("couch").get(0).getFreq());
		Assert.assertEquals("relax on the couch", f.find("couch").get(0).getGram());
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
}
