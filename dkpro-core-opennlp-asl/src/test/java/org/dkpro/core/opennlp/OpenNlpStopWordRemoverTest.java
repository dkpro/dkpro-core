/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.opennlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.JCasFactory.createJCas;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.dkpro.core.testing.AssertAnnotations.assertToken;

import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.TestRunner;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class OpenNlpStopWordRemoverTest
{
    @Test
    public void testBundledEnglishList() throws Exception
    {
        var jcas = TestRunner.runTest(createEngineDescription(OpenNlpStopWordRemover.class), "en",
                "The quick brown fox jumps over the lazy dog and was happy .");

        assertToken(new String[] { "quick", "brown", "fox", "jumps", "lazy", "dog", "happy", "." },
                select(jcas, Token.class));
    }

    @Test
    public void testBundledGermanListViaLanguageParameter() throws Exception
    {
        var jcas = TestRunner.runTest(createEngineDescription(OpenNlpStopWordRemover.class, //
                OpenNlpStopWordRemover.PARAM_LANGUAGE, "de"), "en", "Der Hund und die Katze");

        assertToken(new String[] { "Hund", "Katze" }, select(jcas, Token.class));
    }

    @Test
    public void testCoveredAnnotationsAreRemoved() throws Exception
    {
        var jcas = createJCas();
        jcas.setDocumentLanguage("en");
        jcas.setDocumentText("The dog");
        var the = new Token(jcas, 0, 3);
        the.addToIndexes();
        var dog = new Token(jcas, 4, 7);
        dog.addToIndexes();
        var lemmaThe = new Lemma(jcas, 0, 3);
        lemmaThe.setValue("the");
        lemmaThe.addToIndexes();
        var lemmaDog = new Lemma(jcas, 4, 7);
        lemmaDog.setValue("dog");
        lemmaDog.addToIndexes();

        runPipeline(jcas, createEngineDescription(OpenNlpStopWordRemover.class));

        assertToken(new String[] { "dog" }, select(jcas, Token.class));
        AssertAnnotations.assertLemma(new String[] { "dog" }, select(jcas, Lemma.class));
    }

    @Test
    public void testCustomListCaseInsensitive() throws Exception
    {
        var jcas = TestRunner.runTest(createEngineDescription(OpenNlpStopWordRemover.class, //
                OpenNlpStopWordRemover.PARAM_MODEL_LOCATION, "classpath:/stopwords/custom.txt"),
                "en", "The Fox and the dog");

        assertToken(new String[] { "The", "and", "the" }, select(jcas, Token.class));
    }

    @Test
    public void testCustomListCaseSensitive() throws Exception
    {
        var jcas = TestRunner.runTest(createEngineDescription(OpenNlpStopWordRemover.class, //
                OpenNlpStopWordRemover.PARAM_MODEL_LOCATION, "classpath:/stopwords/custom.txt", //
                OpenNlpStopWordRemover.PARAM_CASE_SENSITIVE, true), "en", "The Fox fox dog Dog");

        assertToken(new String[] { "The", "Fox", "dog" }, select(jcas, Token.class));
    }

    @Test
    public void testUnsupportedLanguage()
    {
        assertThatThrownBy(() -> TestRunner
                .runTest(createEngineDescription(OpenNlpStopWordRemover.class), "zh", "我 爱 北京"))
                        .hasRootCauseMessage(
                                "No stop word list bundled with OpenNLP for language [zh]. "
                                        + "Supported languages: [bg, da, de, en, es, fi, fr, it, "
                                        + "nl, pt, ru]");
    }
}
