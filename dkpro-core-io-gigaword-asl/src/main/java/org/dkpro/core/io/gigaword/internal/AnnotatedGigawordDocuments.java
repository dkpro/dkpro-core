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
package org.dkpro.core.io.gigaword.internal;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dkpro.core.api.io.ResourceCollectionReaderBase.Resource;
import org.dkpro.core.api.resources.CompressionUtils;

import com.google.common.collect.AbstractIterator;

/**
 * The LDC distributes annotated Gigaword as a moderate number of gzipped files, each of which has
 * many documents concatenated together. This class lets you iterate over the documents stored in
 * such a file.
 */
public class AnnotatedGigawordDocuments
    implements Iterable<AnnotatedGigawordArticle>
{
    private List<AnnotatedGigawordArticle> articleList;

    private AnnotatedGigawordDocuments(List<AnnotatedGigawordArticle> aArticleList)
    {
        articleList = aArticleList;
    }

    public static AnnotatedGigawordDocuments fromAnnotatedGigawordFile(Resource aResource)
        throws Exception
    {
        try (InputStream is = new BufferedInputStream(CompressionUtils
                .getInputStream(aResource.getLocation(), aResource.getInputStream()))) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            AnnotatedGigawordParser parser = new AnnotatedGigawordParser(aResource);
            saxParser.parse(is, parser);
            return new AnnotatedGigawordDocuments(parser.getArticleList());
        }
    }

    @Override
    public Iterator<AnnotatedGigawordArticle> iterator()
    {
        return new AnnotatedArticlesIterator();
    }

    private class AnnotatedArticlesIterator
        extends AbstractIterator<AnnotatedGigawordArticle>
    {
        private int startNextIndex = 0;

        @Override
        protected AnnotatedGigawordArticle computeNext()
        {
            if (startNextIndex >= articleList.size()) {
                return endOfData();
            }
            else {
                AnnotatedGigawordArticle nextArticle = articleList.get(startNextIndex);
                startNextIndex++;
                return nextArticle;
            }
        }
    }
}
