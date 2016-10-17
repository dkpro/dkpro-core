/*
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
 *
 * This code is based on the PDFTextStripper written by Ben Litchfield from
 * the PDFbox 0.7.x project and licensed under the BSD license. In accordance
 * with the terms of this license, the following copyright statement is retained:
 *
 * Copyright (c) 2003-2007, www.pdfbox.org
 * All rights reserved.
 *
 * Furthermore the modified code is re-licensed under the Apache License,
 * Version 2.0 as stated above.
 */
package de.tudarmstadt.ukp.dkpro.core.io.pdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.interactive.pagenavigation.PDThreadBead;
import org.apache.pdfbox.util.PDFStreamEngine;
import org.apache.pdfbox.util.ResourceLoader;
import org.apache.pdfbox.util.TextPosition;

/**
 * This class will take a PDF document and strip out all of the text and ignore the formatting and
 * such. Please note; it is up to clients of this class to verify that a specific user has the
 * correct permissions to extract text from the PDF document.
 * <p>
 * This class is based on the pdfbox 1.7.0 PDFTextStripper class and was substantially modified and
 * enhanced for basic paragraph and heading detection. Unfortunately it was not possible to add
 * these enhancements through sub-classing, thus the code was copied and adapted.
 */
public abstract class PdfLayoutEventStripper
    extends PDFStreamEngine
{
    public static enum Values
    {
        LEFT, RIGHT, TOP, BOTTOM, LINESPACING, LINEHEIGHT
    }

    public static enum Style
    {
        PAGE, PARAGRAPH, HEADING
    }

    private PDDocument document;

    private int currentPageNo = 0;
    private int startPage = 1;
    private int maxPage = 0;
    private int endPage = Integer.MAX_VALUE;
    private boolean suppressDuplicateOverlappingText = true;
    private boolean shouldSeparateByBeads = true;

    private List<PDThreadBead> pageArticles = null;
    /**
     * The charactersByArticle is used to extract text by article divisions. For example a PDF that
     * has two columns like a newspaper, we want to extract the first column and then the second
     * column. In this example the PDF would have 2 beads(or articles), one for each column. The
     * size of the charactersByArticle would be 5, because not all text on the screen will fall into
     * one of the articles. The five divisions are shown below
     * 
     * Text before first article first article text text between first article and second article
     * second article text text after second article
     * 
     * Most PDFs won't have any beads, so charactersByArticle will contain a single entry.
     */
    protected Vector<List<TextPosition>> charactersByArticle = new Vector<List<TextPosition>>();

    private final Map<String, List<TextPosition>> characterListMapping = new HashMap<String, List<TextPosition>>();

    /**
     * Instantiate a new PDFTextStripper object. This object will load properties from
     * Resources/PDFTextStripper.properties.
     * 
     * @throws IOException
     *             If there is an error loading the properties.
     */
    public PdfLayoutEventStripper()
        throws IOException
    {
        super(ResourceLoader.loadProperties(
                "org/apache/pdfbox/resources/PDFTextStripper.properties", true));
    }

    /**
     * Instantiate a new PDFTextStripper object. Loading all of the operator mappings from the
     * properties object that is passed in.
     * 
     * @param props
     *            The properties containing the mapping of operators to PDFOperator classes.
     * 
     * @throws IOException
     *             If there is an error reading the properties.
     */
    public PdfLayoutEventStripper(final Properties props)
        throws IOException
    {
        super(props);
    }

    /**
     * This will take a PDDocument and write the text of that document to the print writer.
     * 
     * @param doc
     *            The document to get the data from.
     * 
     * @throws IOException
     *             If the doc is in an invalid state.
     */
    public void writeText(final PDDocument doc)
        throws IOException
    {
        resetEngine();

        currentPageNo = 0;
        document = doc;
        startDocument(document);

        if (document.isEncrypted()) {
            // We are expecting non-encrypted documents here, but it is common
            // for users to pass in a document that is encrypted with an empty
            // password (such a document appears to not be encrypted by
            // someone viewing the document, thus the confusion). We will
            // attempt to decrypt with the empty password to handle this case.
            //
            try {
                document.decrypt("");
            }
            catch (CryptographyException e) {
                throw new IOException("Error decrypting document, details: ", e);
            }
            catch (InvalidPasswordException e) {
                throw new IOException("Error: document is encrypted", e);
            }
        }

        processPages(document.getDocumentCatalog().getAllPages());
        endDocument(document);
    }

    /**
     * This will process all of the pages and the text that is in them.
     * 
     * @param pages
     *            The pages object in the document.
     * 
     * @throws IOException
     *             If there is an error parsing the text.
     */
    protected void processPages(List<PDPage> pages)
        throws IOException
    {
        maxPage = pages.size();

        for (final PDPage page : pages) {
            currentPageNo++;
            final PDStream contentStream = page.getContents();
            if (contentStream != null) {
                final COSStream contents = contentStream.getStream();
                processPage(page, contents);
            }
        }
    }

    /**
     * This will process the contents of a page.
     * 
     * @param page
     *            The page to process.
     * @param content
     *            The contents of the page.
     * 
     * @throws IOException
     *             If there is an error processing the page.
     */
    protected void processPage(final PDPage page, final COSStream content)
        throws IOException
    {
        if ((currentPageNo >= startPage) && (currentPageNo <= endPage)) {
            startPage(startPage, Math.min(maxPage, endPage), currentPageNo, page);
            pageArticles = page.getThreadBeads();
            int numberOfArticleSections = 1 + pageArticles.size() * 2;
            if (!shouldSeparateByBeads) {
                numberOfArticleSections = 1;
            }
            final int originalSize = charactersByArticle.size();
            charactersByArticle.setSize(numberOfArticleSections);
            for (int i = 0; i < numberOfArticleSections; i++) {
                if (numberOfArticleSections < originalSize) {
                    charactersByArticle.get(i).clear();
                }
                else {
                    charactersByArticle.set(i, new ArrayList<TextPosition>());
                }
            }

            characterListMapping.clear();

            // processStream will call showCharacter were we will simply
            // collect all the TextPositions for the page
            processStream(page, page.findResources(), content);

            // Now we do the real processing
            for (int i = 0; i < charactersByArticle.size(); i++) {
                processArticle(charactersByArticle.get(i));
            }

            endPage(startPage, endPage, currentPageNo, page);
        }
    }

    /**
     * This method tries do detect headings and paragraphs and line boundaries.
     * 
     * @param textList
     *            the text.
     * @throws IOException
     *             if there is an error writing to the stream.
     */
    protected void processArticle(final List<TextPosition> textList)
        throws IOException
    {
        // Nothing to do in this article?
        if (textList.size() == 0) {
            return;
        }

        // System.out.println("XScale: "+textList.get(0).getXScale());
        // System.out.println("YScale: "+textList.get(0).getYScale());

        final int prediction_depth = 10;
        Prediction pred = null;
        final Block block = new Block(textList, 0);
        Line currentLine = null;

        boolean newRegion = false;
        Style currentStyle = null;
        Style prevStyle = null;
        int cur = 0;
        while (cur < textList.size()) {
            // Initialize the line (if not already done)
            if (currentLine == null) {
                currentLine = new Line(textList, cur);

                // Get the style for the line (base on style for current
                // element)
                prevStyle = currentStyle;
                currentStyle = getStyle(textList.get(cur));

                // Test for a style change
                if ((newRegion) || (prevStyle != currentStyle)) {
                    if (newRegion) {
                        newRegion = false;
                    }
                    // On a style change issue the proper events
                    if (prevStyle != null) {
                        endRegion(prevStyle);
                    }
                    startRegion(currentStyle);
                    pred = predictGeneralStructure(textList, cur, prediction_depth);
                }
            }

            // Check if we left the line
            if (!currentLine.withinLine(textList.get(cur))
                    && !currentLine.isSuperscript(textList.get(cur))
                    && !currentLine.isSubscript(textList.get(cur))) {
                // We left the line
                currentLine = null;

                // Check if we left the region
                final boolean columnSwitch = isColumnSwitch(textList.get(cur), block);
                final boolean leftIndented = isLeftIndented(textList.get(cur), pred);
                final boolean leftOutdented = isLeftOutdented(textList.get(cur), pred);
                // boolean fontSwitch = (fontSize[cur] != fontSize[cur-1]);
                final boolean vAdjacent = isVerticallyAdjacent(textList.get(cur).getY(), textList
                        .get(cur - 1).getY(), block.linespacing);

                if (!columnSwitch && !leftIndented && !leftOutdented &&
                /* !fontSwitch && */vAdjacent) {
                    // Same region. Issue a line separator and restart
                    processLineSeparator();
                }
                else {
                    // New region
                    newRegion = true;
                    block.reset(cur);

                    if ((pred == null) || !vAdjacent) {
                        pred = predictGeneralStructure(textList, cur, prediction_depth);
                    }
                    else if (vAdjacent) {
                        // If the block is directly adjacent, we may be better
                        // of
                        // with the old prediction... let's see if we can get a
                        // comparatively good new one.
                        final Prediction new_pred = predictGeneralStructure(textList, cur,
                                prediction_depth);
                        final boolean badPred = isSignifiantlyWorse(new_pred.quality, pred.quality,
                                0.4);
                        if (!badPred) {
                            pred = new_pred;
                        }
                    }
                }

                continue; // Start again to create a new currentLine
            }

            // Ok, we are in the same line still.

            // Let's check if the block is adjacent or needs a space
            // if (!isRightAdjacent(textList, cur, cur-1, cur-2)) {
            if ((cur > 0) && !isNextChar(textList.get(cur), textList.get(cur - 1))) {
                processWordSeparator();
            }

            // Grow the current block to calculate better spacings.
            block.grow(cur);

            // Write of the characters and advance.
            writeCharacters(textList.get(cur));
            cur++;
        }

        // Close region
        if (currentStyle != null) {
            endRegion(currentStyle);
        }
    }

    /**
     * This will show add a character to the list of characters to be printed to the text file.
     * 
     * @param text
     *            The description of the character to display.
     */
    @Override
    protected void processTextPosition(final TextPosition text)
    {
        boolean showCharacter = true;
        if (suppressDuplicateOverlappingText) {
            showCharacter = false;
            final String textCharacter = text.getCharacter();
            final float textX = text.getX();
            final float textY = text.getY();
            List<TextPosition> sameTextCharacters = characterListMapping.get(textCharacter);
            if (sameTextCharacters == null) {
                sameTextCharacters = new ArrayList<TextPosition>();
                characterListMapping.put(textCharacter, sameTextCharacters);
            }

            // RDD - Here we compute the value that represents the end of the
            // rendered
            // text. This value is used to determine whether subsequent text
            // rendered
            // on the same line overwrites the current text.
            //
            // We subtract any positive padding to handle cases where extreme
            // amounts
            // of padding are applied, then backed off (not sure why this is
            // done, but there
            // are cases where the padding is on the order of 10x the character
            // width, and
            // the TJ just backs up to compensate after each character). Also,
            // we subtract
            // an amount to allow for kerning (a percentage of the width of the
            // last
            // character).
            //
            boolean suppressCharacter = false;
            final float tolerance = (text.getWidth() / textCharacter.length()) / 3.0f;
            for (int i = 0; i < sameTextCharacters.size() && textCharacter != null; i++) {
                final TextPosition character = sameTextCharacters.get(i);
                final String charCharacter = character.getCharacter();
                final float charX = character.getX();
                final float charY = character.getY();
                // only want to suppress

                if (charCharacter != null &&
                // charCharacter.equals( textCharacter ) &&
                        within(charX, textX, tolerance) && within(charY, textY, tolerance)) {
                    suppressCharacter = true;
                }
            }
            if (!suppressCharacter && (text.getCharacter() != null)
                    && (text.getCharacter().length() > 0)) {
                sameTextCharacters.add(text);
                showCharacter = true;
            }
        }

        if (showCharacter) {
            // if we are showing the character then we need to determine which
            // article it belongs to.
            int foundArticleDivisionIndex = -1;
            int notFoundButFirstLeftAndAboveArticleDivisionIndex = -1;
            int notFoundButFirstLeftArticleDivisionIndex = -1;
            int notFoundButFirstAboveArticleDivisionIndex = -1;
            final float x = text.getX();
            final float y = text.getY();
            if (shouldSeparateByBeads) {
                for (int i = 0; i < pageArticles.size() && foundArticleDivisionIndex == -1; i++) {
                    final PDThreadBead bead = pageArticles.get(i);
                    if (bead != null) {
                        final PDRectangle rect = bead.getRectangle();
                        if (rect.contains(x, y)) {
                            foundArticleDivisionIndex = i * 2 + 1;
                        }
                        else if ((x < rect.getLowerLeftX() || y < rect.getUpperRightY())
                                && notFoundButFirstLeftAndAboveArticleDivisionIndex == -1) {
                            notFoundButFirstLeftAndAboveArticleDivisionIndex = i * 2;
                        }
                        else if (x < rect.getLowerLeftX()
                                && notFoundButFirstLeftArticleDivisionIndex == -1) {
                            notFoundButFirstLeftArticleDivisionIndex = i * 2;
                        }
                        else if (y < rect.getUpperRightY()
                                && notFoundButFirstAboveArticleDivisionIndex == -1) {
                            notFoundButFirstAboveArticleDivisionIndex = i * 2;
                        }
                    }
                    else {
                        foundArticleDivisionIndex = 0;
                    }
                }
            }
            else {
                foundArticleDivisionIndex = 0;
            }
            int articleDivisionIndex = -1;
            if (foundArticleDivisionIndex != -1) {
                articleDivisionIndex = foundArticleDivisionIndex;
            }
            else if (notFoundButFirstLeftAndAboveArticleDivisionIndex != -1) {
                articleDivisionIndex = notFoundButFirstLeftAndAboveArticleDivisionIndex;
            }
            else if (notFoundButFirstLeftArticleDivisionIndex != -1) {
                articleDivisionIndex = notFoundButFirstLeftArticleDivisionIndex;
            }
            else if (notFoundButFirstAboveArticleDivisionIndex != -1) {
                articleDivisionIndex = notFoundButFirstAboveArticleDivisionIndex;
            }
            else {
                articleDivisionIndex = charactersByArticle.size() - 1;
            }
            final List<TextPosition> textList = charactersByArticle.get(articleDivisionIndex);
            textList.add(text);
        }
    }

    /**
     * This will determine of two floating point numbers are within a specified variance.
     * 
     * @param first
     *            The first number to compare to.
     * @param second
     *            The second number to compare to.
     * @param variance
     *            The allowed variance.
     * @return if the number is within the specified variance.
     */
    private static boolean within(final float first, final float second, final float variance)
    {
        return second > first - variance && second < first + variance;
    }

    private static float getWordSpacing(final TextPosition position)
    {
        if (position == null) {
            return 0;
        }

        float wordSpacing = 0;

        if (wordSpacing == 0) {
            // try to get width of a space character
            wordSpacing = position.getWidthOfSpace();
            // if still zero fall back to getting the width of the current
            // character
            if (wordSpacing == 0) {
                wordSpacing = position.getWidth();
            }
        }

        return wordSpacing;
    }

    private static boolean validPosition(final List<TextPosition> textList, final int pos)
    {
        return (pos >= 0) && (pos < textList.size());
    }

    /**
     * Detects whether text in two positions is on the same line. This method is a bit fuzzy so we
     * also get potential superscripts and subscripts.
     * 
     * @param cur current position.
     * @param prev previous position.
     * @return if both are in the same line.
     */
    private static boolean isSameLine(final TextPosition cur, final TextPosition prev)
    {
        if (cur.getY() == prev.getY()) {
            return true;
        }
        else {
            final float prevCenter = prev.getY() + prev.getHeight() / 2.0f;
            final float prevHeight = prev.getHeight();
            final float curCenter = cur.getY() + cur.getHeight() / 2.0f;

            final boolean result = Math.abs(curCenter - prevCenter) < (prevHeight * 0.25f);

            // if (!result) {
            // _log.debug("sameLine ["+result+"]"+
            // "[px:"+f_y1[prev]+"-"+f_y2[prev]+":"+contents[prev]+"]"+
            // "[cx:"+f_y1[cur]+"-"+f_y2[cur]+":"+contents[cur]+"]");
            // }

            return result;
        }
    }

    /**
     * Tests if two objects are vertically adjacent or if they are so far away from each other that
     * they have to be considered different blocks.
     * 
     * @param cur_top
     *            current top.
     * @param prev_top
     *            previous top.
     * @param spacing
     *            spacing.
     * @return if the two objects are verticalla adjacent.
     */
    private static boolean isVerticallyAdjacent(final float cur_top, final float prev_top,
            final float spacing)
    {
        /* set vertical error margin */
        final float verterr = (float) (spacing * 1.27);

        final boolean aboveThreshold = (cur_top < (prev_top + verterr));
        final boolean belowprev = (cur_top > prev_top);

        return aboveThreshold && belowprev;
    }

    private static boolean isLeftIndented(final TextPosition cur, final Prediction pred)
    {
        return cur.getX() > (pred.left + (pred.linespacing * 0.2));
    }

    private static boolean isLeftOutdented(final TextPosition cur, final Prediction pred)
    {
        return cur.getX() < (pred.left - (pred.linespacing * 0.2));
    }

    /**
     * Check if the current fragment is in a new column.
     * 
     * @param cur
     *            current text position.
     * @param block
     *            current block.
     * @return if the fragment is in a new column.
     */
    private static boolean isColumnSwitch(final TextPosition cur, final Block block)
    {
        return (cur.getY() < block.top); // && (f_x1[cur] > block.right);
    }

    private static boolean isSignifiantlyWorse(final double qnew, final double qold,
            final double limit)
    {
        final double deviation = Math.abs(((qnew - qold) / (qnew + qold)));
        final boolean result = (deviation > limit) && (qnew < qold);
        // if (_log.isTraceEnabled()) {
        // _log.trace("Deviation: "+deviation+ " - "+(result?"BAD":"OK"));
        // }
        return result;
    }

    /**
     * Determine whether we need to insert a word separator between the two positions or not.
     * 
     * Adapted from PDFBox PDFTextStripper.flushText()
     * 
     * @param cur
     *            current position.
     * @param prev
     *            previous position.
     * @return if the two positions are immediately adjacent.
     */
    private static boolean isNextChar(final TextPosition cur, final TextPosition prev)
    {
        float lastWordSpacing = getWordSpacing(prev);
        final float wordSpacing = getWordSpacing(cur);
        float startOfNextWordX;
        final float endOfLastTextX = prev.getX() + prev.getWidth();

        // RDD - We add a conservative approximation for space determination.
        // basically if there is a blank area between two characters that is
        // equal to some percentage of the word spacing then that will be the
        // start of the next word
        if (lastWordSpacing <= 0) {
            startOfNextWordX = endOfLastTextX + (wordSpacing * 0.50f);
        }
        else {
            startOfNextWordX = endOfLastTextX + (((wordSpacing + lastWordSpacing) / 2f) * 0.50f);
        }

        lastWordSpacing = wordSpacing;

        // if (startOfNextWordX > cur.getX()) {
        // System.out.print("{O:"+(startOfNextWordX - cur.getX())+"}");
        // }

        if (startOfNextWordX != -1 && startOfNextWordX < cur.getX() && prev != null &&
        // only bother adding a space if the last character was not a
        // space
                prev.getCharacter() != null && !prev.getCharacter().endsWith(" ")) {
            return false;
        }
        else {
            return true;
        }
    }

    private List<Line> collectLines(final List<TextPosition> textList, final int blk_start,
            final int depth)
    {
        final ArrayList<Line> lines = new ArrayList<Line>(depth);
        Line l = new Line(textList, blk_start);
        lines.add(l);
        for (int i = 1; i < depth && l.hasNextLine(); i++) {
            l = l.getNextLine();

            // Bail out if we have a potential column switch
            if (l.top < lines.get(lines.size() - 1).bottom) {
                break;
            }
            lines.add(l);
        }
        return lines;
    }

    /**
     * Return a block with the probable linespacing, lineheight and left and right borders.
     * 
     * @param textList
     *            text.
     * @param blk_start
     *            block start.
     * @param depth
     *            depth.
     * @return structure prediction.
     */
    private Prediction predictGeneralStructure(final List<TextPosition> textList,
            final int blk_start, final int depth)
    {
        // Try to fetch the next lines up to depth
        final List<Line> lines = collectLines(textList, blk_start, depth);

        // Calculate the line block parameters
        LineBlock lb = new LineBlock(lines);

        // Iterate once more over the lines because we may have a big spacing
        // indicating a new block.

        final List<Line> lines2 = new ArrayList<Line>(depth);
        final Line l = lines.get(0);
        lines2.add(l);
        for (int i = 1; i < lines.size(); i++) {
            // Bail out if we have too much distance
            if (!isVerticallyAdjacent(lines.get(i).top, lines.get(i - 1).top, lb.linespacing)) {
                break;
            }
            lines2.add(lines.get(i));
        }

        // Get the bounds in buckets
        final Buckets left_buckets = new Buckets(lb.linespacing * 0.1);
        final Buckets right_buckets = new Buckets(lb.linespacing * 0.1);
        for (final Line ln : lines2) {
            left_buckets.put(ln.left);
            right_buckets.put(ln.right);
        }

        // if (_log.isTraceEnabled()) {
        // _log.trace("Left:  size:"+left_buckets.getBest().size()+" - lines:"+lines2.size()+" - depth:"+depth);
        // }

        lb = new LineBlock(lines2);

        // Return values
        final Prediction result = new Prediction();
        result.linespacing = lb.linespacing;
        result.lineheight = lb.avglineheight;
        result.left = (float) left_buckets.getBest().getValue();
        result.right = (float) right_buckets.getBest().getValue();
        result.quality = (float) left_buckets.getBest().size() / (float) depth;

        return result;
    }

    protected Style getStyle(final TextPosition pos)
    {
        if ((pos.getFontSize() * pos.getYScale()) > 14) {
            return Style.HEADING;
        }
        else {
            return Style.PARAGRAPH;
        }
    }

    /**
     * This method is available for subclasses of this class. It will be called before processing of
     * the document start.
     * 
     * @param pdf
     *            The PDF document that is being processed.
     * @throws IOException
     *             If an IO error occurs.
     */
    protected abstract void startDocument(PDDocument pdf)
        throws IOException;

    /**
     * This method is available for subclasses of this class. It will be called after processing of
     * the document finishes.
     * 
     * @param pdf
     *            The PDF document that is being processed.
     * @throws IOException
     *             If an IO error occurs.
     */
    protected abstract void endDocument(PDDocument pdf)
        throws IOException;

    /**
     * Start a new region.
     * 
     * @param style
     *            the style.
     * @throws IOException
     *             If there is any error writing to the stream.
     */
   protected abstract void startRegion(Style style)
        throws IOException;

    /**
     * End a region.
     * 
     * @param style
     *            the style.
     * @throws IOException
     *             If there is any error writing to the stream.
     */
    protected abstract void endRegion(Style style)
        throws IOException;

    /**
     * Start a new page.
     * 
     * @param firstPage
     *            first page.
     * @param lastPage
     *            last page.
     * @param currentPage
     *            current page.
     * @param page
     *            The page we are about to process.
     * 
     * @throws IOException
     *             If there is any error writing to the stream.
     */
    protected abstract void startPage(int firstPage, int lastPage, int currentPage, PDPage page)
        throws IOException;

    /**
     * End a page.
     * 
     * @param firstPage
     *            first page.
     * @param lastPage
     *            last page.
     * @param currentPage
     *            current page.
     * @param page
     *            The page we are about to process.
     * 
     * @throws IOException
     *             If there is any error writing to the stream.
     */
    protected abstract void endPage(int firstPage, int lastPage, int currentPage, PDPage page)
        throws IOException;

    protected abstract void processLineSeparator()
        throws IOException;

    protected abstract void processWordSeparator()
        throws IOException;

    /**
     * Write the string to the output stream.
     * 
     * @param text
     *            The text to write to the stream.
     * @throws IOException
     *             If there is an error when writing the text.
     */
    protected abstract void writeCharacters(TextPosition text)
        throws IOException;

    /**
     * This is the page that the text extraction will start on. The pages start at page 1. For
     * example in a 5 page PDF document, if the start page is 1 then all pages will be extracted. If
     * the start page is 4 then pages 4 and 5 will be extracted. The default value is 1.
     * 
     * @return Value of property startPage.
     */
    public int getStartPage()
    {
        return startPage;
    }

    /**
     * This will set the first page to be extracted by this class.
     * 
     * @param startPageValue
     *            New value of property startPage.
     */
    public void setStartPage(final int startPageValue)
    {
        startPage = startPageValue;
    }

    /**
     * This will get the last page that will be extracted. This is inclusive, for example if a 5
     * page PDF an endPage value of 5 would extract the entire document, an end page of 2 would
     * extract pages 1 and 2. This defaults to Integer.MAX_VALUE such that all pages of the pdf will
     * be extracted.
     * 
     * @return Value of property endPage.
     */
    public int getEndPage()
    {
        return endPage;
    }

    /**
     * This will set the last page to be extracted by this class.
     * 
     * @param endPageValue
     *            New value of property endPage.
     */
    public void setEndPage(final int endPageValue)
    {
        endPage = endPageValue;
    }

    /**
     * @return Returns the suppressDuplicateOverlappingText.
     */
    public boolean shouldSuppressDuplicateOverlappingText()
    {
        return suppressDuplicateOverlappingText;
    }

    /**
     * Get the current page number that is being processed.
     * 
     * @return A 1 based number representing the current page.
     */
    protected int getCurrentPageNo()
    {
        return currentPageNo;
    }

    /**
     * Character strings are grouped by articles. It is quite common that there will only be a
     * single article. This returns a List that contains List objects, the inner lists will contain
     * TextPosition objects.
     * 
     * @return A double List of TextPositions for all text strings on the page.
     */
    protected List<List<TextPosition>> getCharactersByArticle()
    {
        return charactersByArticle;
    }

    /**
     * By default the text stripper will attempt to remove text that overlapps each other. Word
     * paints the same character several times in order to make it look bold. By setting this to
     * false all text will be extracted, which means that certain sections will be duplicated, but
     * better performance will be noticed.
     * 
     * @param suppressDuplicateOverlappingTextValue
     *            The suppressDuplicateOverlappingText to set.
     */
    public void setSuppressDuplicateOverlappingText(boolean suppressDuplicateOverlappingTextValue)
    {
        this.suppressDuplicateOverlappingText = suppressDuplicateOverlappingTextValue;
    }

    /**
     * This will tell if the text stripper should separate by beads.
     * 
     * @return If the text will be grouped by beads.
     */
    public boolean shouldSeparateByBeads()
    {
        return shouldSeparateByBeads;
    }

    /**
     * Set if the text stripper should group the text output by a list of beads. The default value
     * is true!
     * 
     * @param aShouldSeparateByBeads
     *            The new grouping of beads.
     */
    public void setShouldSeparateByBeads(boolean aShouldSeparateByBeads)
    {
        this.shouldSeparateByBeads = aShouldSeparateByBeads;
    }

    static class LineBlock
    {
        final List<Line> lines;
        final float linespacing;
        final float avglineheight;

        LineBlock(final List<Line> ls)
        {
            lines = ls;
            linespacing = calcLinespacing();
            avglineheight = calcAvgLineheight();
        }

        float calcLinespacing()
        {
            if (lines.size() == 1) {
                return Math.abs(lines.get(0).top - lines.get(0).bottom);
            }

            float avgls = 0.0f;
            for (int i = 0; i < (lines.size() - 1); i++) {
                avgls += Math.abs(lines.get(i).top - lines.get(i + 1).top);
            }
            return avgls / (lines.size() - 1);
        }

        private float calcAvgLineheight()
        {
            float avglh = 0.0f;
            for (final Line l : lines) {
                avglh += l.lineheight;
            }
            return avglh / lines.size();
        }
    }

    static class Prediction
    {
        float lineheight;
        float linespacing;
        float left;
        float right;
        float quality;
    }

    static class Line
        extends BasicBlock
    {
        final int start;
        final int end;
        final float lineheight;

        Line(final List<TextPosition> tl, final int pos)
        {
            super(tl);
            start = pos;
            end = findEnd();
            lineheight = growAndCalcLineheight();
        }

        private float growAndCalcLineheight()
        {
            float h = textList.get(start).getHeight();
            reset(start);
            for (int i = start + 1; i < end; i++) {
                h = Math.max(h, textList.get(i).getHeight());
                grow(i);
            }
            return h;
        }

        private int findEnd()
        {
            int cur = start;
            while (validPosition(textList, cur)
                    && isSameLine(textList.get(cur), textList.get(start))) {
                cur++;
            }
            return cur;
        }

        boolean hasNextLine()
        {
            return validPosition(textList, end);
        }

        Line getNextLine()
        {
            if (hasNextLine()) {
                return new Line(textList, end);
            }
            else {
                return null;
            }
        }

        /**
         * Return true if the text position is within the line height boundaries. Left and right
         * boundaries are not checked.
         * 
         * @param pos
         *            text position.
         * @return if the position is within the line.
         */
        boolean withinLine(final TextPosition pos)
        {
            final boolean underTop = top <= pos.getY();
            final boolean overBottom = (pos.getY() + pos.getHeight()) <= bottom;
            return underTop && overBottom;
        }

        boolean isSuperscript(final TextPosition pos)
        {
            final boolean underTop = (top - lineheight * 0.6f) <= pos.getY();
            final boolean overBottom = (pos.getY() + pos.getHeight()) <= bottom;
            return underTop && overBottom;
        }

        boolean isSubscript(final TextPosition pos)
        {
            final boolean underTop = (top <= pos.getY());
            final boolean overBottom = (pos.getY() + pos.getHeight() + lineheight * 0.6f) <= bottom;
            return underTop && overBottom;
        }

        @Override
        public String toString()
        {
            return "[t:" + top + " b:" + bottom + "|" + content + "]";
        }
    }

    static class BasicBlock
    {
        float left;
        float top;
        float right;
        float bottom;
        int lines;
        int last_pos;
        final List<TextPosition> textList;

        // This is for debugging purposes only.
        final StringBuilder content = new StringBuilder();

        public BasicBlock(final List<TextPosition> tl)
        {
            textList = tl;
        }

        float getValue(final Values v)
        {
            switch (v) {
            case BOTTOM:
                return bottom;
            case TOP:
                return top;
            case RIGHT:
                return right;
            case LEFT:
                return left;
            default:
                throw new IllegalArgumentException("Unsupported value");
            }
        }

        void normalize()
        {
            if (top < bottom) {
                final float b = top;
                top = bottom;
                bottom = b;
            }

            if (left > right) {
                final float l = left;
                left = right;
                right = l;
            }
        }

        void reset(final int pos)
        {
            final TextPosition p = textList.get(pos);

            last_pos = pos;
            lines = 0;
            left = p.getX();
            right = p.getX() + p.getWidth();
            top = p.getY();
            bottom = p.getY() + p.getHeight();

            content.setLength(0);
            content.append(p.getCharacter());
        }

        void grow(final int pos)
        {
            final TextPosition p = textList.get(pos);

            if (!isSameLine(p, textList.get(last_pos))) {
                lines++;
            }

            last_pos = pos;
            left = Math.min(p.getX(), left);
            right = Math.max(p.getX() + p.getWidth(), right);
            top = Math.min(p.getY(), top);
            bottom = Math.max(p.getY() + p.getHeight(), bottom);

            content.append(" ");
            content.append(p.getCharacter());
        }
    }

    class Block
        extends BasicBlock
    {
        float linespacing;
        float lineheight;

        Block(final List<TextPosition> textList, final int pos)
        {
            super(textList);
            reset(pos);
        }

        @Override
        void reset(final int pos)
        {
            super.reset(pos);
            linespacing = new LineBlock(collectLines(textList, pos, 3)).linespacing;
            lineheight = Math.abs(bottom - top);
        }

        @Override
        void grow(final int pos)
        {
            super.grow(pos);
            lineheight = Math.max(lineheight, textList.get(pos).getHeight());
        }
    }
}
