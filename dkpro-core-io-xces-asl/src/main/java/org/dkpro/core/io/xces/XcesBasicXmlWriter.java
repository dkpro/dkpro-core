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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
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
import org.dkpro.core.io.xces.models.XcesBodyBasic;
import org.dkpro.core.io.xces.models.XcesParaBasic;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import eu.openminted.share.annotations.api.DocumentationResource;
import javanet.staxutils.IndentingXMLEventWriter;

/**
 * Writer for the basic XCES XML format.
 */
@ResourceMetaData(name = "XCES Basic XML Writer")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({MimeTypes.APPLICATION_X_XCES_BASIC})
@TypeCapability(
        inputs = {            
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph" })
public class XcesBasicXmlWriter
    extends JCasFileWriter_ImplBase
{
    /**
     * Use this filename extension.
     */
    public static final String PARAM_FILENAME_EXTENSION = 
            ComponentParameters.PARAM_FILENAME_EXTENSION;
    @ConfigurationParameter(name = PARAM_FILENAME_EXTENSION, mandatory = true, defaultValue = ".xml")
    private String filenameExtension;

    /**
     * Character encoding of the output data.
     */
    public static final String PARAM_TARGET_ENCODING = ComponentParameters.PARAM_TARGET_ENCODING;
    @ConfigurationParameter(name = PARAM_TARGET_ENCODING, mandatory = true, 
            defaultValue = ComponentParameters.DEFAULT_ENCODING)
    private String targetEncoding;

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        OutputStream docOS = null;
        XMLEventWriter xmlEventWriter = null;
        try {
            docOS = getOutputStream(aJCas, filenameExtension);
            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            xmlEventWriter = new IndentingXMLEventWriter(
                    xmlOutputFactory.createXMLEventWriter(docOS, targetEncoding));
            JAXBContext context = JAXBContext.newInstance(XcesBodyBasic.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            xmlEventWriter.add(xmlef.createStartDocument());
            // Begin cesDoc
            xmlEventWriter.add(xmlef.createStartElement("", "", "cesDoc"));
            // Begin and End cesHeader
            xmlEventWriter.add(xmlef.createStartElement("", "", "cesHeader"));
            xmlEventWriter.add(xmlef.createEndElement("", "", "cesHeader"));

            // Begin text and body
            xmlEventWriter.add(xmlef.createStartElement("", "", "text"));
            // xmlEventWriter.add(xmlef.createStartElement("", "", "body"));

            // Begin body of all the paragraphs            
            Collection<Paragraph> parasInCas = JCasUtil.select(aJCas, Paragraph.class);
            XcesBodyBasic xb = convertToXcesBasicPara(parasInCas);
            marshaller.marshal(
                    new JAXBElement<XcesBodyBasic>(new QName("body"), XcesBodyBasic.class, xb),
                    xmlEventWriter);
            // End body of all the paragraphs
            // xmlEventWriter.add(xmlef.createEndElement("", "", "body"));
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

    private XcesBodyBasic convertToXcesBasicPara(Collection<Paragraph> parasInCas)
    {
        int paraNo = 1;
        XcesBodyBasic xb = new XcesBodyBasic();
        List<XcesParaBasic> paraList = new ArrayList<XcesParaBasic>();
        for (Paragraph p : parasInCas) {
            XcesParaBasic para = new XcesParaBasic();
            para.s = p.getCoveredText();
            para.id = "p" + Integer.toString(paraNo);
            paraList.add(para);
            paraNo++;
        }
        xb.p = paraList;
        return xb;
    }

}
