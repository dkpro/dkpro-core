/*******************************************************************************
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
 *******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.decompounding.uima.annotator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.ExternalResourceFactory.createExternalResourceDescription;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.testing.factory.TokenBuilder;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.CompoundPart;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.LinkingMorpheme;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Split;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.decompounding.uima.resource.FrequencyRankerResource;
import de.tudarmstadt.ukp.dkpro.core.decompounding.uima.resource.LeftToRightSplitterResource;
import de.tudarmstadt.ukp.dkpro.core.decompounding.uima.resource.SharedDictionary;
import de.tudarmstadt.ukp.dkpro.core.decompounding.uima.resource.SharedFinder;
import de.tudarmstadt.ukp.dkpro.core.decompounding.uima.resource.SharedLinkingMorphemes;
import de.tudarmstadt.ukp.dkpro.core.decompounding.web1t.LuceneIndexer;

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
    public void testAnnotator()
        throws Exception
    {
        AnalysisEngineDescription aed = createPrimitiveDescription(
                CompoundAnnotator.class,
                CompoundAnnotator.PARAM_SPLITTING_ALGO,
                createExternalResourceDescription(LeftToRightSplitterResource.class),
                CompoundAnnotator.PARAM_RANKING_ALGO,
                createExternalResourceDescription(FrequencyRankerResource.class),
                CompoundAnnotator.PARAM_DICT_RESOURCE,
                createExternalResourceDescription(
                        SharedDictionary.class,
                        SharedDictionary.PARAM_DICTIONARY_PATH, "src/test/resources/dic/de_DE.dic"),
                CompoundAnnotator.PARAM_MORPHEME_RESOURCE,
                createExternalResourceDescription(
                        SharedLinkingMorphemes.class,
                        SharedLinkingMorphemes.PARAM_MORPHEMES_PATH, "src/test/resources/dic/de_DE.linking"),
                CompoundAnnotator.PARAM_FINDER_RESOURCE,
                createExternalResourceDescription(SharedFinder.class,
                        SharedFinder.PARAM_INDEX_PATH, indexPath,
                        SharedFinder.PARAM_NGRAM_LOCATION, jWeb1TPath));
                runAnnotator(aed);

    }

    @Test
    public void testWithDefaults() throws CASException, UIMAException {
        AnalysisEngineDescription aed = createPrimitiveDescription(
                CompoundAnnotator.class,
                CompoundAnnotator.PARAM_SPLITTING_ALGO,
                createExternalResourceDescription(LeftToRightSplitterResource.class),
                CompoundAnnotator.PARAM_RANKING_ALGO,
                createExternalResourceDescription(FrequencyRankerResource.class),
                CompoundAnnotator.PARAM_DICT_RESOURCE,
                createExternalResourceDescription(SharedDictionary.class),
                CompoundAnnotator.PARAM_MORPHEME_RESOURCE,
                createExternalResourceDescription(SharedLinkingMorphemes.class),
                CompoundAnnotator.PARAM_FINDER_RESOURCE,
                createExternalResourceDescription(SharedFinder.class,
                        SharedFinder.PARAM_INDEX_PATH, indexPath,
                        SharedFinder.PARAM_NGRAM_LOCATION, jWeb1TPath));
                runAnnotator(aed);
    }

    private void runAnnotator(AnalysisEngineDescription aed)
        throws CASException, UIMAException{
        // Create Analysis Engine
        AnalysisEngine ae = AnalysisEngineFactory.createAggregate(aed);

        // Create cas with token
        CAS cas = ae.newCAS();
        TokenBuilder<Token, Annotation> builder = new TokenBuilder<Token, Annotation>(Token.class,
                Annotation.class);
        builder.buildTokens(cas.getJCas(), "Aktionsplan im Doppelprozessormaschine");
        ae.typeSystemInit(cas.getTypeSystem());
        ae.process(cas);

        String[] splits = new String[] { "Aktion", "s", "plan", "Doppel","prozessormaschine",
                "prozessor","maschine"};
        String[] compounds = new String[] {"Aktionsplan", "Doppelprozessormaschine"};
        String[] compoundsParts = new String[] { "Aktion", "plan", "Doppel", "prozessormaschine",
                "prozessor","maschine"};
        String[] linkingMorphemes = new String[] {"s"};

        // Check if splits and morphemes are equal
        assertThat(getAnnotation(cas.getJCas(), Compound.class), is(compounds));
        assertThat(getAnnotation(cas.getJCas(), Split.class), is(splits));
        assertThat(getAnnotation(cas.getJCas(), CompoundPart.class), is(compoundsParts));
        assertThat(getAnnotation(cas.getJCas(), LinkingMorpheme.class), is(linkingMorphemes));
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
