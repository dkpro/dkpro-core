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
import org.dkpro.jwpl.api.WikiConstants.Language;
import org.junit.jupiter.api.Test;

class WikipediaQueryReaderTest
{
    @Test
    void wikipediaReaderTest() throws Exception
    {
        var reader = createReaderDescription( //
                WikipediaQueryReader.class, //
                WikipediaQueryReader.PARAM_HOST, "localhost", //
                WikipediaQueryReader.PARAM_DB, "wikiapi_test", //
                WikipediaQueryReader.PARAM_JDBC_URL,
                "jdbc:hsqldb:file:./src/test/resources/db/wikiapi_test", //
                WikipediaQueryReader.PARAM_DRIVER, "org.hsqldb.jdbcDriver", //
                WikipediaQueryReader.PARAM_USER, "sa", //
                WikipediaQueryReader.PARAM_PASSWORD, "", //
                WikipediaQueryReader.PARAM_LANGUAGE, Language._test,
                WikipediaQueryReader.PARAM_TITLE_PATTERN, "UK%");

        int i = 0;
        for (var jcas : new JCasIterable(reader)) {
            assertNotNull(jcas);
            i++;
        }

        assertEquals(1, i);
    }

    @Test
    void wikipediaReaderTest2() throws Exception
    {
        var reader = createReaderDescription( //
                WikipediaQueryReader.class, //
                WikipediaQueryReader.PARAM_HOST, "localhost", //
                WikipediaQueryReader.PARAM_DB, "wikiapi_test", //
                WikipediaQueryReader.PARAM_JDBC_URL,
                "jdbc:hsqldb:file:./src/test/resources/db/wikiapi_test", //
                WikipediaQueryReader.PARAM_DRIVER, "org.hsqldb.jdbcDriver", //
                WikipediaQueryReader.PARAM_USER, "sa", //
                WikipediaQueryReader.PARAM_PASSWORD, "", //
                WikipediaQueryReader.PARAM_LANGUAGE, Language._test,
                WikipediaQueryReader.PARAM_MIN_TOKENS, 1, WikipediaQueryReader.PARAM_MAX_TOKENS,
                200, WikipediaQueryReader.PARAM_TITLE_PATTERN, "UK%");

        int i = 0;
        for (var jcas : new JCasIterable(reader)) {
            assertNotNull(jcas);
            i++;
        }

        assertEquals(1, i);
    }
}
