/*******************************************************************************
 * Copyright 2013
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
package de.tudarmstadt.ukp.dkpro.core.io.bliki;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createCollectionReader;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

public class BlikiWikipediaReaderTest
{

    @Test
    public void wikipediaReaderTestPlainText()
        throws Exception
    {
        CollectionReader reader = createCollectionReader(
                BlikiWikipediaReader.class,
                BlikiWikipediaReader.PARAM_SOURCE_LOCATION, "http://en.wikipedia.org/w/api.php",
                BlikiWikipediaReader.PARAM_LANGUAGE, "en",
                BlikiWikipediaReader.PARAM_PAGE_TITLES, new String[]{"New York City", "Darmstadt"}
        );

        int i = 0;
        for (JCas jcas : new JCasIterable(reader)) {
            assertNotNull(jcas);
            assertTrue(jcas.getDocumentText().length() > 0);
            System.out.println(jcas.getDocumentText());
            i++;
        }

        assertEquals(2, i);
    }

    @Test
    public void wikipediaReaderTestMarkup()
        throws Exception
    {
        CollectionReader reader = createCollectionReader(
                BlikiWikipediaReader.class,
                BlikiWikipediaReader.PARAM_OUTPUT_PLAIN_TEXT, false,
                BlikiWikipediaReader.PARAM_SOURCE_LOCATION, "http://en.wikipedia.org/w/api.php",
                BlikiWikipediaReader.PARAM_LANGUAGE, "en",
                BlikiWikipediaReader.PARAM_PAGE_TITLES, new String[]{"New York City", "Darmstadt"}
        );

        int i = 0;
        for (JCas jcas : new JCasIterable(reader)) {
            assertNotNull(jcas);
            assertTrue(jcas.getDocumentText().length() > 0);
            System.out.println(jcas.getDocumentText());
            i++;
        }

        assertEquals(2, i);
    }

    @Test
    public void wikipediaReaderUnknownPage()
        throws Exception
    {
        CollectionReader reader = createCollectionReader(
                BlikiWikipediaReader.class,
                BlikiWikipediaReader.PARAM_SOURCE_LOCATION, "http://en.wikipedia.org/w/api.php",
                BlikiWikipediaReader.PARAM_LANGUAGE, "en",
                BlikiWikipediaReader.PARAM_PAGE_TITLES, new String[]{"humbelgrpf"}
        );

        int i = 0;
        for (JCas jcas : new JCasIterable(reader)) {
            assertNotNull(jcas);
            assertTrue(jcas.getDocumentText().length() == 0);
            i++;
        }

        assertEquals(1, i);
    }
}
