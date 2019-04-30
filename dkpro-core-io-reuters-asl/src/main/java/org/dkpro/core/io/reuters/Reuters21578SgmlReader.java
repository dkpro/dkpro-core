/*
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.reuters;

import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import org.dkpro.core.api.parameter.MimeTypes;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.MetaDataStringField;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Read a Reuters-21578 corpus in SGML format.
 * <p>
 * Set the directory that contains the SGML files with {@link #PARAM_SOURCE_LOCATION}.
 */
@ResourceMetaData(name = "Reuters-21578 Corpus SGML Reader")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({MimeTypes.APPLICATION_X_REUTERS21578_SGML})
@TypeCapability(
        outputs = { 
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData" })
public class Reuters21578SgmlReader
        extends JCasResourceCollectionReader_ImplBase
{
    private Queue<ReutersDocument> documentQueue;

    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);
        documentQueue = new LinkedList<>();
    }

    @Override public void getNext(JCas jCas)
            throws IOException, CollectionException
    {
        if (documentQueue.isEmpty()) {
            /* read next SGML file */
            assert getResourceIterator().hasNext();
            Resource resource = getResourceIterator().next();
            try {
                documentQueue.addAll(ExtractReuters
                        .extractFile(resource.getInputStream(), resource.getResolvedUri()));
            }
            catch (ParseException e) {
                throw new CollectionException(e);
            }
        }

        /* process 1st element of document queue */
        try {
            ReutersDocument doc = documentQueue.poll();
            initCas(jCas.getCas(), doc);
            MetaDataStringField date = new MetaDataStringField(jCas);
            date.setKey("DATE");
            date.setValue(doc.getDate().toString());
            date.addToIndexes();
        }
        catch (CASException e) {
            throw new CollectionException(e);
        }
    }

    @Override
    public boolean hasNext()
            throws IOException, CollectionException
    {
        return !documentQueue.isEmpty() || getResourceIterator().hasNext();
    }

    @Override public Progress[] getProgress()
    {
        return new Progress[0];
    }

    private void initCas(CAS aCas, ReutersDocument doc)
            throws IOException, CASException
    {
        DocumentMetaData docMetaData = DocumentMetaData.create(aCas);
        docMetaData.setDocumentTitle(doc.getTitle());
        docMetaData.setDocumentUri(doc.getPath().toString());
        docMetaData.setDocumentId(Integer.toString(doc.getNewid()));
        docMetaData.setDocumentBaseUri(getSourceLocation());
        docMetaData.setCollectionId(getSourceLocation());

        aCas.setDocumentLanguage(getLanguage());
        aCas.setDocumentText(doc.getBody());
    }
}
