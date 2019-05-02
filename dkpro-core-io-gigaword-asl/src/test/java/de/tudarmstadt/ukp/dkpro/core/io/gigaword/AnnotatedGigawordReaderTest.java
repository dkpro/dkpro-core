/*
 * Copyright 2019
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
package de.tudarmstadt.ukp.dkpro.core.io.gigaword;

import static com.google.common.collect.Iterables.partition;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.gigaword.internal.Article;

public class AnnotatedGigawordReaderTest
{
    private static final String FILE_PATH = "src/test/resources/gigaword_test.txt";
    
    @Test
    public void collectArticlesFromAnnotatedGigaword()
            throws Exception
    {
        List<Article> collectedArticles = new ArrayList<>();
        Iterable<List<Article>> iterator;
        iterator = partition(AnnotatedGigawordDocuments
                .fromAnnotatedGigawordFile(new File(FILE_PATH).toPath()), 1);
        for (List<Article> articles : iterator) {
            collectedArticles.addAll(articles);
        }
        assertEquals(collectedArticles.size(), 3);
    }
}
