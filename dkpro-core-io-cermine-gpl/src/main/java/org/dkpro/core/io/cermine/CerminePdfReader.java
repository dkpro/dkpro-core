/**
 * Copyright 2007-2017
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

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Heading;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
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
import org.jdom.Element;
import pl.edu.icm.cermine.ContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;

import java.io.IOException;
import java.io.InputStream;

import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * Collection reader for PDF files using CERMINE
 * <a href="https://github.com/CeON/CERMINE">https://github.com/CeON/CERMINE</a>.
 */
@ResourceMetaData(name = "CERMINE PDF Reader")
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
     * If set to true the converter will normalize all whitespaces (e.g. tab, newline) to a single
     * whitespace
     */
    public static final String PARAM_NORMALIZE_TEXT = "normalizeText";
    @ConfigurationParameter(name = PARAM_NORMALIZE_TEXT, mandatory = false, defaultValue = "false")
    private boolean normalizeText;
    
    /**
     * If set to true the converter will discard all of the detected citations
     */
    public static final String PARAM_IGNORE_CITATIONS = "ignoreCitations";
    @ConfigurationParameter(name = PARAM_IGNORE_CITATIONS, mandatory = false, defaultValue = "false")
    private boolean ignoreCitations;

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
                .withIgnoreCitations(ignoreCitations);
    }

    @Override
    public void getNext(CAS aCAS) throws IOException, CollectionException
    {
        Resource res = nextFile();
        initCas(aCAS, res);

        InputStream is = null;

        try {
            is = res.getInputStream();

            // Process PDF
            ContentExtractor extractor = new ContentExtractor();
            extractor.setPDF(is);
            Element result = extractor.getContentAsNLM();
            nlmHandler.process(result, aCAS);

            // Set up language
            if (getConfigParameterValue(PARAM_LANGUAGE) != null) {
                aCAS.setDocumentLanguage((String) getConfigParameterValue(PARAM_LANGUAGE));
            }
        }
        catch (AnalysisException e) {
            throw new IOException("An exception occurred while processing the PDF document.", e);
        }
        finally {
            closeQuietly(is);
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

        private int beginIndex;

        public NlmHandler withParagraphAnnotation(String paragraphAnnotation)
        {
            paragraphType = paragraphAnnotation;
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
            for (Object node : root.getChildren()) {
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
                    parseBack(element);

                    // close the previous open paragraph, if any
                    makeAnnotation(paragraphType);
                }
            }
        }

        private void parseHeader(Element root)
        {
            if (root.getChildren().isEmpty() && !root.getValue().isEmpty()) {
                if (!ignoreCitations || !isCitationElement(root)) {
                    sb.append(DELIMITER).append(normalizeString(root.getValue()));
                }
                return;
            }
            for (Object node : root.getChildren()) {
                Element element = (Element) node;
                if (element.getName().equals("title") || element.getName().equals("subtitle")
                        || element.getName().equals("trans-title-group")
                        || element.getName().equals("alt-title")
                        || element.getName().equals("article-title")) {
                    // close the previous open paragraph, if any
                    makeAnnotation(paragraphType);

                    if (element.getName().equals("title")
                            || element.getName().equals("article-title")) {
                        title = element.getValue();
                    }
                    makeAnnotationFromElement(element, headingType);
                }
                else {
                    parseHeader(element);
                }
            }
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
            if (root.getChildren().isEmpty() && !root.getValue().isEmpty()) {
                if (!ignoreCitations || !isCitationElement(root)) {
                    sb.append(DELIMITER).append(normalizeString(root.getValue()));
                }
                return;
            }

            for (Object node : root.getChildren()) {
                Element element = (Element) node;
                if (element.getName().equals("title") || element.getName().equals("p")) {
                    // close the previous open paragraph, if any
                    makeAnnotation(paragraphType);

                    String annotation = element.getName().equals("title") ? headingType
                            : paragraphType;
                    makeAnnotationFromElement(element, annotation);
                }
                else {
                    parseBody(element);
                }
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
                AnnotationFS a = cas.createAnnotation(t, beginIndex, sb.length());
                cas.addFsToIndexes(a);
                updateCursor();
            }
        }

        private void parseText(Element root)
        {
            if (root.getChildren().isEmpty() && !root.getValue().isEmpty()) {
                if (!ignoreCitations || !isCitationElement(root)) {
                    sb.append(DELIMITER).append(normalizeString(root.getValue()));
                }
            }
            else {
                for (Object node : root.getChildren()) {
                    Element element = (Element) node;
                    parseText(element);
                }
            }
        }

        private void parseBack(Element root)
        {
            parseText(root);
        }

        protected String normalizeString(String input)
        {
            if (normalizeText)
                return input.replaceAll("\\s+", " ");
            else
                return input;
        }
    }
}
