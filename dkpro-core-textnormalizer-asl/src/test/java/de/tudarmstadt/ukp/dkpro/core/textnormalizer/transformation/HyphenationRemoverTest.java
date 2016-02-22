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
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.junit.Assert.assertEquals;

import java.io.File;

import static org.apache.commons.io.FileUtils.*;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.io.text.TokenizedTextWriter;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpSegmenter;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class HyphenationRemoverTest
{
    private static final String RESOURCE_GERMAN_DICTIONARY = "src/test/resources/dictionary/ngerman";

    @Test
    public void testHyphenationRemover()
        throws Exception
    {
        String inputText = "Ich habe ein- en super-tollen Bär-\nen.";
        String normalizedText = "Ich habe einen super-tollen Bären.";

        AnalysisEngineDescription normalizer = createEngineDescription(HyphenationRemover.class,
                HyphenationRemover.PARAM_MODEL_LOCATION, RESOURCE_GERMAN_DICTIONARY);

        assertTransformedText(normalizedText, inputText, "de", normalizer);
    }

    @Test
    public void testHyphenationRemoverInPipelineReaderWriter()
        throws Exception
    {
        File outputPath = testContext.getTestOutputFolder();
        
        final String language = "de";
        final String variant = "maxent";
        String sourcePath = "src/test/resources/texts/test3.txt";

        final String expected = "Ich habe einen super-tollen Bären .\n"+
                "Für eine Registrierung einer Organisation und eine EMail Adresse .\n";

        /* process input file */
        final CollectionReader reader = createReader(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, sourcePath);

        AnalysisEngineDescription hyphenationRemover = createEngineDescription(
                HyphenationRemover.class, 
                HyphenationRemover.PARAM_MODEL_LOCATION, RESOURCE_GERMAN_DICTIONARY);

        AnalysisEngineDescription segmenter = createEngineDescription(
                OpenNlpSegmenter.class,
                OpenNlpSegmenter.PARAM_LANGUAGE, language, 
                OpenNlpSegmenter.PARAM_VARIANT, variant);

        AnalysisEngineDescription writer = createEngineDescription(
                TokenizedTextWriter.class,
                TokenizedTextWriter.PARAM_TARGET_LOCATION, new File(outputPath, "test3.txt"),
                TokenizedTextWriter.PARAM_SINGULAR_TARGET, true,
                TokenizedTextWriter.PARAM_OVERWRITE, true);

        SimplePipeline.runPipeline(reader, hyphenationRemover, segmenter, writer);

        assertEquals(expected, readFileToString(new File(outputPath, "test3.txt"), "UTF-8"));
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}