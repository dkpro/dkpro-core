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
package org.dkpro.core.castransformation;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileWriter;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.AnnotationBase;
import org.dkpro.core.castransformation.internal.AlignmentStorage;
import org.dkpro.core.io.text.TextReader;
import org.dkpro.core.io.xmi.XmiWriter;
import org.dkpro.core.testing.EOLUtils;
import org.dkpro.core.testing.dumper.CasDumpWriter;
import org.dkpro.core.tokit.BreakIteratorSegmenter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation;

public class ApplyChangesBackmapperTest
{
    public static final String TARGET_VIEW = "TargetView";

    private @TempDir File tempDir;

    @Test
    public void testBackMappingWithCachedAlignmentStateWhenRemovingTextFromTarget()
        throws Exception
    {
        boolean clearAlignmentCache = false;
        testBackMappingWhenRemovingTextFromTarget(clearAlignmentCache);
    }

    @Test
    public void testBackMappingWithoutCachedAlignmentStateWhenRemovingTextFromTarget()
        throws Exception
    {
        boolean clearAlignmentCache = true;
        testBackMappingWhenRemovingTextFromTarget(clearAlignmentCache);
    }

    private void testBackMappingWhenRemovingTextFromTarget(boolean clearAlignmentState)
        throws Exception
    {
        File output = tempDir;
        File inputFile = new File("src/test/resources/input.txt");
        File dumpFile = new File(output, "output.txt");
        String pipelineFilePath = new File(output, "pipeline.xml").getPath();

        CollectionReaderDescription reader = createReaderDescription(
                TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION,
                inputFile,
                TextReader.PARAM_LANGUAGE, "en"
        );

        AnalysisEngineDescription deletes = createEngineDescription(SofaDeleteAnnotator.class);

        AnalysisEngineDescription applyChanges = createEngineDescription(
                ApplyChangesAnnotator.class);

        AnalysisEngineDescription segmenter = createEngineDescription(BreakIteratorSegmenter.class);

        AnalysisEngineDescription clearAlignmentCache =
                createEngineDescription(ClearAlignmentCache.class);

        AnalysisEngineDescription backMapper = createEngineDescription(Backmapper.class,
                Backmapper.PARAM_CHAIN, new String[] { TARGET_VIEW, CAS.NAME_DEFAULT_SOFA });

        AnalysisEngineDescription xmiWriter = createEngineDescription(XmiWriter.class,
                XmiWriter.PARAM_TARGET_LOCATION, output);

        AnalysisEngineDescription dumpWriter = createEngineDescription(CasDumpWriter.class,
                CasDumpWriter.PARAM_TARGET_LOCATION, dumpFile);

        AggregateBuilder builder = new AggregateBuilder();
        builder.add(deletes); // Removing some lines to make sure to confuse the backmapper
        builder.add(applyChanges, ApplyChangesAnnotator.VIEW_TARGET, TARGET_VIEW,
                ApplyChangesAnnotator.VIEW_SOURCE, CAS.NAME_DEFAULT_SOFA);
        builder.add(segmenter, CAS.NAME_DEFAULT_SOFA, TARGET_VIEW);
        if (clearAlignmentState) {
            builder.add(clearAlignmentCache, CAS.NAME_DEFAULT_SOFA, TARGET_VIEW);
        }
        builder.add(backMapper);
        builder.add(xmiWriter, CAS.NAME_DEFAULT_SOFA, TARGET_VIEW);
        builder.add(dumpWriter, CAS.NAME_DEFAULT_SOFA, TARGET_VIEW);
        AnalysisEngineDescription pipeline = builder.createAggregateDescription();

        try (FileWriter writer = new FileWriter(pipelineFilePath)) {
            pipeline.toXML(writer);
        }

        SimplePipeline.runPipeline(reader, pipeline);

        String expected = FileUtils.readFileToString(
                new File("src/test/resources/output.txt"),
                "UTF-8"
        );
        String actual = FileUtils.readFileToString(dumpFile, "UTF-8");
        
        expected = EOLUtils.normalizeLineEndings(expected);
        actual = EOLUtils.normalizeLineEndings(actual);
        assertEquals(expected, actual);
    }

