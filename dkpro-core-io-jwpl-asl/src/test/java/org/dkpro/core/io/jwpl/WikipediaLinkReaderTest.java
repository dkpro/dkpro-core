/*
 * Copyright 2017
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
package org.dkpro.core.io.jwpl;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.JCasUtil;
import org.dkpro.jwpl.api.WikiConstants.Language;
import org.dkpro.jwpl.parser.Link;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaLink;

class WikipediaLinkReaderTest
{
    @Test
    void wikipediaReaderTest()
        throws Exception
    {
        var reader = createReaderDescription(
                WikipediaLinkReader.class,
                WikipediaLinkReader.PARAM_ALLOWED_LINK_TYPES, Link.type.INTERNAL.name(),
                WikipediaLinkReader.PARAM_HOST, "localhost", //
                WikipediaLinkReader.PARAM_DB, "wikiapi_test", //
                WikipediaLinkReader.PARAM_JDBC_URL,
                "jdbc:hsqldb:file:./src/test/resources/db/wikiapi_test", //
                WikipediaLinkReader.PARAM_DRIVER, "org.hsqldb.jdbcDriver", //
                WikipediaLinkReader.PARAM_USER, "sa", //
                WikipediaLinkReader.PARAM_PASSWORD, "", //
                WikipediaLinkReader.PARAM_LANGUAGE, Language._test);

        int i = 0;
        for (var jcas : new JCasIterable(reader)) {
            assertNotNull(jcas);
            i++;
        }

        assertEquals(30, i);
    }

    @Test
    void wikipediaLinkReaderTest()
        throws Exception
    {
        var reader = createReaderDescription(
                WikipediaLinkReader.class,
                WikipediaLinkReader.PARAM_ALLOWED_LINK_TYPES, Link.type.INTERNAL.name(),
                WikipediaLinkReader.PARAM_HOST, "localhost", //
                WikipediaLinkReader.PARAM_DB, "wikiapi_test", //
                WikipediaLinkReader.PARAM_JDBC_URL,
                "jdbc:hsqldb:file:./src/test/resources/db/wikiapi_test", //
                WikipediaLinkReader.PARAM_DRIVER, "org.hsqldb.jdbcDriver", //
                WikipediaLinkReader.PARAM_USER, "sa", //
                WikipediaLinkReader.PARAM_PASSWORD, "", //
                WikipediaLinkReader.PARAM_LANGUAGE, Language._test);

        int linkCounter = 0;
        for (var jcas : new JCasIterable(reader)) {
            for (var link : JCasUtil.select(jcas, WikipediaLink.class)) {
                System.out.println(link.getCoveredText());
                linkCounter++;
            }
            assertNotNull(jcas);
        }

        assertEquals(2, linkCounter);
    }
}
