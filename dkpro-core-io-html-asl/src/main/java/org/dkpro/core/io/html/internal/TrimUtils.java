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
package org.dkpro.core.io.html.internal;

public class TrimUtils
{
    /**
     * Remove trailing or leading whitespace from the annotation.
     * @param aText the text.
     * @param aSpan the offsets.
     */
    public static void trim(CharSequence aText, int[] aSpan)
    {
        int begin = aSpan[0];
        int end = aSpan[1] - 1;

        while (
                (begin < (aText.length() - 1))
                && trimChar(aText.charAt(begin))
        ) {
            begin ++;
        }
        while (
                (end > 0)
                && trimChar(aText.charAt(end))
        ) {
            end --;
        }

        end++;

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
