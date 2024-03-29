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
package org.dkpro.core.io.imscwb;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.iteratePipeline;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.io.ResourceCollectionReaderBase;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class ImsCwbReaderTest
{
    @Test
    public void wackyTest()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                ImsCwbReader.class,
                ImsCwbReader.PARAM_SOURCE_LOCATION, "src/test/resources/wacky/",
                ImsCwbReader.PARAM_LANGUAGE, "de",
                ImsCwbReader.PARAM_SOURCE_ENCODING, "ISO-8859-15",
                ResourceCollectionReaderBase.PARAM_PATTERNS, "[+]test.txt");

        String firstSentence = "Nikita ( La Femme Nikita ) Dieser Episodenf\u00FChrer wurde von " +
                "September 1998 bis Mai 1999 von Konstantin C.W. Volkmann geschrieben und im Mai " +
                "2000 von Stefan B\u00F6rzel \u00FCbernommen . ";

        int i = 0;
        for (JCas jcas : iteratePipeline(reader)) {
            // System.out.println(jcas.getDocumentText());
            if (i == 0) {
                assertEquals(11406, select(jcas, Token.class).size());
                assertEquals(11406, select(jcas, Lemma.class).size());
                assertEquals(11406, select(jcas, POS.class).size());
                assertEquals(717, select(jcas, Sentence.class).size());

                assertEquals(firstSentence, select(jcas, Sentence.class).iterator().next()
                        .getCoveredText());

                assertEquals("http://www.epguides.de/nikita.htm", DocumentMetaData.get(jcas)
                        .getDocumentTitle());
            }
            i++;
        }

        assertEquals(4, i);

    }

    @Test
    public void wackyTest_noAnnotations()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                ImsCwbReader.class,
                ImsCwbReader.PARAM_SOURCE_LOCATION, "src/test/resources/wacky/",
                ImsCwbReader.PARAM_PATTERNS, "[+]test.txt",
                ImsCwbReader.PARAM_LANGUAGE, "de",
                ImsCwbReader.PARAM_SOURCE_ENCODING, "ISO-8859-15",
                ImsCwbReader.PARAM_READ_TOKEN, false,
                ImsCwbReader.PARAM_READ_LEMMA, false,
                ImsCwbReader.PARAM_READ_POS, false,
                ImsCwbReader.PARAM_READ_SENTENCES, false);

        int i = 0;
        for (JCas jcas : iteratePipeline(reader)) {
            if (i == 0) {
                assertEquals(0, select(jcas, Token.class).size());
                assertEquals(0, select(jcas, POS.class).size());
                assertEquals(0, select(jcas, Sentence.class).size());
            }
            i++;
        }

        assertEquals(4, i);
    }

    @Test
    public void wackyTest__expectedException()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                ImsCwbReader.class,
                ImsCwbReader.PARAM_SOURCE_LOCATION, "src/test/resources/wacky",
                ImsCwbReader.PARAM_LANGUAGE, "de",
                ImsCwbReader.PARAM_SOURCE_ENCODING, "ISO-8859-15",
                ImsCwbReader.PARAM_READ_TOKEN, false,
                ImsCwbReader.PARAM_READ_LEMMA, true,
                ImsCwbReader.PARAM_READ_POS, false,
                ImsCwbReader.PARAM_READ_SENTENCES, false);
        
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> iteratePipeline(reader).iterator().next());
    }
}
