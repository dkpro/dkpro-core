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
package org.dkpro.core.decompounding.web1t;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FinderTest
{
    private File source = new File("src/test/resources/n-grams");
    private File jWeb1T = new File("src/test/resources/web1t/de");

    @Test
    public void testFinder1(@TempDir File index) throws Exception
    {
        // Create index
        LuceneIndexer indexer = new LuceneIndexer(source, index);
        indexer.index();

        try (Finder f = new Finder(index, jWeb1T)) {
            // Search and check if data is correct
            List<NGramModel> result = f.find("couch");
            assertEquals(1, result.size());
            assertEquals("relax on the couch", result.get(0).getGram());
            assertEquals(4, result.get(0).getN());
            assertEquals(100, result.get(0).getFreq());

            result = f.find("relax couch");
            assertEquals(1, result.size());
            assertEquals("relax on the couch", result.get(0).getGram());
            assertEquals(4, result.get(0).getN());
            assertEquals(100, result.get(0).getFreq());

            result = f.find("relax");
            assertEquals(3, result.size());

            result = f.find("relax");
            assertEquals(3, result.size());
        }

        // Delete index again
        for (File file : index.listFiles()) {
            for (File _f : file.listFiles()) {
                _f.delete();
            }
            file.delete();
        }

        index.delete();
    }

    @Test
    public void testFinder2(@TempDir File index) throws Exception
    {
        // Create index
        LuceneIndexer indexer = new LuceneIndexer(source, index, 2);
        indexer.index();

        try (Finder f = new Finder(index, jWeb1T)) {
            // Search and check if data is correct
            List<NGramModel> result = f.find("couch");
            assertEquals(1, result.size());
            assertEquals("relax on the couch", result.get(0).getGram());
            assertEquals(4, result.get(0).getN());
            assertEquals(100, result.get(0).getFreq());

            result = f.find("relax couch");
            assertEquals(1, result.size());
            assertEquals("relax on the couch", result.get(0).getGram());
            assertEquals(4, result.get(0).getN());
            assertEquals(100, result.get(0).getFreq());

            result = f.find("relax");
            assertEquals(3, result.size());

            result = f.find("relax");
            assertEquals(3, result.size());
        }

        // Delete index again
        for (File file : index.listFiles()) {
            for (File _f : file.listFiles()) {
                _f.delete();
            }
            file.delete();
        }

        index.delete();
    }
}
