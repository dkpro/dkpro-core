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
package de.tudarmstadt.ukp.dkpro.core.io.jwpl.util;


public class WikiUtils
{

//    /**
//     * A fast alternative to the JWPL Parser for converting MediaWikiMarkup to plain text.
//     * 
//     * @param markup The string with markup.
//     * @return The cleaned string.
//     * @throws IOException
//     */
//    public static String mediaWikiMarkup2PlainText(String markup) throws IOException {
//
//        StringWriter writer = new StringWriter();
//
//        HtmlDocumentBuilder builder = new HtmlDocumentBuilder(writer);
//        builder.setEmitAsDocument(false);
//
//        MarkupParser parser = new MarkupParser(new MediaWikiDialect());
//        parser.setBuilder(builder);
//        parser.parse(markup);
//
//        final String html = writer.toString();
//        final StringBuilder cleaned = new StringBuilder();
//
//        HTMLEditorKit.ParserCallback callback = new HTMLEditorKit.ParserCallback() {
//                @Override
//                public void handleText(char[] data, int pos) {
//                    cleaned.append(new String(data)).append(' ');
//                }
//        };
//        new ParserDelegator().parse(new StringReader(html), callback, false);
//
//        return cleaned.toString();
//    }

    /**
     * Clean a string from left-over WikiMarkup (most parsers do not work 100% correct).
     * 
     * @param text A string with rests of WikiMarkup.
     * @return The cleaned string.
     */
    public static String cleanText(String text)
    {
        String plainText = text;

        plainText = plainText.replaceAll("<.+?>", " ");
        plainText = plainText.replaceAll("__.+?__", " ");
        plainText = plainText.replaceAll("\\[http.+?\\]", " ");
        plainText = plainText.replaceAll("\\{\\|.+?\\|\\}", " ");
        plainText = plainText.replaceAll(" - ", " ");

        plainText = plainText.replace('"', ' ');
        plainText = plainText.replace('\'', ' ');
        plainText = plainText.replace('[', ' ');
        plainText = plainText.replace(']', ' ');
        plainText = plainText.replace('=', ' ');
        plainText = plainText.replace('*', ' ');
        plainText = plainText.replace('|', ' ');
        plainText = plainText.replace(':', ' ');
        plainText = plainText.replace('{', ' ');
        plainText = plainText.replace('}', ' ');
        plainText = plainText.replace('(', ' ');
        plainText = plainText.replace(')', ' ');
        plainText = plainText.replaceAll("\\s{2,}", " ");

        return plainText;
    }
}
