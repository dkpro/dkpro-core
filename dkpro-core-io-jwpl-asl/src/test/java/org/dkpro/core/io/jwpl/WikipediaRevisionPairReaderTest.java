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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.dkpro.jwpl.api.WikiConstants.Language;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

class WikipediaRevisionPairReaderTest
{
    @Disabled("Currently there is no test database to test revisions")
    @Test
    void wikipediaRevisionReaderTest()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                WikipediaRevisionPairReader.class,
                WikipediaRevisionPairReader.PARAM_HOST, "localhost", //
                WikipediaRevisionPairReader.PARAM_DB, "wikiapi_test", //
                WikipediaRevisionPairReader.PARAM_JDBC_URL,
                "jdbc:hsqldb:file:./src/test/resources/db/wikiapi_test", //
                WikipediaRevisionPairReader.PARAM_DRIVER, "org.hsqldb.jdbcDriver", //
                WikipediaRevisionPairReader.PARAM_USER, "sa", //
                WikipediaRevisionPairReader.PARAM_PASSWORD, "", //
                WikipediaRevisionPairReader.PARAM_LANGUAGE, Language.simple_english);

        int i = 0;
        for (JCas jcas : new JCasIterable(reader)) {
            assertNotNull(jcas);

            JCas view1 = jcas.getView(WikipediaRevisionPairReader.REVISION_1);
            JCas view2 = jcas.getView(WikipediaRevisionPairReader.REVISION_2);

            DocumentMetaData md1 = DocumentMetaData.get(view1);
            DocumentMetaData md2 = DocumentMetaData.get(view2);

//            System.out.println(md1);
//            System.out.println(md2);

            i++;
            if (i > 10) {
                break;
            }
        }
    }
}
