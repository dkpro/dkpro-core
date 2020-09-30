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
package org.dkpro.core.io.tei;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.util.Files.contentOf;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.assertj.core.api.ListAssert;
import org.dkpro.core.io.imscwb.ImsCwbWriter;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.ReaderAssert;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TeiReaderTest
{
    @Test
    public void digibibTest()
        throws Exception
    {
        ListAssert<JCas> casList = ReaderAssert.assertThat(
                TeiReader.class,
                TeiReader.PARAM_OMIT_IGNORABLE_WHITESPACE, true,
                TeiReader.PARAM_LANGUAGE, "de",
                TeiReader.PARAM_SOURCE_LOCATION, "classpath:/digibib",
                TeiReader.PARAM_PATTERNS, new String[] { "[+]*.xml" })
            .asJCasList();
        
        casList
            .extracting(
                jcas -> DocumentMetaData.get(jcas).getDocumentId(),
                jcas -> jcas.getDocumentText().length())
            .containsExactly(
                tuple("Literatur-Balde,-Jacob.xml#1", 152),
                tuple("Literatur-Balde,-Jacob.xml#2", 14378),
                tuple("Literatur-Balde,-Jacob.xml#3", 532),
                tuple("Literatur-Balde,-Jacob.xml#4", 1322),
                tuple("Literatur-Balde,-Jacob.xml#5", 26588),
                tuple("Literatur-Besser,-Johann-von.xml#1", 279),
                tuple("Literatur-Besser,-Johann-von.xml#2", 3846),
                tuple("Literatur-Besser,-Johann-von.xml#3", 22363),
                tuple("Literatur-Besser,-Johann-von.xml#4", 3576),
                tuple("Literatur-Besser,-Johann-von.xml#5", 3369),
                tuple("Literatur-Besser,-Johann-von.xml#6", 3903),
                tuple("Literatur-Besser,-Johann-von.xml#7", 2035),
                tuple("Literatur-Kobell,-Franz-von.xml#1", 164),
                tuple("Literatur-Kobell,-Franz-von.xml#2", 2078),
                tuple("Literatur-Kobell,-Franz-von.xml#3", 50730),
                tuple("Literatur-Marcel,-Gabriel.xml#1", 52696),
                tuple("Literatur-Meister,-Johann-Gottlieb.xml#1", 41418));
    }

    @Test
    public void thatBrownCorpusIsReadCorrectly()
        throws Exception
    {
        ReaderAssert.assertThat(
                TeiReader.class,
                TeiReader.PARAM_LANGUAGE, "en",
                TeiReader.PARAM_SOURCE_LOCATION, "classpath:/brown_tei/",
                TeiReader.PARAM_PATTERNS, new String[] { "[+]*.xml" })
            .asJCasList()
            .hasSize(3)
            .element(0)
            .extracting(
                jcas -> jcas.select(Token.class).count(),
                jcas -> jcas.select(POS.class).count(),
                jcas -> jcas.select(Sentence.class).count(),
                jcas -> jcas.select(Sentence.class).get(0).getCoveredText())
            .containsExactly(
                2242l, 
                2242l, 
                98l, 
                "The Fulton County Grand Jury said Friday an investigation of "
                + "Atlanta's recent primary election produced `` no evidence '' that any "
                + "irregularities took place .");
    }

    @Test
    public void thatBrownCorpusTeiCanBeReadFromClasspath()
        throws Exception
    {
        ReaderAssert.assertThat(
                TeiReader.class,
                TeiReader.PARAM_LANGUAGE, "en",
                TeiReader.PARAM_SOURCE_LOCATION, "classpath:/brown_tei/",
                TeiReader.PARAM_PATTERNS, new String[] { "[+]*.xml" })
            .usingWriter(
                ImsCwbWriter.class,
                ImsCwbWriter.PARAM_WRITE_CPOS, true,
                ImsCwbWriter.PARAM_SENTENCE_TAG, "sentence")
            .writingToSingular("${TARGET}/brown.vrt")
            .outputAsString()
            .isEqualToNormalizingNewlines(contentOf(
                    new File("src/test/resources/brown_tei/brown-ref.vrt"), UTF_8));
    }

    @Test
    public void thatBrownCorpusTeiCanBeReadFromGZippedFile()
        throws Exception
    {
        ReaderAssert.assertThat(
                TeiReader.class,
                TeiReader.PARAM_LANGUAGE, "en",
                TeiReader.PARAM_SOURCE_LOCATION, "classpath:/brown_tei_gzip/",
                TeiReader.PARAM_PATTERNS, new String[] { "[+]*.xml.gz" })
            .usingWriter(
                ImsCwbWriter.class,
                ImsCwbWriter.PARAM_WRITE_CPOS, true,
                ImsCwbWriter.PARAM_SENTENCE_TAG, "sentence")
            .writingToSingular("${TARGET}/brown.vrt")
            .outputAsString()
            .isEqualToNormalizingNewlines(contentOf(
                new File("src/test/resources/brown_tei_gzip/brown-ref.vrt"), UTF_8));
    }

    @Test
    public void brownReaderTest_noSentences()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
            TeiReader.class,
            TeiReader.PARAM_LANGUAGE, "en",
            TeiReader.PARAM_SOURCE_LOCATION, "classpath:/brown_tei/",
            TeiReader.PARAM_PATTERNS, new String[] { "[+]*.xml" },
            TeiReader.PARAM_READ_SENTENCE, false);

        int i = 0;
        for (JCas jcas : new JCasIterable(reader)) {
            DocumentMetaData meta = DocumentMetaData.get(jcas);
            String text = jcas.getDocumentText();
            // System.out.printf("%s - %d%n", meta.getDocumentId(), text.length());

            if (i == 0) {
                assertEquals(2242, JCasUtil.select(jcas, Token.class).size());
                assertEquals(2242, JCasUtil.select(jcas, POS.class).size());
                assertEquals(0, JCasUtil.select(jcas, Sentence.class).size());
            }
            i++;
        }

        assertEquals(3, i);
    }

    @Test
    public void brownReaderTest_noToken_noPOS()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
            TeiReader.class,
            TeiReader.PARAM_LANGUAGE, "en",
            TeiReader.PARAM_SOURCE_LOCATION, "classpath:/brown_tei/",
            TeiReader.PARAM_PATTERNS, new String[] { "[+]*.xml" },
            TeiReader.PARAM_READ_TOKEN, false,
            TeiReader.PARAM_READ_POS, false
        );

        int i = 0;
        for (JCas jcas : new JCasIterable(reader)) {
            DocumentMetaData meta = DocumentMetaData.get(jcas);
            String text = jcas.getDocumentText();
            // System.out.printf("%s - %d%n", meta.getDocumentId(), text.length());

            if (i == 0) {
                assertEquals(0, JCasUtil.select(jcas, Token.class).size());
                assertEquals(0, JCasUtil.select(jcas, POS.class).size());
                assertEquals(98, JCasUtil.select(jcas, Sentence.class).size());
            }
            i++;
        }

        assertEquals(3, i);
    }

    @Test(expected = IllegalStateException.class)
    public void brownReaderTest_expectedException()
        throws Exception
    {
        CollectionReaderDescription reader = createReaderDescription(
                TeiReader.class,
                TeiReader.PARAM_LANGUAGE, "en",
                TeiReader.PARAM_SOURCE_LOCATION, "classpath:/brown_tei/",
                TeiReader.PARAM_PATTERNS, new String[] { "[+]*.xml" },
                TeiReader.PARAM_READ_POS, true,
                TeiReader.PARAM_READ_TOKEN, false);

        for (JCas jcas : new JCasIterable(reader)) {
            // should never get here
            // System.out.println(jcas.getDocumentText());
        }
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
