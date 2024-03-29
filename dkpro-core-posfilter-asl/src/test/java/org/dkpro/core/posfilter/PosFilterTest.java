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
package org.dkpro.core.posfilter;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.io.conll.Conll2006Reader;
import org.dkpro.core.snowball.SnowballStemmer;
import org.dkpro.core.testing.AssertAnnotations;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class PosFilterTest
{
    @Test
    public void testEnglish1()
        throws Exception
    {
        String testDocument = "src/test/resources/posfilter/text1.conll";

        String[] tokens = { "long", "test", "sentence", "second", "test", "sentence", "More",
                "sentences", "necessary", "tests" };

        runTest("en", testDocument, tokens, null, null, null, null, PosFilter.PARAM_TYPE_TO_REMOVE,
                Token.class.getName(), PosFilter.PARAM_ADJ, true, PosFilter.PARAM_NOUN, true);
    }

    @Test
    public void testEnglish2()
        throws Exception
    {
        String testDocument = "src/test/resources/posfilter/text1.conll";

        String[] tokens = { "is", "long", "test", "sentence", "is", "second", "test", "sentence",
                "More", "sentences", "are", "necessary", "tests" };

        runTest("en", testDocument, tokens, null, null, null, null, PosFilter.PARAM_TYPE_TO_REMOVE,
                Token.class.getName(), PosFilter.PARAM_ADJ, true, PosFilter.PARAM_NOUN, true,
                PosFilter.PARAM_VERB, true);
    }

    @Test
    public void testEnglish3()
        throws Exception
    {
        String testDocument = "src/test/resources/posfilter/text2.conll";

        String[] tokens = { "This", "is", "a", "not", "so", "long", "test", "sentence", "." };
        
        String[] lemmas = { "be", "long", "test", "sentence" };

        runTest("en", testDocument, tokens, lemmas, null, null, null,
                PosFilter.PARAM_TYPE_TO_REMOVE, Lemma.class.getName(), PosFilter.PARAM_ADJ, true,
                PosFilter.PARAM_NOUN, true, PosFilter.PARAM_VERB, true);
    }

    @Test
    public void testEnglish4()
        throws Exception
    {
        String testDocument = "src/test/resources/posfilter/text2.conll";

        String[] tokens = { "This", "is", "a", "not", "so", "long", "test", "sentence", "." };
        
        String[] originalPos = { "VBZ", "JJ", "NN", "NN" };
        
        String[] mappedPos = { "POS_VERB", "POS_ADJ", "POS_NOUN", "POS_NOUN" };

        runTest("en", testDocument, tokens, null, null, originalPos, mappedPos,
                PosFilter.PARAM_TYPE_TO_REMOVE, POS.class.getName(), PosFilter.PARAM_ADJ, true,
                PosFilter.PARAM_NOUN, true, PosFilter.PARAM_VERB, true);
    }

    private void runTest(String language, String testDocument, String[] aTokens, String[] aLemmas,
            String[] aStems, String[] aPOSs, String[] aOrigPOSs, Object... aExtraParams)
        throws Exception
    {
        List<Object> posFilterParams = new ArrayList<Object>();
        posFilterParams.addAll(asList(aExtraParams));

        CollectionReaderDescription reader = createReaderDescription(Conll2006Reader.class, 
                Conll2006Reader.PARAM_SOURCE_LOCATION, testDocument,
                Conll2006Reader.PARAM_POS_TAG_SET, "ptb",
                Conll2006Reader.PARAM_LANGUAGE, "en");
        
        AnalysisEngineDescription aggregate = createEngineDescription(
                createEngineDescription(SnowballStemmer.class),
                createEngineDescription(PosFilter.class,
                        posFilterParams.toArray(new Object[posFilterParams.size()])));
        
        JCas jcas = SimplePipeline.iteratePipeline(reader, aggregate).iterator().next();

        Type stemType = jcas.getCas().getTypeSystem().getType(Stem.class.getCanonicalName());
        Type lemmaType = jcas.getCas().getTypeSystem().getType(Lemma.class.getCanonicalName());
        Type posType = jcas.getCas().getTypeSystem().getType(POS.class.getCanonicalName());
        Collection<Token> tokens = select(jcas, Token.class);
        AssertAnnotations.assertToken(aTokens, tokens);
        if (aLemmas != null) {
            AssertAnnotations.assertLemma(aLemmas, select(jcas, Lemma.class));
            for (Token t : tokens) {
                assertEquals(t.getLemma(), getAnnotation(lemmaType, t));
            }
        }
        if (aStems != null) {
            AssertAnnotations.assertStem(aStems, select(jcas, Stem.class));
            for (Token t : tokens) {
                assertEquals(t.getStem(), getAnnotation(stemType, t));
            }
        }
        if (aPOSs != null) {
            AssertAnnotations.assertPOS(aOrigPOSs, aPOSs, select(jcas, POS.class));
            for (Token t : tokens) {
                assertEquals(t.getPos(), getAnnotation(posType, t));
            }
        }
    }

    private AnnotationFS getAnnotation(Type type, AnnotationFS annotation)
    {
        List<AnnotationFS> annotations = CasUtil.selectCovered(type, annotation);

        if (annotations.size() != 1) {
            return null;
        }

        return annotations.get(0);
    }
}
