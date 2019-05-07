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

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.google.common.collect.AbstractIterator;
import de.tudarmstadt.ukp.dkpro.core.io.gigaword.internal.Article;

/**
 * The LDC distributes annotated Gigaword as a moderate number of gzipped files,
 * each of which has many documents concatenated together. This class lets you iterate
 * over the documents stored in such a file. This class was authored by the UKP Lab of
 * Technische Universität Darmstadt and is included here for their convenience.
 */

public class AnnotatedGigawordDocuments implements Iterable<Article> {
    private List<Article> articleList;
    
    private AnnotatedGigawordDocuments(List<Article> aArticleList) {
        this.articleList = aArticleList;
    }
    
    public static AnnotatedGigawordDocuments fromAnnotatedGigawordFile(Path p) throws Exception {
        try (InputStream fileInputStream = new FileInputStream(p.toString())) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            AnnotatedGigawordReader parser = new AnnotatedGigawordReader();
            saxParser.parse(fileInputStream, parser);
            return new AnnotatedGigawordDocuments(parser.getArticleList());
        }
    }
    
    public Iterator<Article> iterator() {
        return new AnnotatedArticlesIterator();
    }
    
    private class AnnotatedArticlesIterator extends AbstractIterator<Article> {
        
        private int startNextIndex = 0;
        
        @Override
        protected Article computeNext() {
            
            if (startNextIndex >= articleList.size()) {
                return endOfData();
            }
            
            else
            {
                Article nextArticle = articleList.get(startNextIndex);
                startNextIndex ++;
                return nextArticle;
            }
        }
    }
}
