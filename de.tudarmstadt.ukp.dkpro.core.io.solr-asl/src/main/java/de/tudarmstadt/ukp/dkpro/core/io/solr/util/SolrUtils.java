/*******************************************************************************
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.core.io.solr.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.map.SingletonMap;
import org.apache.solr.common.SolrInputDocument;

/**
 * Helper utilities.
 *
 *
 */
public class SolrUtils
{
    /**
     * The modifiers available for Solr atomic updates
     *
     * @see <a
     *      href="http://wiki.apache.org/solr/Atomic_Updates">http://wiki.apache.org/solr/Atomic_Updates</a>
     */
    public enum Modifier
    {
        SET, ADD, INC
    };

    private static final Modifier DEFAULT_MODIFIER = Modifier.SET;
    private static final Map<Modifier, String> modifierMap = new HashMap<Modifier, String>()
    {
        private static final long serialVersionUID = 1791784069195163737L;
        {
            put(Modifier.SET, "set");
            put(Modifier.ADD, "add");
            put(Modifier.INC, "inc");
        }
    };

    /**
     * Add a field and optionally update if applicable. Updates can be "set", "add", or "inc".
     *
     * @param document
     *            the {@link SolrInputDocument} to add/update
     * @param fieldname
     *            the field name to add/update
     * @param value
     *            the value to insert for the field.
     * @param update
     *            if true, use Solr atomic update mechanism; otherwise overwrite
     * @param modifier
     *            The {@link Modifier} to use for updating a document (only used if {@code update}
     *            is true).
     * @see #setField(SolrInputDocument, String, Object, boolean)
     */
    public static void setField(SolrInputDocument document, String fieldname, Object value,
            boolean update, Modifier modifier)
    {
        if (update) {
            document.setField(fieldname, new SingletonMap<>(modifierMap.get(modifier), value));
        }
        else {
            document.setField(fieldname, value);
        }
    }

    /**
     * Add a field and optionally update, using the default atomic update operation ("set").
     *
     * @param document
     *            the {@link SolrInputDocument} to add/update
     * @param fieldname
     *            the field name to add/update
     * @param value
     *            the value to insert for the field.
     * @param update
     *            if true, use Solr atomic update mechanism; otherwise overwrite
     * @see #setField(SolrInputDocument, String, Object, boolean, Modifier)
     */
    public static void setField(SolrInputDocument document, String fieldname, Object value,
            boolean update)
    {
        setField(document, fieldname, value, update, DEFAULT_MODIFIER);
    }
}
