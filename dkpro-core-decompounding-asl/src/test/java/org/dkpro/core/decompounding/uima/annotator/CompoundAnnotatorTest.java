/*
 * Copyright 2010
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
 **/
package org.dkpro.core.decompounding.uima.annotator;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.ExternalResourceFactory.createResourceDescription;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.testing.factory.TokenBuilder;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.dkpro.core.decompounding.uima.resource.AsvToolboxSplitterResource;
import org.dkpro.core.decompounding.uima.resource.FrequencyRankerResource;
import org.dkpro.core.decompounding.uima.resource.LeftToRightSplitterResource;
import org.dkpro.core.decompounding.uima.resource.RankerResource;
import org.dkpro.core.decompounding.uima.resource.SharedDictionary;
import org.dkpro.core.decompounding.uima.resource.SharedFinder;
import org.dkpro.core.decompounding.uima.resource.SharedLinkingMorphemes;
import org.dkpro.core.decompounding.uima.resource.SharedPatriciaTries;
import org.dkpro.core.decompounding.uima.resource.SplitterResource;
import org.dkpro.core.decompounding.web1t.LuceneIndexer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.CompoundPart;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.LinkingMorpheme;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Split;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class CompoundAnnotatorTest
{

    static File source = new File("src/test/resources/ranking/n-grams");
    static File index = new File("target/test/index");
    static String jWeb1TPath = "src/test/resources/web1t/de";
    static String indexPath = "target/test/index";


    @BeforeClass
    public static void createIndex()
        throws Exception
    {
        index.mkdirs();

        LuceneIndexer indexer = new LuceneIndexer(source, index);
        indexer.index();
    }

    @Test
    public void testWithoutRanking() throws CASException, UIMAException {
        AnalysisEngineDescription aed = createEngineDescription(
                CompoundAnnotator.class,
                CompoundAnnotator.RES_SPLITTING_ALGO,
                createResourceDescription(
                        LeftToRightSplitterResource.class,
                        SplitterResource.PARAM_DICT_RESOURCE,
                        createResourceDescription(SharedDictionary.class),
                        SplitterResource.PARAM_MORPHEME_RESOURCE,
                        createResourceDescription(SharedLinkingMorphemes.class)));
        String[] splits = new String[] { "Aktion", "s", "plan", "Doppel","prozessormaschine"};
        String[] compoundsParts = new String[] { "Aktion", "plan", "Doppel", "prozessormaschine"};
        runAnnotator(aed, splits, compoundsParts);
    }

    @Test
    public void testWithAsvToolbox() throws CASException, UIMAException {
        AnalysisEngineDescription aed = createEngineDescription(
                CompoundAnnotator.class,
                CompoundAnnotator.RES_SPLITTING_ALGO,
                createResourceDescription(
                        AsvToolboxSplitterResource.class,
                        AsvToolboxSplitterResource.PARAM_DICT_RESOURCE,
                        createResourceDescription(SharedDictionary.class),
                        AsvToolboxSplitterResource.PARAM_MORPHEME_RESOURCE,
                        createResourceDescription(SharedLinkingMorphemes.class),
                        AsvToolboxSplitterResource.PARAM_PATRICIA_TRIES_RESOURCE,
                        createResourceDescription(SharedPatriciaTries.class)),
                CompoundAnnotator.RES_RANKING_ALGO,
                createResourceDescription(
                        FrequencyRankerResource.class,
                        RankerResource.PARAM_FINDER_RESOURCE,
                        createResourceDescription(SharedFinder.class,
                                SharedFinder.PARAM_INDEX_PATH, indexPath,
                                SharedFinder.PARAM_NGRAM_LOCATION, jWeb1TPath)));
        String[] splits = new String[] { "Aktion", "s", "plan", "Doppel","prozessormaschine",
                "prozessor","maschine"};
        String[] compoundsParts = new String[] { "Aktion", "plan", "Doppel", "prozessormaschine",
                "prozessor","maschine"};
        runAnnotator(aed, splits, compoundsParts);
    }


    @Test
    public void testWithDefaults() throws CASException, UIMAException {
        AnalysisEngineDescription aed = createEngineDescription(
                CompoundAnnotator.class,
                CompoundAnnotator.RES_SPLITTING_ALGO,
                createResourceDescription(
                        LeftToRightSplitterResource.class,
                        SplitterResource.PARAM_DICT_RESOURCE,
                        createResourceDescription(SharedDictionary.class),
                        SplitterResource.PARAM_MORPHEME_RESOURCE,
                        createResourceDescription(SharedLinkingMorphemes.class)),
                CompoundAnnotator.RES_RANKING_ALGO,
                createResourceDescription(
                        FrequencyRankerResource.class,
                        RankerResource.PARAM_FINDER_RESOURCE,
                        createResourceDescription(SharedFinder.class,
                                SharedFinder.PARAM_INDEX_PATH, indexPath,
                                SharedFinder.PARAM_NGRAM_LOCATION, jWeb1TPath)));
        String[] splits = new String[] { "Aktion", "s", "plan", "Doppel","prozessormaschine",
                "prozessor","maschine"};
        String[] compoundsParts = new String[] { "Aktion", "plan", "Doppel", "prozessormaschine",
                "prozessor","maschine"};
        runAnnotator(aed, splits, compoundsParts);
    }

    private void runAnnotator(AnalysisEngineDescription aed, String[] splits,
            String[] compoundsParts)
        throws CASException, UIMAException
    {
        // Create Analysis Engine
        AnalysisEngine ae = AnalysisEngineFactory.createEngine(aed);

        // Create cas with token
        CAS cas = ae.newCAS();
        TokenBuilder<Token, Annotation> builder = new TokenBuilder<Token, Annotation>(Token.class,
                Annotation.class);
        builder.buildTokens(cas.getJCas(), "Aktionsplan im Doppelprozessormaschine");
        ae.typeSystemInit(cas.getTypeSystem());
        ae.process(cas);

        // Check if splits and morphemes are equal
        assertThat(getAnnotation(cas.getJCas(), Compound.class))
                .containsExactly("Aktionsplan", "Doppelprozessormaschine");
        assertThat(getAnnotation(cas.getJCas(), Split.class))
                .containsExactly(splits);
        assertThat(getAnnotation(cas.getJCas(), CompoundPart.class))
                .containsExactly(compoundsParts);
        assertThat(getAnnotation(cas.getJCas(), LinkingMorpheme.class))
                .containsExactly("s");
    }

    protected <T extends Annotation> String[] getAnnotation(JCas aCas, Class<T> aClass)
    {
        List<String> result = new ArrayList<String>();
        for (T s : JCasUtil.select(aCas, aClass)) {
            result.add(s.getCoveredText());
        }

        return result.toArray(new String[] {});
    }

    @AfterClass
    public static void tearDown()
        throws Exception
    {
        // Delete index again
        for (File f : index.listFiles()) {
            for (File _f : f.listFiles()) {
                _f.delete();
            }
            f.delete();
        }

        index.delete();
    }
}
