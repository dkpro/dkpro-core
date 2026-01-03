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
package org.dkpro.core.io.bioc;

import static org.dkpro.core.io.bioc.BioCComponent.addCollectionMetadataField;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Optional;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.io.bioc.internal.BioCToCas;
import org.dkpro.core.io.bioc.internal.model.BioCDocument;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Reader for the BioC format.
 */
@ResourceMetaData(name = "BioC XML Reader")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability(MimeTypes.APPLICATION_X_BIOC)
@TypeCapability(outputs = { "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData" })
public class BioCReader
    extends BioCReaderImplBase
{
    private XmlMapper mapper;
    private Optional<BioCDocument> nextDocument;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException
    {
        super.initialize(aContext);
        mapper = new XmlMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            nextDocument = nextBioCDocument();
        }
        catch (CollectionException | XMLStreamException | IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void getNext(JCas aJCas) throws IOException, CollectionException
    {
        initCas(aJCas, currentResource());

        addCollectionMetadataField(aJCas, E_KEY, getCollectionKey());
        addCollectionMetadataField(aJCas, E_SOURCE, getCollectionSource());
        addCollectionMetadataField(aJCas, E_DATE, getCollectionDate());

        var document = nextDocument.get();

        // if (getCollectionSource() != null) {
        // DocumentMetaData.get(aJCas).setDocumentId(getCollectionSource());
        // }
        //
        // if (document.getId() != null) {
        // DocumentMetaData.get(aJCas).setDocumentId(document.getId());
        // }

        var jb = new JCasBuilder(aJCas);
        new BioCToCas().readDocument(jb, document);
        jb.close();

        try {
            nextDocument = nextBioCDocument();
        }
        catch (XMLStreamException | IOException e) {
            throw new IOException(e);
        }
    }

    @Override
    public boolean hasNext() throws IOException, CollectionException
    {
        return nextDocument.isPresent();
    }

    private Optional<BioCDocument> nextBioCDocument()
        throws XMLStreamException, CollectionException, IOException
    {
        if (!isFileOpen()) {
            openNextFile();
            readCollectionMetdata();
        }

        if (isFileOpen()) {
            return nextBioCDocumentInFile();
        }

        return Optional.empty();
    }

    @Override
    protected void openNextFile() throws IOException, XMLStreamException, CollectionException
    {
        super.openNextFile();
        // no-op for XmlMapper-based parsing
    }

    @Override
    protected void closeFile()
    {
        // mapper is reused, nothing to clear per-file
        super.closeFile();
    }

    private Optional<BioCDocument> nextBioCDocumentInFile() throws XMLStreamException, IOException
    {
        if (seekNextBioCDocumentInFile()) {
            // Serialize the current <document> event sequence to a string and parse with XmlMapper
            var sw = new StringWriter();
            var outFactory = XMLOutputFactory.newFactory();
            var xew = outFactory.createXMLEventWriter(sw);

            int depth = 0;
            while (getXmlEventReader().hasNext()) {
                var e = getXmlEventReader().nextEvent();
                xew.add(e);
                if (e.isStartElement()) {
                    depth++;
                }
                else if (e.isEndElement()) {
                    depth--;
                    if (depth == 0) {
                        break;
                    }
                }
            }
            xew.flush();
            xew.close();

            var xml = sw.toString();
            var document = mapper.readValue(xml, BioCDocument.class);
            return Optional.of(document);
        }

        closeFile();

        return Optional.empty();
    }
}