    public static class SofaDeleteAnnotator
        extends JCasAnnotator_ImplBase
    {
        @Override
        public void process(JCas jCas)
            throws AnalysisEngineProcessException
        {
            try {
                // Removes some "sentences" in a deterministic way. Assumes there are at least 5
                // sentences though :-)
                String text = jCas.getDocumentText();
                int previousPunctuation = -1;
                int sentenceCount = 0;
                for (int i = 0; i < text.length(); i++) {
                    if (text.charAt(i) == '.') {
                        if (sentenceCount % 5 == 0) {
                            SofaChangeAnnotation delete = new SofaChangeAnnotation(jCas);
                            delete.setOperation("delete");
                            delete.setBegin(previousPunctuation + 1);
                            delete.setEnd(i + 1);
                            delete.addToIndexes();
                        }
                        previousPunctuation = i;
                        sentenceCount++;
                    }
                }
            }
            catch (Exception e) {
                throw new AnalysisEngineProcessException(e);
            }
        }
    }

    public static class ClearAlignmentCache extends JCasAnnotator_ImplBase {

        @Override
        public void process(JCas jCas) throws AnalysisEngineProcessException {
            // Simulates a CAS restore before backmapping where the alignment cache has been
            // cleared, so that the fallback to reconstructing alignment state works in the
            // Backmapper. This is somewhat hacked, since it depends very much on inner mechanics of
            // the Backmapper and the alignment store involved, but it is much simpler compared
            // to building pipelines that store the CAS at the point before backmapping, and then
            // restore it to resume processing from that point with a complete process restart
            // between store and restore, since the alignment store is a singleton that will
            // otherwise persist and not be cleared.
            AlignmentStorage.getInstance().put(
                    jCas.getCasImpl().getBaseCAS(),
                    CAS.NAME_DEFAULT_SOFA, TARGET_VIEW,
                    null
            );
        }
    }

    @Test
    public void testBackMappingOfGeneralFeatureStructures()
        throws Exception
    {

        File inputFile = new File("src/test/resources/input.txt");
        CollectionReaderDescription reader = createReaderDescription(
                TextReader.class,
                TextReader.PARAM_SOURCE_LOCATION,
                inputFile,
                TextReader.PARAM_LANGUAGE,
                "en"
        );

        AnalysisEngineDescription applyChanges = createEngineDescription(
                ApplyChangesAnnotator.class);

        AnalysisEngineDescription fsCreator = createEngineDescription(CreateFeatureStructure.class);

        AnalysisEngineDescription backMapper = createEngineDescription(Backmapper.class,
                Backmapper.PARAM_CHAIN, new String[] { TARGET_VIEW, CAS.NAME_DEFAULT_SOFA });

        AnalysisEngineDescription assertNotYetMappedBack = createEngineDescription(
                AssertFeatureStructureCount.class,
                AssertFeatureStructureCount.PARAM_EXPECTED_COUNT,
                0
        );

        AnalysisEngineDescription assertMappedBack = createEngineDescription(
                AssertFeatureStructureCount.class,
                AssertFeatureStructureCount.PARAM_EXPECTED_COUNT,
                1
        );

        AggregateBuilder builder = new AggregateBuilder();
        builder.add(applyChanges, ApplyChangesAnnotator.VIEW_TARGET, TARGET_VIEW,
                ApplyChangesAnnotator.VIEW_SOURCE, CAS.NAME_DEFAULT_SOFA);
        builder.add(fsCreator, CAS.NAME_DEFAULT_SOFA, TARGET_VIEW);
        builder.add(assertNotYetMappedBack); // Should only exist in target view
        builder.add(backMapper);
        builder.add(assertMappedBack); // Should now be present in initial view

        AnalysisEngineDescription pipeline = builder.createAggregateDescription();

        SimplePipeline.runPipeline(reader, pipeline);
    }

    public static class CreateFeatureStructure
        extends JCasAnnotator_ImplBase
    {

        @Override
        public void process(JCas jCas)
            throws AnalysisEngineProcessException
        {
            new AnnotationBase(jCas).addToIndexes();
        }
    }

    public static class AssertFeatureStructureCount
        extends JCasAnnotator_ImplBase
    {

        public static final String PARAM_EXPECTED_COUNT = "expectedCount";

        @ConfigurationParameter(name = PARAM_EXPECTED_COUNT, mandatory = true)
        private int expectedCount;

        @Override
        public void process(JCas jCas)
            throws AnalysisEngineProcessException
        {
            int fsCount = (int) JCasUtil.select(jCas, AnnotationBase.class).stream()
                    .filter(t -> t.getClass().equals(AnnotationBase.class)).count();

            assertEquals(fsCount, expectedCount);
        }
    }
}
