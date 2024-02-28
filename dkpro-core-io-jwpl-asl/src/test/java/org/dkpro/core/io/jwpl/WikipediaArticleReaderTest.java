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

class WikipediaArticleReaderTest
{
    @Test
    void wikipediaReaderTest() throws Exception
    {
        var reader = createReaderDescription( //
                WikipediaArticleReader.class, //
                WikipediaArticleReader.PARAM_HOST, "localhost", //
                WikipediaArticleReader.PARAM_DB, "wikiapi_test", //
                WikipediaArticleReader.PARAM_JDBC_URL,
                "jdbc:hsqldb:file:./src/test/resources/db/wikiapi_test", //
                WikipediaArticleReader.PARAM_DRIVER, "org.hsqldb.jdbcDriver", //
                WikipediaArticleReader.PARAM_USER, "sa", //
                WikipediaArticleReader.PARAM_PASSWORD, "", //
                WikipediaArticleReader.PARAM_LANGUAGE, Language._test);

        int i = 0;
        for (var jcas : new JCasIterable(reader)) {
            assertNotNull(jcas);
            i++;
        }

        assertEquals(30, i);
    }

    @Test
    void wikipediaArticleIdReaderTest() throws Exception
    {
        var reader = createReaderDescription( //
                WikipediaArticleReader.class, //
                WikipediaArticleReader.PARAM_PAGE_ID_LIST, new String[] { "1041", "103", "107" }, //
                WikipediaArticleReader.PARAM_HOST, "localhost", //
                WikipediaArticleReader.PARAM_DB, "wikiapi_test", //
                WikipediaArticleReader.PARAM_JDBC_URL,
                "jdbc:hsqldb:file:./src/test/resources/db/wikiapi_test", //
                WikipediaArticleReader.PARAM_DRIVER, "org.hsqldb.jdbcDriver", //
                WikipediaArticleReader.PARAM_USER, "sa", //
                WikipediaArticleReader.PARAM_PASSWORD, "", //
                WikipediaArticleReader.PARAM_LANGUAGE, Language._test);

        int i = 0;
        for (var jcas : new JCasIterable(reader)) {
            assertNotNull(jcas);
            i++;
        }

        assertEquals(3, i);
    }

    @Test
    public void wikipediaArticleTitleReaderTest() throws Exception
    {
        var reader = createReaderDescription( //
                WikipediaArticleReader.class, //
                WikipediaArticleReader.PARAM_PAGE_TITLE_LIST, new String[] { "TK1", "TK3" }, //
                WikipediaArticleReader.PARAM_HOST, "localhost", //
                WikipediaArticleReader.PARAM_DB, "wikiapi_test", //
                WikipediaArticleReader.PARAM_JDBC_URL,
                "jdbc:hsqldb:file:./src/test/resources/db/wikiapi_test", //
                WikipediaArticleReader.PARAM_DRIVER, "org.hsqldb.jdbcDriver", //
                WikipediaArticleReader.PARAM_USER, "sa", //
                WikipediaArticleReader.PARAM_PASSWORD, "", //
                WikipediaArticleReader.PARAM_LANGUAGE, Language._test);

        int i = 0;
        for (var jcas : new JCasIterable(reader)) {
            assertNotNull(jcas);
            i++;
        }

        assertEquals(2, i);
    }

    @Test
    public void wikipediaArticleIdFileReaderTest() throws Exception
    {
        var reader = createReaderDescription( //
                WikipediaArticleReader.class, //
                WikipediaArticleReader.PARAM_PATH_TO_PAGE_ID_LIST, "src/test/resources/idList", //
                WikipediaArticleReader.PARAM_HOST, "localhost", //
                WikipediaArticleReader.PARAM_DB, "wikiapi_test", //
                WikipediaArticleReader.PARAM_JDBC_URL,
                "jdbc:hsqldb:file:./src/test/resources/db/wikiapi_test", //
                WikipediaArticleReader.PARAM_DRIVER, "org.hsqldb.jdbcDriver", //
                WikipediaArticleReader.PARAM_USER, "sa", //
                WikipediaArticleReader.PARAM_PASSWORD, "", //
                WikipediaArticleReader.PARAM_LANGUAGE, Language._test);

        int i = 0;
        for (var jcas : new JCasIterable(reader)) {
            assertNotNull(jcas);
            i++;
        }

        assertEquals(3, i);
    }

    @Test
    public void wikipediaArticleTitleFileReaderTest() throws Exception
    {
        var reader = createReaderDescription( //
                WikipediaArticleReader.class, //
                WikipediaArticleReader.PARAM_PATH_TO_PAGE_TITLE_LIST,
                "src/test/resources/titleList", //
                WikipediaArticleReader.PARAM_HOST, "localhost", //
                WikipediaArticleReader.PARAM_DB, "wikiapi_test", //
                WikipediaArticleReader.PARAM_JDBC_URL,
                "jdbc:hsqldb:file:./src/test/resources/db/wikiapi_test", //
                WikipediaArticleReader.PARAM_DRIVER, "org.hsqldb.jdbcDriver", //
                WikipediaArticleReader.PARAM_USER, "sa", //
                WikipediaArticleReader.PARAM_PASSWORD, "", //
                WikipediaArticleReader.PARAM_LANGUAGE, Language._test);

        int i = 0;
        for (var jcas : new JCasIterable(reader)) {
            assertNotNull(jcas);
            i++;
        }

        assertEquals(2, i);
    }
}
