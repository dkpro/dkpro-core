/*******************************************************************************
 * Copyright 2015
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.tei;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.regex.Pattern;

import javanet.staxutils.IndentingXMLEventWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.events.Attribute;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;

/**
 * UIMA CAS consumer writing the CAS document text in TEI format.
 */
@TypeCapability(
        inputs = { 
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma",
                "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent" })
public class TeiWriter
    extends JCasFileWriter_ImplBase
{
    /**
     * Specify the suffix of output files. Default value <code>.xml</code>. If the suffix is not
     * needed, provide an empty string as value.
     */
    public static final String PARAM_FILENAME_SUFFIX = "filenameSuffix";
    @ConfigurationParameter(name = PARAM_FILENAME_SUFFIX, mandatory = true, defaultValue = ".xml")
    private String filenameSuffix;

    /**
     * A token matching this pattern is rendered as a TEI "c" element instead of a "w" element.
     */
    public static final String PARAM_C_TEXT_PATTERN = "cTextPattern";
    @ConfigurationParameter(name = PARAM_C_TEXT_PATTERN, mandatory = true, defaultValue = "[,.:;()]|(``)|('')|(--)")
    private Pattern cTextPattern;

    /**
     * Write constituent annotations to the CAS. Disabled by default because it requires type
     * priorities to be set up (Constituents must have a higher prio than Tokens).
     */
    public static final String PARAM_WRITE_CONSTITUENT = ComponentParameters.PARAM_WRITE_CONSTITUENT;
    @ConfigurationParameter(name = PARAM_WRITE_CONSTITUENT, mandatory = true, defaultValue = "false")
    private boolean writeConstituent;
    
    /**
     * Indent the XML.
     */
    public static final String PARAM_INDENT = "indent";
    @ConfigurationParameter(name = PARAM_INDENT, mandatory = true, defaultValue = "false")
    private boolean indent;

    private XMLEventFactory xmlef = XMLEventFactory.newInstance();

    private String TEI_NS = "http://www.tei-c.org/ns/1.0";
    private QName E_TEI_TEI = new QName(TEI_NS, "TEI");
    private QName E_TEI_HEADER = new QName(TEI_NS, "teiHeader");
    private QName E_TEI_FILE_DESC = new QName(TEI_NS, "fileDesc");
    private QName E_TEI_TITLE_STMT = new QName(TEI_NS, "titleStmt");
    private QName E_TEI_TITLE = new QName(TEI_NS, "title");
    private QName E_TEI_TEXT = new QName(TEI_NS, "text");
    private QName E_TEI_BODY = new QName(TEI_NS, "body");
    
    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        String text = aJCas.getDocumentText();

        try (OutputStream docOS = getOutputStream(aJCas, filenameSuffix)) {
            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            xmlOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);

            XMLEventWriter xmlEventWriter = xmlOutputFactory.createXMLEventWriter(docOS);
            if (indent) {
                xmlEventWriter = new IndentingXMLEventWriter(xmlEventWriter);
            }

            xmlEventWriter.add(xmlef.createStartDocument());
            xmlEventWriter.setDefaultNamespace(TEI_NS);
            xmlEventWriter.add(xmlef.createStartElement(E_TEI_TEI, null, null));

            // Render header
            DocumentMetaData meta = DocumentMetaData.get(aJCas);
            xmlEventWriter.add(xmlef.createStartElement(E_TEI_HEADER, null, null));
            xmlEventWriter.add(xmlef.createStartElement(E_TEI_FILE_DESC, null, null));
            xmlEventWriter.add(xmlef.createStartElement(E_TEI_TITLE_STMT, null, null));
            xmlEventWriter.add(xmlef.createStartElement(E_TEI_TITLE, null, null));
            xmlEventWriter.add(xmlef.createCharacters(meta.getDocumentTitle()));
            xmlEventWriter.add(xmlef.createEndElement(E_TEI_TITLE, null));
            xmlEventWriter.add(xmlef.createEndElement(E_TEI_TITLE_STMT, null));
            xmlEventWriter.add(xmlef.createEndElement(E_TEI_FILE_DESC, null));
            xmlEventWriter.add(xmlef.createEndElement(E_TEI_HEADER, null));
            
            // Render text
            xmlEventWriter.add(xmlef.createStartElement(E_TEI_TEXT, null, null));
            xmlEventWriter.add(xmlef.createStartElement(E_TEI_BODY, null, null));
            
            FSIterator<Annotation> iterator = aJCas.getAnnotationIndex().iterator();

            Stack<Annotation> stack = new Stack<Annotation>();
            int pos = 0;
            Annotation cur = null;

            while (iterator.isValid()) {
                Annotation nextAnnot = iterator.get();

                // Ignore unmapped elements
                Optional<String> teiElement = getTeiTag(nextAnnot);
                if (!teiElement.isPresent()) {
                    iterator.moveToNext();
                    continue;
                }

                // Check if next annotation is potentially nested
                if (cur == null || nextAnnot.getBegin() < cur.getEnd()) {
                    // Check if next annotation is fully nested
                    if (cur == null || nextAnnot.getEnd() <= cur.getEnd()) {
                        // Text between current and next annotation
                        xmlEventWriter.add(xmlef.createCharacters(text.substring(pos,
                                nextAnnot.getBegin())));
                        // Next annotation
                        xmlEventWriter.add(xmlef.createStartElement(new QName(TEI_NS, teiElement.get()),
                                getAttributes(nextAnnot), null));

                        stack.push(cur);
                        cur = nextAnnot;
                        pos = nextAnnot.getBegin();
                    }
                    else {
                        // Overlapping annotations are ignored
                        getLogger().debug("Unable to render overlapping annotation");
                    }
                    iterator.moveToNext();
                }
                // Next annotation is following, not nested
                else {
                    // Text between current and next annotation
                    xmlEventWriter.add(xmlef.createCharacters(text.substring(pos, cur.getEnd())));
                    xmlEventWriter.add(xmlef.createEndElement(new QName(TEI_NS, teiElement.get()), null));

                    pos = cur.getEnd();
                    cur = stack.pop();
                }
            }

            // End of text, end all elements that are still on the stack
            if (cur != null) {
                xmlEventWriter.add(xmlef.createCharacters(text.substring(pos, cur.getEnd())));
                pos = cur.getEnd();
                xmlEventWriter.add(xmlef.createEndElement(new QName(TEI_NS, getTeiTag(cur).get()), null));

                while (!stack.isEmpty()) {
                    cur = stack.pop();
                    if (cur == null) {
                        break;
                    }
                    xmlEventWriter.add(xmlef.createCharacters(text.substring(pos, cur.getEnd())));
                    pos = cur.getEnd();
                    xmlEventWriter.add(xmlef.createEndElement(new QName(TEI_NS, getTeiTag(cur).get()), null));
                }
            }

            if (pos < text.length()) {
                xmlEventWriter.add(xmlef.createCharacters(text.substring(pos, text.length())));
            }

            xmlEventWriter.add(xmlef.createEndElement(E_TEI_BODY, null));
            xmlEventWriter.add(xmlef.createEndElement(E_TEI_TEXT, null));
            xmlEventWriter.add(xmlef.createEndElement(E_TEI_TEI, null));
            xmlEventWriter.add(xmlef.createEndDocument());
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private Iterator<Attribute> getAttributes(Annotation aAnnotation) {
        List<Attribute> attributes = new ArrayList<Attribute>();
        if (aAnnotation instanceof Token) {
            Token t = (Token) aAnnotation;
            if (t.getPos() != null) {
                attributes.add(xmlef.createAttribute("type", t.getPos().getPosValue()));
            }
            if (t.getLemma() != null) {
                attributes.add(xmlef.createAttribute("lemma", t.getLemma().getValue()));
            }
        }
        else if (aAnnotation instanceof Constituent) {
            Constituent c = (Constituent) aAnnotation;
            if ("ROOT".equals(c.getConstituentType())) {
                System.out.println();
            }
            if (c.getConstituentType() != null) {
                attributes.add(xmlef.createAttribute("type", c.getConstituentType()));
            }
            if (c.getSyntacticFunction() != null) {
                attributes.add(xmlef.createAttribute("function", c.getSyntacticFunction()));
            }
        } {
            
        }
        return attributes.iterator();
    }
    
    private Optional<String> getTeiTag(Annotation aAnnotation)
    {
        if (aAnnotation instanceof Constituent) {
            Constituent c = (Constituent) aAnnotation;
            if ("ROOT".equals(c.getConstituentType())) {
                System.out.println();
            }
        }
        
        if (aAnnotation.getTypeIndexID() == Token.type) {
            if (cTextPattern.matcher(aAnnotation.getCoveredText()).matches()) {
                return Optional.of("c");
            }
            return Optional.of("w");
        }
        else if (aAnnotation.getTypeIndexID() == Sentence.type) {
            return Optional.of("s");
        }
        else if (aAnnotation.getTypeIndexID() == Paragraph.type) {
            return Optional.of("p");
        }
        else if (writeConstituent && (aAnnotation instanceof ROOT)) {
            // We do not render ROOT nodes
            return Optional.empty();
        }
        else if (writeConstituent && (aAnnotation instanceof Constituent)) {
            return Optional.of("phr");
        }
        else {
            return Optional.empty();
        }
    }
}
