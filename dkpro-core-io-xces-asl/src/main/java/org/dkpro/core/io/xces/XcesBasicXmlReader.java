/*
 * Copyright 2016
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
package org.dkpro.core.io.xces;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.io.xces.models.XcesBodyBasic;
import org.dkpro.core.io.xces.models.XcesParaBasic;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;

@TypeCapability(outputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph"})
public class XcesBasicXmlReader
    extends JCasResourceCollectionReader_ImplBase
{

    @Override
    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {

        Resource res = nextFile();
        initCas(aJCas, res);

        InputStream is = null;

        try {
            is = CompressionUtils.getInputStream(res.getLocation(), res.getInputStream());
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            XMLEventReader xmlEventReaderBasic = xmlInputFactory.createXMLEventReader(is);
            
            //JAXB context for XCES body with basic type
            JAXBContext contextBasic = JAXBContext.newInstance(XcesBodyBasic.class);
            Unmarshaller unmarshallerBasic = contextBasic.createUnmarshaller();

            unmarshallerBasic.setEventHandler(new ValidationEventHandler()
            {
                public boolean handleEvent(ValidationEvent event)
                {
                    throw new RuntimeException(event.getMessage(), event.getLinkedException());
                }
            });

            JCasBuilder jb = new JCasBuilder(aJCas);

            XMLEvent eBasic = null;
            while ((eBasic = xmlEventReaderBasic.peek()) != null) {
                if (isStartElement(eBasic, "body")) {
                    try {
                        XcesBodyBasic parasBasic = (XcesBodyBasic) unmarshallerBasic
                                .unmarshal(xmlEventReaderBasic, XcesBodyBasic.class).getValue();
                        readPara(jb, parasBasic);
                    }
                    catch (RuntimeException ex) {
                        getLogger().warn(
                                "Input is not in basic xces format.");
                    }
                }
                else {
                    xmlEventReaderBasic.next();
                }

            }
            jb.close();

        }
        catch (XMLStreamException ex1) {
            throw new IOException(ex1);
        }
        catch (JAXBException e1) {
            throw new IOException(e1);
        }
        finally {
            closeQuietly(is);
        }

    }

    private void readPara(JCasBuilder jb, Object bodyObj)
    {
        //Below is the sample paragraph format
        //<p id="p1">Αυτή είναι η πρώτη γραμμή.</p>
        if (bodyObj instanceof XcesBodyBasic) {            
            for (XcesParaBasic p : ((XcesBodyBasic) bodyObj).p) {
                int start = jb.getPosition();
                int end = start + p.s.length();
                Paragraph para = new Paragraph(jb.getJCas(), start,end);             
                para.addToIndexes(jb.getJCas());
                jb.add(p.s);
                jb.add("\n\n");
            }

        }
    }

    public static boolean isStartElement(XMLEvent aEvent, String aElement)
    {

        return aEvent.isStartElement()
                && ((StartElement) aEvent).getName().getLocalPart().equals(aElement);
    }

}
