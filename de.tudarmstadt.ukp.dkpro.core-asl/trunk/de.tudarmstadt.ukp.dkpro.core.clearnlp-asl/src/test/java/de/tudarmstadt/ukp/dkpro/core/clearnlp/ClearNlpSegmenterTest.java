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
package de.tudarmstadt.ukp.dkpro.core.clearnlp;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.testing.harness.SegmenterHarness;

public class ClearNlpSegmenterTest
{
	@Test
	public void run() throws Throwable
	{
		AnalysisEngineDescription aed = createPrimitiveDescription(ClearNlpSegmenter.class);
		
		SegmenterHarness.run(aed, "de.1", "de.2", "de.3", "de.4", "en.1", "en.7", "en.8", "en.9",
				"ar.1", "zh.1", "zh.2");
	}
}
