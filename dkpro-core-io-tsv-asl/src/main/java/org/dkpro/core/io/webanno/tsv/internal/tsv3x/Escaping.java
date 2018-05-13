/*
 * Copyright 2017
 * Ubiquitous Knowledge Processing (UKP) Lab and FG Language Technology
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */package org.dkpro.core.io.webanno.tsv.internal.tsv3x;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeJava;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dkpro.core.io.webanno.tsv.internal.tsv3x.model.TsvFormatHeader;

public class Escaping
{
    public static String escapeValue(String aValue)
    {
        return StringUtils.replaceEach(aValue,
                new String[] { "\\", "[", "]", "|", "_", "->", ";", "\t", "\n", "*" },
                new String[] { "\\\\", "\\[", "\\]", "\\|", "\\_", "\\->", "\\;", "\\t", "\\n",
                        "\\*" });
    }

    public static String unescapeValue(String aValue)
    {
        return StringUtils.replaceEach(aValue,
                new String[] { "\\\\", "\\[", "\\]", "\\|", "\\_", "\\->", "\\;", "\\t", "\\n",
                        "\\*" },
                new String[] { "\\", "[", "]", "|", "_", "->", ";", "\t", "\n", "*" });
    }

    public static String escapeText(String aText)
    {
        List<String> pat = new ArrayList<>();
        List<String> esc = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            if (i > 7 && i < 14) {
                continue;
            }
            pat.add(Character.toString((char) i));
            esc.add("\\" + Character.toString((char) i));
        }
        // with a readable Java escape sequence
        // TAB
        pat.add("\t");
        esc.add("\\t");
        // linefeed
        pat.add("\n");
        esc.add("\\n");
        // formfeed
        pat.add("\f");
        esc.add("\\f");
        // carriage return
        pat.add("\r");
        esc.add("\\r");
        // backspace
        pat.add("\b");
        esc.add("\\b");
        // backslash
        pat.add("\\");
        esc.add("\\\\");

        return StringUtils.replaceEach(aText, pat.toArray(new String[pat.size()]),
                esc.toArray(new String[esc.size()]));
    }

    public static String unescapeText(TsvFormatHeader aHeader, String aText) {
        if ("3.1".equals(aHeader.getVersion())) {
            return unescapeJava(aText);
        }
        else if ("3.2".equals(aHeader.getVersion())) {
            List<String> pat = new ArrayList<>();
            List<String> esc = new ArrayList<>();
            for (int i = 0; i < 32; i++) {
                if (i > 7 && i < 14) {
                    continue;
                }
                pat.add(Character.toString((char) i));
                esc.add("\\" + Character.toString((char) i));
            }
            // with a readable Java escape sequence
            // TAB
            pat.add("\t");
            esc.add("\\t");
            // linefeed
            pat.add("\n");
            esc.add("\\n");
            // formfeed
            pat.add("\f");
            esc.add("\\f");
            // carriage return
            pat.add("\r");
            esc.add("\\r");
            // backspace
            pat.add("\b");
            esc.add("\\b");
            // backslash
            pat.add("\\");
            esc.add("\\\\");
            return StringUtils.replaceEach(aText, 
                    esc.toArray(new String[esc.size()]), pat.toArray(new String[pat.size()]));
        }
        else {
            throw new IllegalStateException("Unknown version: [" + aHeader.getVersion() + "]");
        }
    }
}
