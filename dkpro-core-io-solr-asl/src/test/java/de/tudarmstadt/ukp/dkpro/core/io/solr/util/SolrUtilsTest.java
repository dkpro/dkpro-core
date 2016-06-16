/*
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
 */
package de.tudarmstadt.ukp.dkpro.core.io.solr.util;

import static org.junit.Assert.assertEquals;

import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

public class SolrUtilsTest
{
    @Test
    public void testAddField()
    {
        String fieldname = "field";
        String value = "value";
        String idValue = "id";
        String idFieldname = idValue;
        boolean update = false;

        SolrInputDocument document = new SolrInputDocument();
        document.addField(idFieldname, idFieldname);
        SolrUtils.setField(document, fieldname, value, update);

        assertEquals(idValue, document.getFieldValue(idValue));
        assertEquals(value, document.getFieldValue(fieldname));
    }
}
