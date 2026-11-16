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
package org.dkpro.core.io.conll.internal;

import static java.util.Comparator.comparingInt;

import java.util.Comparator;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

/**
 * Constants and helpers shared by the CoNLL-U reader and writer.
 */
public final class ConllUFormat
{
    /**
     * Marks an unspecified value.
     */
    public static final String UNUSED = "_";

    /**
     * The head ID of the artificial root.
     */
    public static final String ROOT_ID = "0";

    /**
     * Document boundary without an ID - the spec allows {@code # newdoc} without a value.
     */
    public static final String META_DOCUMENT = "newdoc";

    /**
     * Paragraph boundary without an ID - the spec allows {@code # newpar} without a value.
     */
    public static final String META_PARAGRAPH = "newpar";

    /**
     * The MISC item indicating that a token is not followed by a space.
     */
    public static final String SPACE_AFTER_NO = "SpaceAfter=No";

    /**
     * Regular word IDs are positive integers without leading zeros or sign. The length is limited
     * so that every valid ID can be represented as an {@code int}.
     */
    public static final Pattern WORD_ID = Pattern.compile("[1-9]\\d{0,8}");

    /**
     * Heads are word IDs or {@link #ROOT_ID} for the root.
     */
    public static final Pattern HEAD_ID = Pattern.compile("0|[1-9]\\d{0,8}");

    /**
     * Empty nodes have IDs of the form {@code i.k} with {@code i >= 0} and {@code k >= 1}.
     */
    public static final Pattern EMPTY_NODE_ID = Pattern
            .compile("(0|[1-9]\\d{0,8})\\.[1-9]\\d{0,8}");

    /**
     * Orders node IDs numerically - regular IDs ({@code 3}) before empty nodes following them
     * ({@code 3.1}), and empty nodes by their numeric suffix ({@code 3.2} before {@code 3.10}). The
     * IDs must be valid.
     */
    public static final Comparator<String> NODE_ID_ORDER = comparingInt(
            (String id) -> Integer.parseInt(StringUtils.substringBefore(id, "."))).thenComparingInt(
                    id -> id.contains(".") ? Integer.parseInt(StringUtils.substringAfter(id, "."))
                            : 0);

    private ConllUFormat()
    {
        // No instances
    }

    /**
     * @param aId
     *            a valid empty node ID {@code i.k}.
     * @return the index {@code i} of the word the empty node follows.
     */
    public static int emptyNodeWordIndex(String aId)
    {
        return Integer.parseInt(aId.substring(0, aId.indexOf('.')));
    }

    /**
     * @param aId
     *            a valid empty node ID {@code i.k}.
     * @return the index {@code k} of the empty node among the empty nodes following word {@code i}.
     */
    public static int emptyNodeIndex(String aId)
    {
        return Integer.parseInt(aId.substring(aId.indexOf('.') + 1));
    }

    /**
     * Identify the document in a CAS for messages - by its URI, or by its ID if it has no URI.
     */
    public static String documentName(JCas aJCas)
    {
        DocumentMetaData dmd = JCasUtil.select(aJCas, DocumentMetaData.class).stream() //
                .findFirst() //
                .orElse(null);
        if (dmd == null) {
            return "unknown document";
        }
        if (dmd.getDocumentUri() != null) {
            return dmd.getDocumentUri();
        }
        return dmd.getDocumentId() != null ? dmd.getDocumentId() : "unknown document";
    }
}
