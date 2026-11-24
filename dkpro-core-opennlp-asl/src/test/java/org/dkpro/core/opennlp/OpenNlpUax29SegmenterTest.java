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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.dkpro.core.testing.AssertAnnotations.assertSentence;
import static org.dkpro.core.testing.AssertAnnotations.assertToken;

import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.AssumeResource;
import org.dkpro.core.testing.harness.SegmenterHarness;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class OpenNlpUax29SegmenterTest
{
    @Test
    public void testEnglish() throws Exception
    {
        var jcas = process("en", true,
                "Don't stop: it's 3.14 or 1,000 users! E-mail me at 10:30 today.");

        assertSentence( //
                new String[] { "Don't stop: it's 3.14 or 1,000 users!",
                        "E-mail me at 10:30 today." },
                select(jcas, Sentence.class));
        assertToken( //
                new String[] { "Don't", "stop", ":", "it's", "3.14", "or", "1,000", "users", "!",
                        "E", "-", "mail", "me", "at", "10", ":", "30", "today", "." },
                select(jcas, Token.class));
    }

    @Test
    public void testLanguageParameter() throws Exception
    {
        // There is no sentence model for the document language, but the language parameter
        // selects the English one
        var engine = createEngine(OpenNlpUax29Segmenter.class, //
                OpenNlpUax29Segmenter.PARAM_LANGUAGE, "en");

        var jcas = engine.newJCas();
        jcas.setDocumentLanguage("zh");
        jcas.setDocumentText("This is one. This is two.");
        engine.process(jcas);

        assertSentence(new String[] { "This is one.", "This is two." },
                select(jcas, Sentence.class));
    }

    @Test
    public void testChineseWithoutSentences() throws Exception
    {
        // There is no OpenNLP sentence model for Chinese, but tokenization works without a model
        var jcas = process("zh", false, "我爱北京。");

        assertToken(new String[] { "我", "爱", "北", "京", "。" }, select(jcas, Token.class));
    }

    @Test
    public void testEmojiAndWhitespace() throws Exception
    {
        var jcas = process("en", false, "  I ❤️  UIMA \t\n 👍🏽 ");

        assertToken(new String[] { "I", "❤️", "UIMA", "👍🏽" }, select(jcas, Token.class));
    }

    @Test
    public void runHarness() throws Throwable
    {
        var aed = createEngineDescription(OpenNlpUax29Segmenter.class);

        // UAX #29 word boundaries differ from the tokenization expected by the harness for
        // abbreviations (de.1, en.1), hyphens (de.4, en.1), contractions (en.3, en.6),
        // ellipses (en.7) and smileys (en.9)
        SegmenterHarness.run(aed, (language, variant) -> {
            AssumeResource.assumeResource(OpenNlpUax29Segmenter.class, "sentence", language,
                    "maxent");
        }, "de.1", "de.4", "en.1", "en.3", "en.6", "en.7", "en.9");
    }

    @Test
    public void testZoning() throws Exception
    {
        SegmenterHarness.testZoning(OpenNlpUax29Segmenter.class);
    }

    private JCas process(String aLanguage, boolean aWriteSentence, String aText) throws Exception
    {
        var engine = createEngine(OpenNlpUax29Segmenter.class, //
                OpenNlpUax29Segmenter.PARAM_WRITE_SENTENCE, aWriteSentence);

        // Cannot use TestRunner because that uses TokenBuilder to create a segmentation.
        var jcas = engine.newJCas();
        jcas.setDocumentLanguage(aLanguage);
        jcas.setDocumentText(aText);
        engine.process(jcas);
        return jcas;
    }
}
