/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.api.segmentation;

import org.apache.uima.jcas.tcas.Annotation;

public class TrimUtils
{
    /**
     * Trim the offsets of the given annotation to remove leading/trailing whitespace.
     * <p>
     * <b>Note:</b> use this method if the document text of the CAS has not been set yet but you
     * have it available in a buffer.
     * <p>
     * <b>Note:</b> best use this method before adding the annotation to the indexes.
     * 
     * @param aText
     *            the document text (available so far).
     * @param aAnnotation
     *            the annotation to trim. Offsets are updated.
     */
    public static void trim(CharSequence aText, Annotation aAnnotation)
    {
        int[] offsets = { aAnnotation.getBegin(), aAnnotation.getEnd() };
        trim(aText, offsets);
        aAnnotation.setBegin(offsets[0]);
        aAnnotation.setEnd(offsets[1]);
    }
    
    /**
     * Remove trailing or leading whitespace from the annotation.
     * @param aText the text.
     * @param aSpan the offsets.
     */
    public static void trim(CharSequence aText, int[] aSpan)
    {
        if (aSpan[0] == aSpan[1]) {
            // Nothing to do on empty spans
            return;
        }
        
        int begin = aSpan[0];
        int end = aSpan[1];

        // First we trim at the end. If a trimmed span is empty, we want to return the original 
        // begin as the begin/end of the trimmed span
        while (
                (end > 0)
                && end > begin
                && trimChar(aText.charAt(end - 1))
        ) {
            end --;
        }

        // Then, trim at the start
        while (
                (begin < (aText.length() - 1))
                && begin < end
                && trimChar(aText.charAt(begin))
        ) {
            begin ++;
        }
        
        aSpan[0] = begin;
        aSpan[1] = end;
    }

    private static boolean trimChar(final char aChar)
    {
        switch (aChar) {
        case '\n':     return true; // Line break
        case '\r':     return true; // Carriage return
        case '\t':     return true; // Tab
        case '\u200E': return true; // LEFT-TO-RIGHT MARK
        case '\u200F': return true; // RIGHT-TO-LEFT MARK
        case '\u2028': return true; // LINE SEPARATOR
        case '\u2029': return true; // PARAGRAPH SEPARATOR
        default:
            return  Character.isWhitespace(aChar);
        }
    }
}
