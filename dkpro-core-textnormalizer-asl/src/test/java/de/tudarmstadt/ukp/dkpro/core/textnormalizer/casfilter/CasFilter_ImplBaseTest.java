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
package de.tudarmstadt.ukp.dkpro.core.textnormalizer.casfilter;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.dumper.CasDumpWriter;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.io.text.StringReader;

public class CasFilter_ImplBaseTest
{
    @Test
    public void testAnnotationFilterPass()
        throws UIMAException, IOException
    {
        File tmpFile = new File(testContext.getTestOutputFolder(), "output.dump");
        
        String input = "test";
        String expectedFirstLine = "======== CAS 0 begin ==================================";

        CollectionReaderDescription reader = createReaderDescription(
                StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, input,
                StringReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription filter = createEngineDescription(AnnotationBasedFilter.class);
        AnalysisEngineDescription annotator = createEngineDescription(TestAnnotator.class);
        AnalysisEngineDescription writer = createEngineDescription(
                CasDumpWriter.class,
                CasDumpWriter.PARAM_TARGET_LOCATION, tmpFile);
        AnalysisEngineDescription aggregator = CasFilter_ImplBase
                .createAggregateBuilderDescription(filter, writer);

        SimplePipeline.runPipeline(reader, annotator, aggregator);

        List<String> output = FileUtils.readLines(tmpFile);
        assertEquals(expectedFirstLine, output.get(0));
        assertEquals(input, output.get(13));
        assertEquals("Sentence", output.get(15));
    }

    @Test
    public void testAnnotationFilterRemove()
        throws UIMAException, IOException
    {
        File tmpFile = new File(testContext.getTestOutputFolder(), "output.dump");
        
        String input = "";

        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, input,
                StringReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription filter = createEngineDescription(AnnotationBasedFilter.class);
        AnalysisEngineDescription annotator = createEngineDescription(TestAnnotator.class);
        AnalysisEngineDescription writer = createEngineDescription(CasDumpWriter.class,
                CasDumpWriter.PARAM_TARGET_LOCATION, tmpFile);
        AnalysisEngineDescription aggregator = CasFilter_ImplBase
                .createAggregateBuilderDescription(filter, writer);

        SimplePipeline.runPipeline(reader, annotator, aggregator);
        assertTrue(FileUtils.readFileToString(tmpFile).isEmpty());
    }

    @Test
    public void testEmptyDocumentFilterRemove()
        throws UIMAException, IOException
    {
        File tmpFile = new File(testContext.getTestOutputFolder(), "output.dump");
        
        String input = "";

        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, input,
                StringReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription filter = createEngineDescription(EmptyDocumentFilter.class);
        AnalysisEngineDescription writer = createEngineDescription(CasDumpWriter.class,
                CasDumpWriter.PARAM_TARGET_LOCATION, tmpFile);
        AnalysisEngineDescription aggregator = CasFilter_ImplBase
                .createAggregateBuilderDescription(filter, writer);

        SimplePipeline.runPipeline(reader, aggregator);
        assertTrue(FileUtils.readFileToString(tmpFile).isEmpty());
    }

    @Test
    public void testEmptyDocumentFilterPass()
        throws UIMAException, IOException
    {
        File tmpFile = new File(testContext.getTestOutputFolder(), "output.dump");

        String input = "test";

        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, input,
                StringReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription filter = createEngineDescription(EmptyDocumentFilter.class);
        AnalysisEngineDescription writer = createEngineDescription(CasDumpWriter.class,
                CasDumpWriter.PARAM_TARGET_LOCATION, tmpFile);
        AnalysisEngineDescription aggregator = CasFilter_ImplBase
                .createAggregateBuilderDescription(filter, writer);

        SimplePipeline.runPipeline(reader, aggregator);
        assertFalse(FileUtils.readFileToString(tmpFile).isEmpty());
    }

    @Test
    public void testLanguageFilterPass()
        throws UIMAException, IOException
    {
        File tmpFile = new File(testContext.getTestOutputFolder(), "output.dump");
        
        String input = "test";

        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, input,
                StringReader.PARAM_LANGUAGE, "en");
        AnalysisEngineDescription filter = createEngineDescription(LanguageFilter.class,
                LanguageFilter.PARAM_REQUIRED_LANGUAGES, new String[] { "de", "en" });
        AnalysisEngineDescription writer = createEngineDescription(CasDumpWriter.class,
                CasDumpWriter.PARAM_TARGET_LOCATION, tmpFile);
        AnalysisEngineDescription aggregator = CasFilter_ImplBase
                .createAggregateBuilderDescription(filter, writer);

        SimplePipeline.runPipeline(reader, aggregator);
        assertFalse(FileUtils.readFileToString(tmpFile).isEmpty());
    }

    @Test
    public void testLanguageFilterRemove()
        throws UIMAException, IOException
    {
        File tmpFile = new File(testContext.getTestOutputFolder(), "output.dump");
        
        String input = "test";

        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, input,
                StringReader.PARAM_LANGUAGE, "ch");
        AnalysisEngineDescription filter = createEngineDescription(LanguageFilter.class,
                LanguageFilter.PARAM_REQUIRED_LANGUAGES, new String[] { "de", "en" });
        AnalysisEngineDescription writer = createEngineDescription(CasDumpWriter.class,
                CasDumpWriter.PARAM_TARGET_LOCATION, tmpFile);
        AnalysisEngineDescription aggregator = CasFilter_ImplBase
                .createAggregateBuilderDescription(filter, writer);

        SimplePipeline.runPipeline(reader, aggregator);
        assertTrue(FileUtils.readFileToString(tmpFile).isEmpty());
    }

    public static class TestAnnotator
        extends JCasAnnotator_ImplBase
    {
        /**
         * Create one sentence over the full text unless the text is empty.
         *
         * @param aJCas
         * @throws AnalysisEngineProcessException
         */
        @Override
        public void process(JCas aJCas)
            throws AnalysisEngineProcessException
        {
            String text = aJCas.getDocumentText();
            if (text.length() > 0) {
                Sentence sentence = new Sentence(aJCas);
                sentence.setBegin(0);
                sentence.setEnd(text.length());
                sentence.addToIndexes(aJCas);
            }
        }
    }

    public static class AnnotationBasedFilter
        extends CasFilter_ImplBase
    {
        /**
         * filter out documents that do not contain any sentence annotation.
         */
        @Override
        protected boolean pass(JCas aJCas)
        {
            return select(aJCas, Sentence.class).size() > 0;
        }
    }

    public static class EmptyDocumentFilter
        extends CasFilter_ImplBase
    {
        @Override
        protected boolean pass(JCas aJCas)
        {
            return aJCas.getDocumentText().length() > 0;
        }
    }

    public static class LanguageFilter
        extends CasFilter_ImplBase
    {
        public static final String PARAM_REQUIRED_LANGUAGES = "requiredLanguages";
        @ConfigurationParameter(name = PARAM_REQUIRED_LANGUAGES, mandatory = true)
        Set<String> requiredLanguages;

        @Override
        protected boolean pass(JCas aJCas)
        {
            return requiredLanguages.contains(aJCas.getDocumentLanguage());
        }
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
