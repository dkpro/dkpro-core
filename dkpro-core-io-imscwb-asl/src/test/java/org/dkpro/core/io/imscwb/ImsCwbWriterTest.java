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
package org.dkpro.core.io.imscwb;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.assertj.core.util.Files.contentOf;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.dkpro.core.io.negra.NegraExportReader;
import org.dkpro.core.opennlp.OpenNlpPosTagger;
import org.dkpro.core.snowball.SnowballStemmer;
import org.dkpro.core.testing.CollectionReaderAssert;
import org.dkpro.core.testing.DkproTestContext;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class ImsCwbWriterTest
{
    @Test
    public void thatWritingTuebaDzSampleWorks()
        throws Exception
    {
        CollectionReaderAssert.assertThat(
                        NegraExportReader.class,
                        NegraExportReader.PARAM_SOURCE_LOCATION, 
                                "src/test/resources/tuebadz/corpus-sample.export",
                        NegraExportReader.PARAM_LANGUAGE, "de",
                        NegraExportReader.PARAM_SOURCE_ENCODING, "UTF-8")
                .usingEngines(
                        createEngineDescription(OpenNlpPosTagger.class))
                .usingWriter(
                        ImsCwbWriter.class,
                        ImsCwbWriter.PARAM_TARGET_ENCODING, "UTF-8")
                .writingToSingular("${TARGET}/corpus-sample.vrt")
                .asString()
                .isEqualToNormalizingNewlines(contentOf(
                        new File("src/test/resources/tuebadz/corpus-sample-ref.txt"), UTF_8));
    }
    
    @Test
    public void thatWritingTuebaDzSampleWithAdditionalFeaturesWorks()
        throws Exception
    {
        CollectionReaderAssert.assertThat(
                        NegraExportReader.class,
                        NegraExportReader.PARAM_SOURCE_LOCATION, 
                                "src/test/resources/tuebadz/corpus-sample.export",
                        NegraExportReader.PARAM_LANGUAGE, "de",
                        NegraExportReader.PARAM_SOURCE_ENCODING, "UTF-8")
                .usingEngines(
                        createEngineDescription(OpenNlpPosTagger.class),
                        createEngineDescription(SnowballStemmer.class))
                .usingWriter(
                        ImsCwbWriter.class,
                        ImsCwbWriter.PARAM_TARGET_LOCATION, "${TARGET}/corpus-sample-addfeat.vrt",
                        ImsCwbWriter.PARAM_SINGULAR_TARGET, true,
                        ImsCwbWriter.PARAM_WRITE_CPOS, true,
                        ImsCwbWriter.PARAM_ADDITIONAL_FEATURES, new String[] { 
                                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem/value" })
                .asString()
                .isEqualToNormalizingNewlines(contentOf(
                        new File("src/test/resources/tuebadz/corpus-sample-addfeat-ref.txt"),
                        UTF_8));
    }    
    
    @Ignore("This test cannot work (yet) because we do not ship the cwb-encode and cwb-makeall binaries")
    @Test
    public void test2()
        throws Exception
    {
        CollectionReader ner = createReader(
                NegraExportReader.class,
                NegraExportReader.PARAM_SOURCE_LOCATION, "src/test/resources/corpus-sample.export",
                NegraExportReader.PARAM_LANGUAGE, "de",
                NegraExportReader.PARAM_SOURCE_ENCODING, "UTF-8");

        AnalysisEngineDescription tag = createEngineDescription(
                OpenNlpPosTagger.class);

        AnalysisEngineDescription tw = createEngineDescription(
                ImsCwbWriter.class,
                ImsCwbWriter.PARAM_TARGET_LOCATION, "target/cqbformat",
                ImsCwbWriter.PARAM_TARGET_ENCODING, "UTF-8",
                ImsCwbWriter.PARAM_CQP_HOME, "/Users/bluefire/bin/cwb-2.2.b99");

        runPipeline(ner, tag, tw);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
