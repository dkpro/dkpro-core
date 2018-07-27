/*
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
 */

package de.tudarmstadt.ukp.dkpro.core.textnormalizer.annotations;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.tokit.BreakIteratorSegmenter;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.text.StringReader;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;

public class AnnotationByTextFilterTest
{
    private static final String LEXICON_FILE = "src/test/resources/sentiws_100.txt";

    @Test
    public void testSentiTokens()
        throws ResourceInitializationException
    {
        String inputText = "Ich begegne dem Abbau mit abfälligen Gedanken .";
        String[] expectedTokens = new String[] { "Abbau", "abfälligen" };

        CollectionReaderDescription stringReader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, inputText,
                StringReader.PARAM_LANGUAGE, "de");
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription wordsFilter = createEngineDescription(
                AnnotationByTextFilter.class,
                AnnotationByTextFilter.PARAM_MODEL_LOCATION, LEXICON_FILE);

        for (JCas jcas : SimplePipeline.iteratePipeline(stringReader, segmenter, wordsFilter)) {
            AssertAnnotations.assertToken(expectedTokens, select(jcas, Token.class));
        }
    }

}
