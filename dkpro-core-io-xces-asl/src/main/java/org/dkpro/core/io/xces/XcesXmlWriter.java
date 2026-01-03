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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.io.xces.models.XcesBody;
import org.dkpro.core.io.xces.models.XcesPara;
import org.dkpro.core.io.xces.models.XcesSentence;
import org.dkpro.core.io.xces.models.XcesToken;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import eu.openminted.share.annotations.api.DocumentationResource;
import javanet.staxutils.IndentingXMLEventWriter;

/**
 * Writer for the XCES XML format.
 */
@ResourceMetaData(name = "XCES XML Writer")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph" })
@MimeTypeCapability({ MimeTypes.APPLICATION_X_XCES })
public class XcesXmlWriter
    extends JCasFileWriter_ImplBase
{
    /**
     * Use this filename extension.
     */
    public static final String PARAM_FILENAME_EXTENSION = ComponentParameters.PARAM_FILENAME_EXTENSION;
    @ConfigurationParameter(name = PARAM_FILENAME_EXTENSION, defaultValue = ".xml")
    private String filenameExtension;

    /**
     * Character encoding of the output data.
     */
    public static final String PARAM_TARGET_ENCODING = "targetEncoding";
    @ConfigurationParameter(name = PARAM_TARGET_ENCODING, defaultValue = ComponentParameters.DEFAULT_ENCODING)
    private String targetEncoding;

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        OutputStream docOS = null;
        XMLEventWriter xmlEventWriter = null;
        try {
            docOS = getOutputStream(aJCas, filenameExtension);
            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            xmlEventWriter = new IndentingXMLEventWriter(
                    xmlOutputFactory.createXMLEventWriter(docOS, targetEncoding));

            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            xmlEventWriter.add(xmlef.createStartDocument());
            // Begin cesDoc
            xmlEventWriter.add(xmlef.createStartElement("", "", "cesDoc"));
            // Begin and End cesHeader
            xmlEventWriter.add(xmlef.createStartElement("", "", "cesHeader"));
            xmlEventWriter.add(xmlef.createEndElement("", "", "cesHeader"));

            // Begin text and body
            xmlEventWriter.add(xmlef.createStartElement("", "", "text"));

            // Begin body of all the paragraphs
            Collection<Paragraph> parasInCas = JCasUtil.select(aJCas, Paragraph.class);
            XcesBody xb = convertToXcesPara(parasInCas);

            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            xmlMapper.getFactory().configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, false);

            // write body element using XmlMapper without XML declaration
            String bodyXml = xmlMapper.writer().withRootName("body").writeValueAsString(xb);
            // remove any XML declaration (we already wrote one via XMLEventWriter)
            bodyXml = bodyXml.replaceAll("<\\?xml[^>]*\\?>", "");

            // write the body fragment into the existing XMLEventWriter by parsing
            // the fragment and copying events (skip start/end document and processing instructions)
            javax.xml.stream.XMLInputFactory xif = javax.xml.stream.XMLInputFactory.newInstance();
            javax.xml.stream.XMLEventReader bodyReader = xif
                    .createXMLEventReader(new java.io.StringReader(bodyXml));
            while (bodyReader.hasNext()) {
                javax.xml.stream.events.XMLEvent ev = bodyReader.nextEvent();
                int type = ev.getEventType();
                if (type == javax.xml.stream.XMLStreamConstants.START_DOCUMENT
                        || type == javax.xml.stream.XMLStreamConstants.END_DOCUMENT
                        || type == javax.xml.stream.XMLStreamConstants.PROCESSING_INSTRUCTION) {
                    continue;
                }
                if (ev.isCharacters() && ev.asCharacters().isWhiteSpace()) {
                    // skip whitespace characters produced by XmlMapper so that the
                    // IndentingXMLEventWriter can apply consistent indentation
                    continue;
                }
                xmlEventWriter.add(ev);
            }
            bodyReader.close();

            // End body of all the paragraphs
            xmlEventWriter.add(xmlef.createEndElement("", "", "text"));
            xmlEventWriter.add(xmlef.createEndElement("", "", "cesDoc"));
            xmlEventWriter.add(xmlef.createEndDocument());
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
        finally {
            if (xmlEventWriter != null) {
                try {
                    xmlEventWriter.close();
                }
                catch (XMLStreamException e) {
                    getLogger().warn("Error closing the XML event writer", e);
                }
            }

            closeQuietly(docOS);
        }
    }

    private XcesBody convertToXcesPara(Collection<Paragraph> paras)
    {
        int paraNo = 1, sentNo = 1, tokenNo = 1;
        XcesBody xb = new XcesBody();
        List<XcesPara> lp = new ArrayList<XcesPara>();
        for (Paragraph p : paras) {
            XcesPara para = new XcesPara();
            List<XcesSentence> xcesSents = new ArrayList<XcesSentence>();
            para.id = "p" + Integer.toString(paraNo);
            for (Sentence s : JCasUtil.selectCovered(Sentence.class, p)) {
                XcesSentence xcesSent = new XcesSentence();
                List<XcesToken> sentTokens = new ArrayList<XcesToken>();
                xcesSent.id = "s" + Integer.toString(sentNo);
                for (Token t : JCasUtil.selectCovered(Token.class, s)) {

                    XcesToken tok = new XcesToken();
                    tok.id = "t" + Integer.toString(tokenNo);
                    tok.word = t.getCoveredText();
                    if (t.getPos() != null) {
                        tok.tag = t.getPos().getPosValue();
                    }
                    if (t.getLemma() != null) {
                        tok.lemma = t.getLemma().getValue();
                    }
                    sentTokens.add(tok);
                    tokenNo++;
                }
                xcesSent.xcesTokens = sentTokens;
                xcesSents.add(xcesSent);
                sentNo++;
            }
            para.s = xcesSents;
            lp.add(para);
            paraNo++;
        }
        xb.p = lp;
        return xb;
    }

}
