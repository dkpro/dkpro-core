/*
 * Copyright 2007-2018
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
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.dkpro.core.stanfordnlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.dkpro.core.testing.AssertAnnotations.assertTransformedText;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.junit.Test;

public class StanfordPtbTransformerTest
{
    @Test
    public void test()
        throws Exception
    {
        String expected = "``Hey you!'', John said.";
        String input = "\"Hey you!\", John said.";

        AnalysisEngineDescription normalizer = createEngineDescription(
                StanfordPtbTransformer.class);

        assertTransformedText(expected, input, "en", normalizer);
    }
}
