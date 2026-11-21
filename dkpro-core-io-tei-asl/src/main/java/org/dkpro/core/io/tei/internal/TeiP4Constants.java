/*
 * Copyright 2019
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
 */
package org.dkpro.core.io.tei.internal;

import javax.xml.namespace.QName;

public final class TeiP4Constants
{
    /**
     * (character) represents a character.
     */
    public static final String TAG_CHARACTER = "c";

    /**
     * (word) represents a grammatical (not necessarily orthographic) word.
     */
    public static final String TAG_WORD = "w";
    public static final String TAG_MULTIWORD = "mw";

    /**
     * (s-unit) contains a sentence-like division of a text.
     */
    public static final String TAG_SUNIT = "s";

    /**
     * (utterance) a stretch of speech usually preceded and followed by silence or by a change of
     * speaker.
     */
    public static final String TAG_U = "u";

    /**
     * (paragraph) marks paragraphs in prose.
     */
    public static final String TAG_PARAGRAPH = "p";

    /**
     * (phrase) represents a grammatical phrase.
     */
    public static final String TAG_PHRASE = "phr";

    /**
     * (referencing string) contains a general purpose name or referring string.
     */
    public static final String TAG_RS = "rs";

    /**
     * contains a single text of any kind, whether unitary or composite, for example a poem or
     * drama, a collection of essays, a novel, a dictionary, or a corpus sample.
     */
    public static final String TAG_TEXT = "text";

    /**
     * contains the title of a work, whether article, book, journal, or series, including any
     * alternative titles or subtitles.
     */
    public static final String TAG_TITLE = "title";

    /**
     * (TEI document) contains a single TEI-conformant document, comprising a TEI header and a text,
     * either in isolation or as part of a <teiCorpus> element.
     */
    public static final String TAG_TEI_DOC = "TEI.2";
    
    /**
     * (text body) contains the whole body of a single unitary text, excluding any front or back
     * matter.
     */
    public static final String TAG_BODY = "body";
    
    /**
     * (TEI Header) supplies the descriptive and declarative information making up an ‘electronic
     * title page’ prefixed to every TEI-conformant text.
     */
    public static final String TAG_TEI_HEADER = "teiHeader";

    /**
     * (File Description) contains a full bibliographic description of an electronic file.
     */
    public static final String TAG_FILE_DESC = "fileDesc";
    
    /**
     * (title statement) groups information about the title of a work and those responsible for its
     * intellectual content.
     */
    public static final String TAG_TITLE_STMT = "titleStmt";
    
    /**
     * (personal name) contains a proper noun or proper-noun phrase referring to a person, possibly
     * including any or all of the person's forenames, surnames, honorifics, added names, etc.
     */
    public static final String TAG_PERS_NAME = "persName";

    public static final String ATTR_TYPE = "type";
    public static final String ATTR_POS = "pos";
    public static final String ATTR_FUNCTION = "function";
    public static final String ATTR_LEMMA = "lemma";

    
    public static final QName E_TEI_TEI = new QName(TAG_TEI_DOC);
    public static final QName E_TEI_HEADER = new QName(TAG_TEI_HEADER);
    public static final QName E_TEI_FILE_DESC = new QName(TAG_FILE_DESC);
    public static final QName E_TEI_TITLE_STMT = new QName(TAG_TITLE_STMT);
    public static final QName E_TEI_TITLE = new QName(TAG_TITLE);
    public static final QName E_TEI_TEXT = new QName(TAG_TEXT);
    public static final QName E_TEI_BODY = new QName(TAG_BODY);
    public static final QName E_TEI_PERS_NAME = new QName(TAG_PERS_NAME);

    private TeiP4Constants()
    {
        // No instances
    }
}
