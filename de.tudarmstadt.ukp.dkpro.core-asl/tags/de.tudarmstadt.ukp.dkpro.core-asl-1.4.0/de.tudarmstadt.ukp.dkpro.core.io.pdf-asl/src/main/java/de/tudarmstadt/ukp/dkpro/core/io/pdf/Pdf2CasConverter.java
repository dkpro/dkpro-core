/*******************************************************************************
 * Copyright 2010
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
package de.tudarmstadt.ukp.dkpro.core.io.pdf;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.TextPosition;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;

/**
 * Converts a PDF to a CAS. Uses a substitution table.
 * 
 * @author Richard Eckart de Castilho
 */
public class Pdf2CasConverter
    extends PDFLayoutEventStripper
{
    private final Log log = LogFactory.getLog(getClass());

    private Trie<String> substitutionTable;
    private CAS cas;
    private StringBuilder text;
    private Style regionStyle;
    private StringBuilder regionText;
    private String paragraphType;
    private String headingType;

    public Pdf2CasConverter()
        throws IOException
    {
        super();
    }

    public void writeText(final CAS aCas, final InputStream aIs)
        throws IOException
    {
        final PDDocument doc = PDDocument.load(aIs);

        try {
            if (doc.isEncrypted()) {
                throw new IOException("Encrypted documents currently not supported");
            }

            cas = aCas;
            text = new StringBuilder();

            writeText(doc);
        }
        finally {
            doc.close();
        }
    }

    @Override
    protected void startDocument(final PDDocument aPdf)
        throws IOException
    {
        if (log.isTraceEnabled()) {
            log.trace("<document>");
        }
    }

    @Override
    protected void endDocument(final PDDocument aPdf)
        throws IOException
    {
        cas.setDocumentText(text.toString());

        if (log.isTraceEnabled()) {
            log.trace("</document>");
        }
    }

    @Override
    protected void processLineSeparator()
        throws IOException
    {
        if (log.isTraceEnabled()) {
            log.trace("<br/>");
        }

        if (regionText == null) {
            throw new IllegalStateException("No region started");
        }

        regionText.append("\n");
    }

    @Override
    protected void processWordSeparator()
        throws IOException
    {
        if (log.isTraceEnabled()) {
            log.trace("< >");
        }

        if (regionText == null) {
            throw new IllegalStateException("No region started");
        }

        regionText.append(" ");
    }

    @Override
    protected void startPage(final int aFirstPage, final int aLastPage, final int aCurrentPage,
            final PDPage page)
        throws IOException
    {
        if (log.isTraceEnabled()) {
            log.trace("<page>");
        }

        if (log.isDebugEnabled()) {
            log.debug("Decoding page " + aCurrentPage + " of " + (aLastPage - aFirstPage + 1));
        }
    }

    @Override
    protected void endPage(final int aStartPage, final int aEndPage, final int aCurrentPage,
            final PDPage page)
        throws IOException
    {
        if (log.isTraceEnabled()) {
            log.trace("</page>");
        }
    }

    @Override
    protected void startRegion(final Style aStyle)
        throws IOException
    {
        if (log.isTraceEnabled()) {
            log.trace("<" + aStyle + ">");
        }

        regionStyle = aStyle;
        regionText = new StringBuilder();
    }

    @Override
    protected void endRegion(final Style aStyle)
        throws IOException
    {
        if (log.isTraceEnabled()) {
            log.trace("</" + aStyle + ">");
        }

        if (regionText == null) {
            throw new IllegalStateException("No region started");
        }

        if (regionStyle != aStyle) {
            throw new IllegalStateException("Current region has style " + regionStyle
                    + ", but closing region has style " + aStyle);
        }

        // Append text
        int begin = text.length();
        sanitize(regionText);
        text.append(regionText.toString());
        int end = text.length();
        text.append('\n');

        // Add annotation
        switch (aStyle) {
        case HEADING:
            if (headingType != null) {
                Type t = cas.getTypeSystem().getType(headingType);
                AnnotationFS a = cas.createAnnotation(t, begin, end);
                cas.addFsToIndexes(a);
            }
            break;
        case PARAGRAPH:
            if (paragraphType != null) {
                Type t = cas.getTypeSystem().getType(paragraphType);
                AnnotationFS a = cas.createAnnotation(t, begin, end);
                cas.addFsToIndexes(a);
            }
            break;
        default:
            throw new IllegalStateException("Unknown region style: " + aStyle);
        }

        regionStyle = null;
        regionText = null;
    }

    @Override
    protected void writeCharacters(final TextPosition aText)
        throws IOException
    {
        if (log.isTraceEnabled()) {
            log.trace("[" + aText.getCharacter() + "]");
        }

        if (regionText == null) {
            throw new IllegalStateException("No region started");
        }

        regionText.append(aText.getCharacter());
    }

    private static boolean isValidXMLChar(final int aCodePoint)
    {
        return (aCodePoint == 0x0009) || (aCodePoint == 0x000A) || (aCodePoint == 0x000D)
                || ((0x0020 <= aCodePoint) && (aCodePoint <= 0xD7FF))
                || ((0xE000 <= aCodePoint) && (aCodePoint <= 0xFFFD));
    }

    private StringBuilder sanitize(final StringBuilder aContent)
    {
        int i = 0;
        int lastBreak = 0;
        while (i < aContent.length()) {
            // Check valid unicode char
            if (!isValidXMLChar(aContent.codePointAt(i))) {
                aContent.setCharAt(i, ' ');
                i++;
                continue;
            }

            // Set up how many characters we want to skip
            int seek = i + 1;

            // Do we maybe have an entity?
            if (aContent.charAt(i) == '&') {
                // REC 2006-10-21 Some PDFs seem to have entities and others
                // don't
                // so we may encounter &'s that do not introduce an entity and
                // just ignore them.
                final int end = aContent.indexOf(";", i);
                if (end != -1) {
                    final String cand = aContent.substring(i, end + 1);
                    String r = null;
                    try {
                        if (cand.startsWith("&#x")) {
                            final int cp = Integer.parseInt(cand.substring(2, cand.length() - 1),
                                    16);
                            r = isValidXMLChar(cp) ? String.valueOf(Character.toChars(cp)) : " ";
                        }
                        else if (cand.startsWith("&#")) {
                            final int cp = Integer.parseInt(cand.substring(2, cand.length() - 1));
                            r = isValidXMLChar(cp) ? String.valueOf(Character.toChars(cp)) : " ";
                        }
                        else {
                            // RE 2006-10-22 The chance that there is a & and a
                            // ;
                            // together in a string is quite big. Let's be
                            // tolerant.
                        }
                    }
                    catch (final NumberFormatException e) {
                        log.warn("Invalid numeric entity in fragment [" + cand + "] - Dropping it.");
                    }

                    // Expand the entity and set proper skip (if found)
                    if (r != null) {
                        aContent.replace(i, i + cand.length(), r);
                        seek = i + r.length();
                    }
                }
            }

            // Match against the Trie after numeric entity expansion is over
            if (substitutionTable != null) {
                final Trie<String>.Node match = substitutionTable.getNode(aContent, i);
                if (match != null) {
                    aContent.replace(i, i + match.level, match.value);
                    seek = i + match.value.length();
                }
            }

            // Check line breaks
            while (i < seek) {
                if (aContent.charAt(i) == '\n') {
                    lastBreak = i;
                }
                else if (Character.isWhitespace(aContent.codePointAt(i)) && (i > (lastBreak + 79))) {
                    lastBreak = i;
                    aContent.replace(i, i + 1, "\n");
                }
                i++;
            }
        }

        return aContent;
    }

    public void setSubstitutionTable(Trie<String> aSubstitutionTable)
    {
        substitutionTable = aSubstitutionTable;
    }

    public Trie<String> getSubstitutionTable()
    {
        return substitutionTable;
    }

    public String getParagraphType()
    {
        return paragraphType;
    }

    public void setParagraphType(String aParagraphType)
    {
        paragraphType = aParagraphType;
    }

    public String getHeadingType()
    {
        return headingType;
    }

    public void setHeadingType(String aHeadingType)
    {
        headingType = aHeadingType;
    }
}
