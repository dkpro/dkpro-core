/**
 * Copyright 2007-2014
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.tudarmstadt.ukp.dkpro.core.corenlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.harness.SegmenterHarness;

public
class CoreNlpSegmenterTest
{
	@Test
	public void run() throws Throwable
	{
		AnalysisEngineDescription aed = createEngineDescription(CoreNlpSegmenter.class);

        SegmenterHarness.run(aed, "de.4", "en.9", "ar.1", "zh.1", "zh.2");
	}
	
    @Test
    public void testZoning() throws Exception
    {
        SegmenterHarness.testZoning(CoreNlpSegmenter.class);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
