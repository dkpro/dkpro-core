/*
 * Copyright 2007-2018
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/ .
 */
package org.dkpro.core.io.cermine;

import java.io.IOException;
import java.io.InputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.segmentation.SegmenterBase;
import org.jdom.Element;
import org.jdom.Text;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Heading;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import eu.openminted.share.annotations.api.DocumentationResource;
import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;

/**
 * Collection reader for PDF files using CERMINE
 * <a href="https://github.com/CeON/CERMINE">https://github.com/CeON/CERMINE</a>.
 */
@ResourceMetaData(name = "CERMINE PDF Reader")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({ MimeTypes.APPLICATION_PDF })
@TypeCapability(outputs = { "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Heading",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph" })
public class CerminePdfReader
    extends ResourceCollectionReaderBase
{
    public static final String BUILT_IN = "<built-in>";

    /**
     * The type used to annotate headings.
     */
    public static final String PARAM_HEADING_TYPE = "headingType";
    @ConfigurationParameter(name = PARAM_HEADING_TYPE, mandatory = false, defaultValue = BUILT_IN)
    private String headingType;

    /**
     * The type used to annotate paragraphs.
     */
    public static final String PARAM_PARAGRAPH_TYPE = "paragraphType";
    @ConfigurationParameter(name = PARAM_PARAGRAPH_TYPE, mandatory = false, defaultValue = BUILT_IN)
    private String paragraphType;
    
    /**
     * If set to true the reader will normalize all whitespaces (e.g. tab, newline) to a single
     * whitespace
     */
    public static final String PARAM_NORMALIZE_TEXT = "normalizeText";
    @ConfigurationParameter(name = PARAM_NORMALIZE_TEXT, mandatory = false, defaultValue = "false")
    private boolean normalizeText;
    
    /**
     * If set to true the reader will discard all of the detected citations
     */
    public static final String PARAM_IGNORE_CITATIONS = "ignoreCitations";
    @ConfigurationParameter(name = PARAM_IGNORE_CITATIONS, mandatory = false, defaultValue = "false")
    private boolean ignoreCitations;
    
    /**
     * If set to true the reader will discard all of the detected text in the references section
     */
    public static final String PARAM_IGNORE_REFERENCES_SECTION = "ignoreReferencesSection";
    @ConfigurationParameter(name = PARAM_IGNORE_REFERENCES_SECTION, mandatory = false, defaultValue = "false")
    private boolean ignoreReferencesSection;

    private NlmHandler nlmHandler;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException
    {
        super.initialize(aContext);

        if (BUILT_IN.equals(headingType)) {
            headingType = Heading.class.getName();
        }

        if (BUILT_IN.equals(paragraphType)) {
            paragraphType = Paragraph.class.getName();
        }

        nlmHandler = new NlmHandler()
                .withHeadingAnnotation(headingType)
                .withParagraphAnnotation(paragraphType)
                .withNormalizeText(normalizeText)
                .withIgnoreCitations(ignoreCitations)
                .withIgnoreReferencesSection(ignoreReferencesSection);
    }

    @Override
    public void getNext(CAS aCAS) throws IOException, CollectionException
    {
        Resource res = nextFile();
        initCas(aCAS, res);

        try (InputStream is = res.getInputStream()) {
            // Process PDF
            ContentExtractor extractor = new ContentExtractor();
            extractor.setPDF(is);
            Element result = extractor.getContentAsNLM();
            nlmHandler.process(result, aCAS);

            // FIXME Setting the language below should not be needed- initCas() should already
            // be taking care of this. Double-check and remove if not necessary.
            // Set up language
            if (getConfigParameterValue(PARAM_LANGUAGE) != null) {
                aCAS.setDocumentLanguage((String) getConfigParameterValue(PARAM_LANGUAGE));
            }
        }
        catch (AnalysisException e) {
            throw new IOException("An exception occurred while processing the PDF document.", e);
        }
    }

    /**
     * A handler for JATZ-NLM format
     * <a href="https://jats.nlm.nih.gov/archiving/tag-library/1.1/index.html">https://jats.nlm.nih.gov/archiving/tag-library/1.1/index.html</a>
     */
    private class NlmHandler
    {
        private static final String DELIMITER = " ";
        StringBuilder sb;
        String title;
        private CAS cas;

        private String paragraphType;
        private String headingType;
        private boolean normalizeText;
        private boolean ignoreCitations;
        private boolean ignoreReferencesSection;

        private int beginIndex;

        public NlmHandler withParagraphAnnotation(String paragraphAnnotation)
        {
            paragraphType = paragraphAnnotation;
            return this;
        }

        public NlmHandler withIgnoreReferencesSection(boolean aIgnoreReferencesSection)
        {
            ignoreReferencesSection = aIgnoreReferencesSection;
            return this;
        }

        public NlmHandler withHeadingAnnotation(String headingAnnotation)
        {
            headingType = headingAnnotation;
            return this;
        }

        public NlmHandler withNormalizeText(boolean isNormalizeText)
        {
            normalizeText = isNormalizeText;
            return this;
        }

        public NlmHandler withIgnoreCitations(boolean isIgnoreCitations)
        {
            ignoreCitations = isIgnoreCitations;
            return this;
        }

        public void process(Element root, CAS aCas)
        {
            sb = new StringBuilder();
            title = "UNKNOWN";
            beginIndex = 0;
            cas = aCas;

            parse(root);

            cas.setDocumentText(sb.toString());
            DocumentMetaData.get(cas).setDocumentTitle(title);
            DocumentMetaData.get(cas).setDocumentId(title);
        }

        private void parse(Element root)
        {
            beginIndex = 0;
            for (Object node : root.getContent()) {
                Element element = (Element) node;
                if (element.getName().equals("front")) {
                    parseHeader(element);
                    // close the previous open paragraph, if any
                    makeAnnotation(paragraphType);
                }
                else if (element.getName().equals("body")) {
                    parseBody(element);
                    // close the previous open paragraph, if any
                    makeAnnotation(paragraphType);
                }
                else if (element.getName().equals("back")) {
                    if (!ignoreReferencesSection) {
                        parseBack(element);
                        // close the previous open paragraph, if any
                        makeAnnotation(paragraphType);
                    }
                }
            }
        }

        private void parseHeader(Element root)
        {
            if (isWriteableLeafNode(root)) {
                writeLeafNode(root);
                return;
            }
            for (Object node : root.getContent()) {
                if (isTextNode(node)) {
                    writeTextNode(node);
                }
                else if (isElementNode(node)) {
                    Element element = (Element) node;
                    if (isArticleTitleElement(element)) {
                        // close the previous open paragraph, if any
                        makeAnnotation(paragraphType);

                        if (isArticleMainTitleElement(element)) {
                            title = element.getValue();
                        }
                        makeAnnotationFromElement(element, headingType);
                    }
                    else {
                        parseHeader(element);
                    }
                }                
            }
        }

        private boolean isArticleMainTitleElement(Element element)
        {
            return element.getName().equals("title")
                    || element.getName().equals("article-title");
        }

        private boolean isArticleTitleElement(Element element)
        {
            return isArticleMainTitleElement(element) || element.getName().equals("subtitle")
                    || element.getName().equals("trans-title-group")
                    || element.getName().equals("alt-title");
        }

        private void makeAnnotationFromElement(Element element, String annotation)
        {
            beginIndex = sb.length();
            parseText(element);
            makeAnnotation(annotation);
            updateCursor();
        }

        private void parseBody(Element root)
        {
            if (isWriteableLeafNode(root)) {
                writeLeafNode(root);
                return;
            }

            for (Object node : root.getContent()) {
                if (isTextNode(node)) {
                    writeTextNode(node);
                }
                else if (isElementNode(node)) {
                    Element element = (Element) node;
                    if (isTitleElement(element) || isParagraphElement(element)) {
                        // close the previous open paragraph, if any
                        makeAnnotation(paragraphType);

                        String annotation = isTitleElement(element) ? headingType
                                : paragraphType;
                        makeAnnotationFromElement(element, annotation);
                    }
                    else {
                        parseBody(element);
                    }
                }                
            }
        }

        private boolean isParagraphElement(Element element)
        {
            return element.getName().equals("p");
        }

        private boolean isTitleElement(Element element)
        {
            return element.getName().equals("title");
        }

        private void writeTextNode(Object node)
        {
            Text text = (Text) node;
            sb.append(DELIMITER).append(normalizeString(text.getValue()));
        }

        private boolean isElementNode(Object node)
        {
            return node instanceof Element;
        }

        private boolean isTextNode(Object node)
        {
            return node instanceof Text;
        }

        private boolean isWriteableLeafNode(Element node)
        {
            return node.getChildren().isEmpty() && !node.getValue().isEmpty();
        }

        private void writeLeafNode(Element node)
        {
            if (!ignoreCitations || !isCitationElement(node)) {
                sb.append(DELIMITER).append(normalizeString(node.getValue()));
            }
        }

        private boolean isCitationElement(Element root)
        {
            String name = root.getName();
            return name.equals("element-citation") || name.equals("mixed-citation")
                    || name.equals("nlm-citation") || name.equals("citation-alternatives")
                    || name.equals("xref");
        }

        private void updateCursor()
        {
            beginIndex = sb.length();
        }

        private void makeAnnotation(String annotationType)
        {
            if (beginIndex < sb.length()) {
                Type t = cas.getTypeSystem().getType(annotationType);
                
                // Trim leading/trailing whitespace
                int[] offsets = {beginIndex, sb.length()};
                SegmenterBase.trim(sb, offsets);
                
                AnnotationFS a = cas.createAnnotation(t, offsets[0], offsets[1]);
                cas.addFsToIndexes(a);
                updateCursor();
            }
        }

        private void parseText(Element root)
        {
            if (isWriteableLeafNode(root)) {
                writeLeafNode(root);
            }
            else {
                for (Object node : root.getContent()) {
                    if (isTextNode(node)) {
                        writeTextNode(node);
                    }
                    else if (isElementNode(node)) {
                        Element element = (Element) node;
                        parseText(element);
                    }
                }
            }
        }

        private void parseBack(Element root)
        {
            parseText(root);
        }

        protected String normalizeString(String input)
        {
            if (normalizeText) {
                return input.replaceAll("\\s+", " ");
            }
            else {
                return input;
            }
        }
    }
}
