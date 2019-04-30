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
package org.dkpro.core.illinoisnlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.dkpro.core.testing.harness.SegmenterHarness;
import org.junit.Test;

public class IllinoisStatefulSegmenterTest
{
    @Test
    public void runHarness()
        throws Throwable
    {
        AnalysisEngineDescription aed = createEngineDescription(IllinoisStatefulSegmenter.class);

        SegmenterHarness.run(aed, "de.4", "en.1", "en.9", "ar.1", "zh.1", "zh.2");
    }
}
