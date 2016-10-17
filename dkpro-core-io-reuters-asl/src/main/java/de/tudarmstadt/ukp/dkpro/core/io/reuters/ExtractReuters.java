/*
 * Copyright 2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.io.reuters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extract all the documents from a Reuters-21587 corpus in SGML format. The SGML files are expected
 * to reside in a single directory.
 * <p>
 * This is an adaption of the {@code ExtractReuters} class in the {@code lucene-benchmarks} package.
 *
 * @see <a href="http://lucene.apache.org/core/5_3_1/benchmark/org/apache/lucene/benchmark/utils/ExtractReuters.html">ExtractReuters</a>
 */
public class ExtractReuters
{
    private static Set<String> NESTED_TAGS = new HashSet<>(
            Arrays.asList(new String[] { "TOPICS" }));
    private static Pattern EXTRACTION_PATTERN = Pattern
            .compile(
                    " (LEWISSPLIT)=\"(.*?)\"|(CGISPLIT)=\"(.*?)\"|(OLDID)=\"(.*?)\"|(NEWID)=\"(.*?)\"|<(TITLE)>(.*?)</TITLE>|<(DATE)>(.*?)</DATE>|<(BODY)>(.*?)</BODY>|<(TOPICS)>(.*?)</TOPICS>|<(PLACES)>(.*?)</PLACES>|<(PEOPLE)>(.*?)</PEOPLE>|<(ORGS)>(.*?)</ORGS>|<(EXCHANGES)>(.*?)</EXCHANGES>|<(COMPANIES)>(.*?)</COMPANIES>|<(UNKNOWN)>(.*?)</UNKNOWN>|<(DATELINE)>(.*?)</DATELINE>");
    private static Pattern NESTED_EXTRACTION_PATTERN = Pattern.compile("<D>(.*?)</D>");

    private static String[] META_CHARS = { "&", "<", ">", "\"", "'" };
    private static String[] META_CHARS_SERIALIZATIONS = { "&amp;", "&lt;", "&gt;", "&quot;",
            "&apos;" };

    /**
     * Read all the SGML file in the given directory.
     *
     * @param reutersDir
     *            the directory that contains the Reuters SGML files.
     * @return a list of {@link ReutersDocument}s
     * @throws IOException
     *             if any of the files cannot be read.
     * @throws ParseException
     *             if there was a problem parsing a date
     */
    public static List<ReutersDocument> extract(Path reutersDir)
            throws IOException, ParseException
    {
        List<ReutersDocument> docs = new ArrayList<>();
        DirectoryStream<Path> stream = Files.newDirectoryStream(reutersDir, "*.sgm");
        for (Path sgmFile : stream) {
            InputStream inputStream = Files.newInputStream(sgmFile);
            docs.addAll(extractFile(inputStream, sgmFile.toUri()));
        }
        return docs;
    }

    /**
     * Read the documents out of a single file. Each file contains approximately 1000 documents.
     *
     * @param sgmFile
     *            an {@link InputStream} of a Reuters SGML file.
     * @param uri
     *            an {@link URI} pointing to the original SGML file location
     * @return a list of {@link ReutersDocument}s extracted from the input stream
     * @throws IOException
     *             if any of the files cannot be read.
     * @throws ParseException
     *             if there was a problem parsing a date
     */
    public static List<ReutersDocument> extractFile(InputStream sgmFile, URI uri)
            throws IOException, ParseException
    {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(sgmFile, StandardCharsets.ISO_8859_1));

        List<ReutersDocument> entries = new ArrayList<>();  // collection of all documents in file
        StringBuilder docBuffer = new StringBuilder(1024);  // text of current document

        String line;
        while ((line = reader.readLine()) != null) {
            // when we see a closing reuters tag, flush the file

            if (!line.contains("</REUTERS")) {
                /* document continuing */

                docBuffer.append(line).append(' ');// accumulate the strings for now,
                // then apply regular expression to
                // get the pieces,
            }
            else {
                /* document end reached in input file, parse content */
                ReutersDocument reutersDocument = new ReutersDocument();

                // Extract the relevant pieces and write to a map representing the document
                Matcher matcher = EXTRACTION_PATTERN.matcher(docBuffer);
                while (matcher.find()) {
                    /* iterate over outer tags */
                    for (int i = 1; i <= matcher.groupCount(); i += 2) {
                        if (matcher.group(i) != null) {
                            String tag = matcher.group(i).trim();
                            String value = matcher.group(i + 1).trim();

                            /* replace SGML characters */
                            for (int j = 0; j < META_CHARS_SERIALIZATIONS.length; j++) {
                                value = value
                                        .replaceAll(META_CHARS_SERIALIZATIONS[j], META_CHARS[j]);
                            }

                            /* extract value(s) */
                            if (NESTED_TAGS.contains(tag)) {
                                extractNested(reutersDocument, tag, value);
                            }
                            else {
                                reutersDocument.set(tag, value);
                            }
                        }
                    }
                }
                /* add metadata information for current doc */
                reutersDocument.setPath(uri);
                entries.add(reutersDocument);
                /* reset document buffer */
                docBuffer.setLength(0);
            }
        }
        return entries;
    }

    /**
     * Find the {@code <D>} tags that are nested within another tag and add them to the given {@link ReutersDocument}.
     *
     * @param doc  the current document represented as a {@link ReutersDocument}.
     * @param tag  the outer tag, e.g. {@code <TOPICS>}
     * @param text the value of the outer tag from which nested tags are extracted
     */
    private static void extractNested(ReutersDocument doc, String tag, String text)
            throws ParseException
    {
        Matcher nestedMatcher = NESTED_EXTRACTION_PATTERN.matcher(text);
        while (nestedMatcher.find()) {
            /* iterate over <D> tags */
            for (int j = 1; j <= nestedMatcher.groupCount(); j++) {
                String d = nestedMatcher.group(j).trim();
                doc.set(tag, d);
            }
        }
    }
}
