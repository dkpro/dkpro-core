/*
 * Copyright 2017
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
import org.dkpro.core.io.xces.models.XcesBody;
import org.dkpro.core.io.xces.models.XcesPara;
import org.dkpro.core.io.xces.models.XcesSentence;
import org.dkpro.core.io.xces.models.XcesToken;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionUtils;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

@TypeCapability(outputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" })
public class XcesXmlReader
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
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(is);

            JAXBContext context = JAXBContext.newInstance(XcesBody.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            unmarshaller.setEventHandler(new ValidationEventHandler()
            {
                public boolean handleEvent(ValidationEvent event)
                {
                    throw new RuntimeException(event.getMessage(), event.getLinkedException());
                }
            });

            JCasBuilder jb = new JCasBuilder(aJCas);

            XMLEvent e = null;
            while ((e = xmlEventReader.peek()) != null) {

                if (isStartElement(e, "body")) {
                    try {
                        XcesBody paras = (XcesBody) unmarshaller
                                .unmarshal(xmlEventReader, XcesBody.class).getValue();
                        readPara(jb, paras);
                    }
                    catch (RuntimeException ex) {
                        System.out.println("Unable to parse XCES format: " + ex);
                    }
                }
                else {
                    xmlEventReader.next();
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
        // Below is the sample paragraph format
        // <p id="p1">
        // <s id="s1">
        // <t id="t1" word="Αυτή" tag="PnDmFe03SgNmXx" lemma="αυτός" />
        // <t id="t2" word="είναι" tag="VbMnIdPr03SgXxIpPvXx" lemma="είμαι" />
        // <t id="t3" word="η" tag="AtDfFeSgNm" lemma="ο" />
        // <t id="t4" word="πρώτη" tag="NmOdFeSgNmAj" lemma="πρώτος" />
        // <t id="t5" word="γραμμή" tag="NoCmFeSgNm" lemma="γραμμή" />
        // <t id="t6" word="." tag="PTERM_P" lemma="." />
        // </s>
        // </p>
        if (bodyObj instanceof XcesBody) {
            for (XcesPara paras : ((XcesBody) bodyObj).p) {
                int paraStart = jb.getPosition();
                int paraEnd = jb.getPosition();
                for (XcesSentence s : paras.s) {
                    int sentStart = jb.getPosition();
                    int sentEnd = jb.getPosition();
                    for (int i = 0; i < s.xcesTokens.size(); i++) {
                        XcesToken t = s.xcesTokens.get(i);
                        XcesToken tnext = i + 1 == s.xcesTokens.size() ? null
                                : s.xcesTokens.get(i + 1);

                        Token token = jb.add(t.word, Token.class);

                        if (t.lemma != null) {
                            Lemma lemma = new Lemma(jb.getJCas(), token.getBegin(), token.getEnd());
                            lemma.setValue(t.lemma);
                            lemma.addToIndexes();
                            token.setLemma(lemma);
                        }
                        if (t.tag != null) {
                            POS pos = new POS(jb.getJCas(), token.getBegin(), token.getEnd());
                            pos.setPosValue(t.tag);
                            pos.addToIndexes();
                            token.setPos(pos);
                        }
                        sentEnd = jb.getPosition();
                        if (tnext == null)
                            jb.add("\n");
                        if (tnext != null) {
                            jb.add(" ");
                        }
                    }
                    Sentence sent = new Sentence(jb.getJCas(), sentStart, sentEnd);
                    sent.addToIndexes();
                    paraEnd = sent.getEnd();
                }
                Paragraph para = new Paragraph(jb.getJCas(), paraStart, paraEnd);
                para.addToIndexes();
                jb.add("\n");
            }

        }
    }

    public static boolean isStartElement(XMLEvent aEvent, String aElement)
    {

        return aEvent.isStartElement()
                && ((StartElement) aEvent).getName().getLocalPart().equals(aElement);
    }

}