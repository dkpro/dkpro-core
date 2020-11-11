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
package org.dkpro.core.io.gigaword;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.io.ResourceCollectionReaderBase;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.io.gigaword.internal.AnnotatedGigawordArticle;
import org.dkpro.core.io.gigaword.internal.AnnotatedGigawordExtractor;
import org.dkpro.core.io.gigaword.internal.AnnotatedGigawordParser;
import org.xml.sax.SAXException;

import com.google.common.collect.AbstractIterator;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * UIMA collection reader for plain text files.
 */
@ResourceMetaData(name = "Annotated Gigaword Text Reader")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability(MimeTypes.TEXT_PLAIN)
@TypeCapability(
        outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData"})
public class AnnotatedGigawordReader
    extends ResourceCollectionReaderBase
{
    private MultiFileArticleIterator iter;
    
    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        iter = new MultiFileArticleIterator();
    }
    
    @Override
    public void getNext(CAS aJCas)
        throws IOException, CollectionException
    {
        AnnotatedGigawordArticle article = iter.next();
        Resource res = article.getResource();
        initCas(aJCas, res, article.getId());

        DocumentMetaData dmd = DocumentMetaData.get(aJCas);
        dmd.setDocumentId(article.getId());
        try
        {
            AnnotatedGigawordParser parser = new AnnotatedGigawordParser();
            parser.setJCas(aJCas.getJCas());
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(new ByteArrayInputStream(article.getText().getBytes()), parser);
        }
        catch (CASException e) {
            throw new CollectionException(e);
        }
        catch (SAXException | ParserConfigurationException e) {
            throw new IOException(e);
        }
    }
    
    @Override
    public boolean hasNext() throws IOException, CollectionException
    {
        return iter.hasNext();
    }
    
    private class MultiFileArticleIterator extends AbstractIterator<AnnotatedGigawordArticle>
    {
        private Iterator<AnnotatedGigawordArticle> currentFileIterator;
        
        @Override
        protected AnnotatedGigawordArticle computeNext()
        {
            try {
                while (
                        (currentFileIterator == null || !currentFileIterator.hasNext())
                        && AnnotatedGigawordReader.super.hasNext()
                ) {
                    Resource res = nextFile();
                    currentFileIterator = new AnnotatedGigawordExtractor(res).getArticleList()
                            .iterator();
                }
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
            
            if (currentFileIterator == null || !currentFileIterator.hasNext()) {
                return endOfData();
            }
            
            return currentFileIterator.next();
        }
    }
}
