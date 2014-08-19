/*******************************************************************************
 * Copyright 2013
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
package de.tudarmstadt.ukp.dkpro.core.frequency.tfidf;

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX;
import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION;
import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.PARAM_PATTERNS;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.model.DfModel;
import de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.util.TfidfUtils;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

/**
 * Unit test of {@link TfidfConsumer} and {@link TfidfAnnotator}.
 * 
 * @author zesch, parzonka
 * 
 */
public class TfidfConsumerTest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void rawScoresTest()
        throws Exception
    {
        File target = folder.newFile(name.getMethodName());
        
        CollectionReaderDescription reader = createReaderDescription(TextReader.class, 
                PARAM_SOURCE_LOCATION, "src/test/resources/consumer/", 
                PARAM_PATTERNS, INCLUDE_PREFIX + "*.txt");

        AnalysisEngineDescription aggregate = createEngineDescription(
                createEngineDescription(BreakIteratorSegmenter.class),
                createEngineDescription(TfidfConsumer.class, 
                        TfidfConsumer.PARAM_FEATURE_PATH, Token.class.getName(),
                        TfidfConsumer.PARAM_TARGET_LOCATION, target));

        // now create the tf and df files
        SimplePipeline.runPipeline(reader, aggregate);

        // check whether they were really created and contain the correct values
        DfModel dfModel = TfidfUtils.getDfModel(target.getPath());

        assertEquals(2, dfModel.getDf("example"));
        assertEquals(2, dfModel.getDf("sentence"));
        assertEquals(1, dfModel.getDf("funny"));
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}
