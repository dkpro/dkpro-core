/*******************************************************************************
 * Copyright 2014
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

package de.tudarmstadt.ukp.dkpro.core.textnormalizer.transformation;

import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.assertTransformedText;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertEquals;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.JCasIterable;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.io.text.TokenizedTextWriter;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;

public class HyphenationRemoverTest
{
    @Test
    public void testHyphenationRemover()
        throws Exception
    {
        String inputText = "Ich habe ein- en super-tollen Bär-\nen.";
        String normalizedText = "Ich habe einen super-tollen Bären.";

        AnalysisEngineDescription normalizer = createEngineDescription(HyphenationRemover.class,
                HyphenationRemover.PARAM_MODEL_LOCATION, "src/test/resources/dictionary/ngerman");

        assertTransformedText(normalizedText, inputText, "de", normalizer);
    }

    @Test
    public void testHyphenationRemoverInPipeline()
        throws Exception
    {
        final String language = "de";
        final String variant = "maxent";
        final String text = "Ich habe ein- en super-tollen Bär-\nen. "
                + "Für eine Registrierung einer Organisation und eine EMail Adresse.";
        final String[] sentences = { "Ich habe einen super-tollen Bären.",
                "Für eine Registrierung einer Organisation und eine EMail Adresse." };
        final String[] tokens = { "Ich", "habe", "einen", "super-tollen", "Bären", ".", "Für",
                "eine", "Registrierung", "einer", "Organisation", "und", "eine", "EMail", "Adresse",
                "." };

        AnalysisEngineDescription normalizerAndSegmenter = createEngineDescription(
                createEngineDescription(HyphenationRemover.class,
                        HyphenationRemover.PARAM_MODEL_LOCATION,
                        "src/test/resources/dictionary/ngerman"),
                createEngineDescription(OpenNlpSegmenter.class, OpenNlpSegmenter.PARAM_LANGUAGE,
                        "de", OpenNlpSegmenter.PARAM_VARIANT, variant));
        AnalysisEngine engine = createEngine(normalizerAndSegmenter);

        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage(language);
        jcas.setDocumentText(text);
        engine.process(jcas);

        AssertAnnotations.assertSentence(sentences, select(jcas, Sentence.class));
        AssertAnnotations.assertToken(tokens, select(jcas, Token.class));
    }

    @Test
    public void testHyphenationRemoverInPipelineReaderWriter()
        throws Exception
    {
        final String language = "de";
        final String variant = "maxent";
        String sourcePath = "src/test/resources/texts/test3.txt";
        String outputPath = "src/test/resources/texts/test3-out.txt";

        final String[] sentences = { "Ich habe einen super-tollen Bären .",
                "Für eine Registrierung einer Organisation und eine EMail Adresse ." };
        final String expectedOutput = sentences[0] + System.lineSeparator() + sentences[1]
                + System.lineSeparator();

        /* process input file */
        final CollectionReader reader = createReader(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, sourcePath);

        AnalysisEngineDescription writer = createEngineDescription(TokenizedTextWriter.class,
                TokenizedTextWriter.PARAM_TARGET_LOCATION, outputPath,
                TokenizedTextWriter.PARAM_SINGULAR_TARGET, true,
                TokenizedTextWriter.PARAM_OVERWRITE, true);

        AnalysisEngineDescription hyphenationRemover = createEngineDescription(
                HyphenationRemover.class, HyphenationRemover.PARAM_MODEL_LOCATION,
                "src/test/resources/dictionary/ngerman");

        AnalysisEngineDescription segmenter = createEngineDescription(OpenNlpSegmenter.class,
                OpenNlpSegmenter.PARAM_LANGUAGE, language, OpenNlpSegmenter.PARAM_VARIANT, variant);

        SimplePipeline.runPipeline(reader, hyphenationRemover, segmenter, writer);

        /* check the output file */
        final CollectionReaderDescription readerDesc = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, outputPath);

        JCas jcas = new JCasIterable(readerDesc).iterator().next();
        assertEquals(expectedOutput, jcas.getDocumentText());
    }
}