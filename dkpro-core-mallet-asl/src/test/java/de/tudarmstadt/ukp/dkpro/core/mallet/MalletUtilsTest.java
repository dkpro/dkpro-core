/*******************************************************************************
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.mallet;

import cc.mallet.types.TokenSequence;
import de.tudarmstadt.ukp.dkpro.core.api.featurepath.FeaturePathException;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.morpha.MorphaLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.Type;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.junit.Test;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.Assert.assertTrue;

public class MalletUtilsTest
{
    private static final String TXT_DIR = "src/test/resources/txt";
    private static final String TXT_FILE_PATTERN = "[+]*.txt";

    @Test
    public void testGenerateSequenceUseLemmas()
            throws ResourceInitializationException, FeaturePathException
    {
        boolean useLemmas = true;
        String language = "en";
        String typeName = Token.class.getName();
        int minTokenLength = 5;
        int minDocumentLength = 200;

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, TXT_DIR,
                TextReader.PARAM_PATTERNS, TXT_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, language);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);
        AnalysisEngineDescription lemmatizer = createEngineDescription(MorphaLemmatizer.class);

        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter, lemmatizer)) {
            Type tokenType = CasUtil.getType(jcas.getCas(), typeName);
            TokenSequence ts = MalletUtils.generateTokenSequence(
                    jcas, tokenType, useLemmas, minTokenLength);
            assertTrue(ts.size() > minDocumentLength);
            ts.forEach((cc.mallet.types.Token token) ->
                    assertTrue(token.getText().length() >= minTokenLength));
        }
    }

    @Test
    public void testGenerateSequence()
            throws ResourceInitializationException, FeaturePathException
    {
        boolean useLemmas = false;
        String language = "en";
        String typeName = Token.class.getName();
        int minTokenLength = 5;
        int minDocumentLength = 300;

        CollectionReaderDescription reader = createReaderDescription(TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION, TXT_DIR,
                TextReader.PARAM_PATTERNS, TXT_FILE_PATTERN,
                TextReader.PARAM_LANGUAGE, language);
        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        for (JCas jcas : SimplePipeline.iteratePipeline(reader, segmenter)) {
            Type tokenType = CasUtil.getType(jcas.getCas(), typeName);
            TokenSequence ts = MalletUtils.generateTokenSequence(
                    jcas, tokenType, useLemmas, minTokenLength);
            assertTrue(ts.size() > minDocumentLength);
            ts.forEach((cc.mallet.types.Token token) ->
                    assertTrue(token.getText().length() >= minTokenLength));
        }
    }
}