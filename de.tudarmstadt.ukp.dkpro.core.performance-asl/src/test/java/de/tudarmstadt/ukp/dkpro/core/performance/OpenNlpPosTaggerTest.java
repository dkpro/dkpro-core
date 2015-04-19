/*******************************************************************************
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.performance;

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.INCLUDE_PREFIX;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReader;

import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.io.tei.TeiReader;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

/**
 * @author zesch
 *
 */
public class OpenNlpPosTaggerTest
{
	@Ignore
    @Test
    public void performanceTest()
        throws Exception
    {
    	SimplePipeline.runPipeline(
			createReader(
	                TeiReader.class,
	                TeiReader.PARAM_LANGUAGE, "en",
	                TeiReader.PARAM_SOURCE_LOCATION, "src/test/resources/corpus/",
	                TeiReader.PARAM_PATTERNS, new String[] {INCLUDE_PREFIX + "*.xml"}
	        ),
	        createEngineDescription(
	        		createEngineDescription(
	                        Stopwatch.class,
	                        Stopwatch.PARAM_TIMER_NAME, "testTimer"
	                ),
	                createEngineDescription(
	                        BreakIteratorSegmenter.class),
	                createEngineDescription(
	                        OpenNlpPosTagger.class),
	                createEngineDescription(
	                        Stopwatch.class,
	                        Stopwatch.PARAM_TIMER_NAME, "testTimer",
	                        Stopwatch.PARAM_OUTPUT_FILE, "target/result.txt"
	                )
	        )
        );
    }
}
