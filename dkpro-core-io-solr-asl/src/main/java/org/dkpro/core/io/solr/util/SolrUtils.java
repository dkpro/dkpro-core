/*
 * Copyright 2016
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
package org.dkpro.core.io.solr.util;

import org.apache.commons.collections4.map.SingletonMap;
import org.apache.solr.common.SolrInputDocument;

/**
 * Helper utilities.
 */
public class SolrUtils
{
    /**
     * The modifiers available for Solr atomic updates: SET, ADD, INC, REMOVE, REMOVEREGEX.
     *
     * @see <a
     *      href=https://cwiki.apache.org/confluence/display/solr/Updating+Parts+of+Documents#UpdatingPartsofDocuments-AtomicUpdates>Updating
     *      Parts of Documents</a>
     * @see <a href="http://wiki.apache.org/solr/Atomic_Updates">Atomic Updates</a>
     */
    public enum Modifier
    {
        SET, ADD, INC, REMOVE, REMOVEREGEX
    }

    private static final Modifier DEFAULT_MODIFIER = Modifier.SET;

    /**
     * Add a field and optionally perform a partial update if applicable on an existing document.
     *
     * @param document
     *            the {@link SolrInputDocument} to add/update
     * @param fieldname
     *            the field name to add/update
     * @param value
     *            the value to insert for the field.
     * @param update
     *            if true, use Solr atomic update mechanism; otherwise overwrite document
     * @param modifier
     *            The {@link Modifier} to use when performing an atomic update (i.e. iff
     *            {@code update} is set to true).
     * @see #setField(SolrInputDocument, String, Object, boolean)
     */
    public static void setField(SolrInputDocument document, String fieldname, Object value,
            boolean update, Modifier modifier)
    {
        if (update) {
            /* perform an atomic update on potentially existing document */
            document.setField(fieldname, new SingletonMap<>(modifier.name().toLowerCase(), value));
        }
        else {
            document.setField(fieldname, value);
        }
    }

    /**
     * Add a field and optionally perform a partial update on an existing document, using the
     * default atomic update operation ("set").
     *
     * @param document
     *            the {@link SolrInputDocument} to add/update
     * @param fieldname
     *            the field name to add/update
     * @param value
     *            the value to insert for the field.
     * @param update
     *            if true, use Solr atomic update mechanism; otherwise overwrite existing document
     * @see #setField(SolrInputDocument, String, Object, boolean, Modifier)
     * @see Modifier
     */
    public static void setField(SolrInputDocument document, String fieldname, Object value,
            boolean update)
    {
        setField(document, fieldname, value, update, DEFAULT_MODIFIER);
    }
}
