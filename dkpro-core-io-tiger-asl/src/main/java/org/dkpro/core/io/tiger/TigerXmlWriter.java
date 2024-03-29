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
package org.dkpro.core.io.tiger;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.uima.fit.util.FSCollectionFactory.create;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.io.tiger.internal.model.TigerEdge;
import org.dkpro.core.io.tiger.internal.model.TigerGraph;
import org.dkpro.core.io.tiger.internal.model.TigerNode;
import org.dkpro.core.io.tiger.internal.model.TigerNonTerminal;
import org.dkpro.core.io.tiger.internal.model.TigerSentence;
import org.dkpro.core.io.tiger.internal.model.TigerTerminal;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import eu.openminted.share.annotations.api.DocumentationResource;
import javanet.staxutils.IndentingXMLEventWriter;

/**
 * UIMA CAS consumer writing the CAS document text in the TIGER-XML format.
 */
@ResourceMetaData(name = "TIGER-XML Writer")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({MimeTypes.APPLICATION_X_TIGER_XML})
@TypeCapability(
        inputs = {
            "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
            "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
            "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
            "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent" })
public class TigerXmlWriter extends JCasFileWriter_ImplBase
{
    /**
     * Specify the suffix of output files. Default value <code>.xml</code>. If the suffix is not
     * needed, provide an empty string as value.
     */
    public static final String PARAM_FILENAME_EXTENSION = 
            ComponentParameters.PARAM_FILENAME_EXTENSION;
    @ConfigurationParameter(name = PARAM_FILENAME_EXTENSION, mandatory = true, defaultValue = ".xml")
    private String filenameSuffix;

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
            docOS = getOutputStream(aJCas, filenameSuffix);

            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            xmlEventWriter = new IndentingXMLEventWriter(
                    xmlOutputFactory.createXMLEventWriter(docOS, targetEncoding));
            
            JAXBContext context = JAXBContext.newInstance(TigerSentence.class);
            Marshaller marshaller = context.createMarshaller();
            // We use the marshaller only for individual sentences. That way, we do not have to 
            // build the whole TIGER object graph before serializing, which should safe us some
            // memory.
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            xmlEventWriter.add(xmlef.createStartDocument());
            xmlEventWriter.add(xmlef.createStartElement("", "", "corpus"));
            xmlEventWriter.add(xmlef.createStartElement("", "", "body"));
            
            int sentenceNumber = 1;
            for (Sentence s : select(aJCas, Sentence.class)) {
                TigerSentence ts = convertSentence(s, sentenceNumber);
                marshaller.marshal(new JAXBElement<TigerSentence>(new QName("s"),
                        TigerSentence.class, ts), xmlEventWriter);
                sentenceNumber++;
            }
            
            xmlEventWriter.add(xmlef.createEndElement("", "", "body"));
            xmlEventWriter.add(xmlef.createEndElement("", "", "corpus"));
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
    
    protected TigerSentence convertSentence(Sentence aSentence, int aSentNum)
    {
        // Reset values
        int nodeNum = 1;
        Map<FeatureStructure, TigerNode> nodes = new HashMap<FeatureStructure, TigerNode>();
        
        TigerSentence sentence = new TigerSentence();
        sentence.id = aSentence.getId() != null ? aSentence.getId() : "s_" + aSentNum;
        sentence.graph = new TigerGraph();
        sentence.graph.terminals = new ArrayList<TigerTerminal>();
        
        // Convert the tokens
        for (Token token : selectCovered(Token.class, aSentence)) {
            TigerTerminal terminal = new TigerTerminal();
            terminal.id = token.getId() != null ? token.getId() : sentence.id + "_" + nodeNum;
            if (token.getPos() != null) {
                terminal.pos = token.getPos().getPosValue();
            }
            if (token.getLemma() != null) {
                terminal.lemma = token.getLemma().getValue();
            }
            terminal.word = token.getCoveredText();
            sentence.graph.terminals.add(terminal);
            nodes.put(token, terminal);
            nodeNum++;
        }
        
        // Convert the parse tree (pass 1: nodes)
        sentence.graph.nonTerminals = new ArrayList<TigerNonTerminal>();
        List<Constituent> constituents = selectCovered(Constituent.class, aSentence);
        for (Constituent constituent : constituents) {
            TigerNonTerminal node = new TigerNonTerminal();
            node.id = sentence.id + "_" + nodeNum;
            node.cat = constituent.getConstituentType();
            node.edges = new ArrayList<TigerEdge>();
            sentence.graph.nonTerminals.add(node);
            nodes.put(constituent, node);
            nodeNum++;
            
            if (constituent.getParent() == null) {
                sentence.graph.root = node.id;
            }
        }

        // Convert the parse tree (pass 2: edges)
        for (Constituent constituent : constituents) {
            TigerNode node = nodes.get(constituent);
            for (FeatureStructure c : create((FSArray<Annotation>) constituent.getChildren())) {
                if (c instanceof Constituent) {
                    String synFun = ((Constituent) c).getSyntacticFunction();
                    TigerEdge edge = new TigerEdge();
                    edge.label = synFun != null ? synFun : "--";
                    edge.idref = nodes.get(c).id;
                    node.edges.add(edge);
                }

                if (c instanceof Token) {
                    TigerEdge edge = new TigerEdge();
                    edge.label = "--";
                    edge.idref = nodes.get(c).id;
                    node.edges.add(edge);
                }
            }
        }
        
        return sentence;
    }
}
