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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import org.junit.Ignore;
import org.junit.Test;

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
        new ThroughputTest().testAggregate(
                ThroughputTest.getStandardReader("en"),
                createEngineDescription(
                    createEngineDescription(
                            BreakIteratorSegmenter.class),
                    createEngineDescription(
                            OpenNlpPosTagger.class)
                )
        );
    }
}
