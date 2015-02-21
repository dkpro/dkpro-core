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
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

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
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma" })
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
                String teiElement = getTeiTag(nextAnnot);
                if (teiElement == null) {
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
                        xmlEventWriter.add(xmlef.createStartElement(new QName(TEI_NS, teiElement),
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
                    xmlEventWriter.add(xmlef.createEndElement(new QName(TEI_NS, teiElement), null));

                    pos = cur.getEnd();
                    cur = stack.pop();
                }
            }

            // End of text, end all elements that are still on the stack
            if (cur != null) {
                xmlEventWriter.add(xmlef.createCharacters(text.substring(pos, cur.getEnd())));
                pos = cur.getEnd();
                xmlEventWriter.add(xmlef.createEndElement(new QName(TEI_NS, getTeiTag(cur)), null));

                while (!stack.isEmpty()) {
                    cur = stack.pop();
                    if (cur == null) {
                        break;
                    }
                    xmlEventWriter.add(xmlef.createCharacters(text.substring(pos, cur.getEnd())));
                    pos = cur.getEnd();
                    xmlEventWriter.add(xmlef.createEndElement(new QName(TEI_NS, getTeiTag(cur)), null));
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
        return attributes.iterator();
    }
    
    private String getTeiTag(Annotation aAnnotation)
    {
        if (aAnnotation.getTypeIndexID() == Token.type) {
            if (cTextPattern.matcher(aAnnotation.getCoveredText()).matches()) {
                return "c";
            }
            return "w";
        }
        else if (aAnnotation.getTypeIndexID() == Sentence.type) {
            return "s";
        }
        else if (aAnnotation.getTypeIndexID() == Paragraph.type) {
            return "p";
        }
        else {
            return null;
        }
    }
}
