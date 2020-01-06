/*
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab and FG Language Technology
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.conll;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.assertj.core.api.Assertions.contentOf;
import static org.assertj.core.api.Assertions.tuple;
import static org.dkpro.core.testing.IOTestRunner.testOneWay;
import static org.dkpro.core.testing.IOTestRunner.testRoundTrip;

import java.io.File;

import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.ReaderAssert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class ConllUReaderWriterTest
{
    @Test
    public void roundTrip()
        throws Exception
    {
        testRoundTrip(
                createReaderDescription(ConllUReader.class),
                createEngineDescription(ConllUWriter.class,
                        ConllUWriter.PARAM_WRITE_TEXT_COMMENT, false),
                "conll/u/conllu-en-orig.conllu");
    }

    @Ignore("This unfortunately doesn't work yet.")
    @Test
    public void roundTripV2EmptyNodes()
        throws Exception
    {
        testRoundTrip(ConllUReader.class, ConllUWriter.class, "conll/u_v2/conllu-empty_nodes.conllu");
    }

    @Test
    public void roundTripV2MorphologicalAnnotation()
        throws Exception
    {
        testRoundTrip(ConllUReader.class, ConllUWriter.class, "conll/u_v2/conllu-morphological_annotation.conllu");
    }

    @Ignore("This unfortunately doesn't work yet.")
    @Test
    public void roundTripV2ParagraphAndDocumentBoundaries()
        throws Exception
    {
        testRoundTrip(
                createReaderDescription(ConllUReader.class),
                createEngineDescription(ConllUWriter.class,
                        ConllUWriter.PARAM_WRITE_TEXT_COMMENT, true),
                "conll/u_v2/conllu-paragraph_and_document_boundaries.conllu");
    }

    @Test
    public void roundTripV2SentenceBoundariesAndComments()
        throws Exception
    {
        testRoundTrip(
                createReaderDescription(ConllUReader.class),
                createEngineDescription(ConllUWriter.class,
                        ConllUWriter.PARAM_WRITE_TEXT_COMMENT, true),
                "conll/u_v2/conllu-sentence_bounaries_and_comments.conllu");
    }

    @Test
    public void roundTripV2SyntacticAnnotation()
        throws Exception
    {
        ReaderAssert.assertThat(ConllUReader.class)
            .readingFrom("src/test/resources/conll/u_v2/conllu-syntactic_annotation.conllu")
            .asJCasList()
            .extracting(
                jcas -> select(jcas, Sentence.class).size(),
                jcas -> select(jcas, Token.class).size())
            .containsExactly(
                tuple(1, 6));
        
        ReaderAssert.assertThat(ConllUReader.class)
            .readingFrom("src/test/resources/conll/u_v2/conllu-syntactic_annotation.conllu")
            .usingWriter(ConllUWriter.class)
            .outputAsString()
                .isEqualToNormalizingNewlines(contentOf(
                        new File("src/test/resources/conll/u_v2/conllu-syntactic_annotation.conllu"),
                        UTF_8));
    }

    @Ignore("This unfortunately doesn't work yet.")
    @Test
    public void roundTripV2UntokenizedText()
        throws Exception
    {
        testRoundTrip(
                createReaderDescription(ConllUReader.class),
                createEngineDescription(ConllUWriter.class,
                        ConllUWriter.PARAM_WRITE_TEXT_COMMENT, true),
                "conll/u_v2/conllu-untokenized_text.conllu");
    }

    @Test
    public void roundTripV2WordsAndTokens()
        throws Exception
    {
        testRoundTrip(ConllUReader.class, ConllUWriter.class, "conll/u_v2/conllu-words_and_tokens.conllu");
    }

    @Test
    public void withComments()
        throws Exception
    {
        testOneWay(
                createReaderDescription(ConllUReader.class),
                createEngineDescription(ConllUWriter.class,
                        ConllUWriter.PARAM_WRITE_TEXT_COMMENT, false),
                "conll/u/conllu-en-ref.conllu",
                "conll/u/conllu-en-orig2.conllu");
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
