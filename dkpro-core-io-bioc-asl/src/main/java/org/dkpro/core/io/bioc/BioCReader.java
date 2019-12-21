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

import static org.dkpro.core.api.resources.CompressionUtils.getInputStream;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.io.bioc.internal.BioC2DKPro;
import org.dkpro.core.io.bioc.internal.model.BioCDocument;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * UIMA collection reader for plain text files.
 */
@ResourceMetaData(name = "BioC Reader")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability(MimeTypes.APPLICATION_X_BIOC)
@TypeCapability(
        outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData"})
public class BioCReader
    extends JCasResourceCollectionReader_ImplBase
{
    // XML stuff
    private JAXBContext context;
    private Unmarshaller unmarshaller;
    private XMLInputFactory xmlInputFactory;
    
    // State between files
    private Resource res;
    private InputStream is;
    private XMLEventReader xmlEventReader;
    private String source;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        // Set up XML deserialization 
        try {
            context = JAXBContext.newInstance(BioCDocument.class);
            unmarshaller = context.createUnmarshaller();
            unmarshaller.setEventHandler(new DefaultValidationEventHandler());
            xmlInputFactory = XMLInputFactory.newInstance();
        }
        catch (JAXBException e) {
            throw new ResourceInitializationException(e);
        }

        // Seek first article
        try {
            step();
        }
        catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    private void closeAll()
    {
        closeQuietly(xmlEventReader);
        xmlEventReader = null;
        IOUtils.closeQuietly(is);
        is = null;
        res = null;
    }
    
    @Override
    public void destroy()
    {
        closeAll();
        super.destroy();
    }
    
    @Override
    public boolean hasNext()
        throws IOException, CollectionException
    {
        // If there is still a reader, then there is still an article. This requires that we call
        // step() already during initialization.
        return xmlEventReader != null;
    }
    
    /**
     * Seek article in file. Stop once article element has been found without reading it.
     */
    private void step() throws IOException
    {
        // Open next file
        while (true) {
            try {
                if (res == null) {
                    // Call to super here because we want to know about the resources, not the 
                    // articles
                    if (getResourceIterator().hasNext()) {
                        // There are still resources left to read
                        res = nextFile();
                        is = getInputStream(res.getLocation(), res.getInputStream());
                        xmlEventReader = xmlInputFactory.createXMLEventReader(is);
                    }
                    else {
                        // No more files to read
                        return;
                    }
                }
                
                // Seek article in file. Stop once article element has been found without reading it
                XMLEvent e = null;
                while ((e = xmlEventReader.peek()) != null) {
                    if (isStartElement(e, "source")) {
                        xmlEventReader.next();
                        source = xmlEventReader.getElementText();
                    }
                    else if (isStartElement(e, "document")) {
                        return;
                    }
                    else {
                        xmlEventReader.next();
                    }
                }
                
                // End of file reached
                closeAll();
            }
            catch (XMLStreamException e) {
                throw new IOException(e);
            }
        }
    }
    
    @Override
    public void getNext(JCas aJCas) throws IOException, CollectionException
    {
        try {
            XMLEvent e = null;
            while ((e = xmlEventReader.peek()) != null) {
               if (isStartElement(e, "document")) {
                    BioCDocument document = unmarshaller
                            .unmarshal(xmlEventReader, BioCDocument.class).getValue();

                    initCas(aJCas, res, document.getId());
                    
                    DocumentMetaData dmd = DocumentMetaData.get(aJCas);
                    dmd.setCollectionId(source);
                    
                    BioC2DKPro.convert(document, aJCas);
                }
                else {
                    xmlEventReader.next();
                }

            }
        }
        catch (Exception e) {
            throw new IOException(e);
        }
        
        // Seek next article so we know what to return on hasNext()
        step();
    }
    
    public static boolean isStartElement(XMLEvent aEvent, String aElement)
    {
        return aEvent.isStartElement()
                && ((StartElement) aEvent).getName().getLocalPart().equals(aElement);
    }

    public static boolean isEndElement(XMLEvent aEvent, String aElement)
    {
        return aEvent.isEndElement()
                && ((EndElement) aEvent).getName().getLocalPart().equals(aElement);
    }
    
    private static void closeQuietly(XMLEventReader aRes)
    {
        if (aRes != null) {
            try {
                aRes.close();
            }
            catch (XMLStreamException e) {
                // Ignore
            }
        }
    }
}
