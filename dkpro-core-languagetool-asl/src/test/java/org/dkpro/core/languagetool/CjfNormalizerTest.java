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
package org.dkpro.core.languagetool;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.dkpro.core.testing.AssertAnnotations.assertTransformedText;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.dkpro.core.languagetool.CjfNormalizer.Direction;
import org.dkpro.core.testing.DkproTestContext;
import org.junit.Rule;
import org.junit.Test;

public class CjfNormalizerTest
{
    @Test
    public void testToSimplified()
        throws Exception
    {
        runTest("毛澤東住在北京", "毛泽东住在北京", CjfNormalizer.Direction.TO_SIMPLIFIED);
    }

    @Test
    public void testToTraditional()
        throws Exception
    {
        runTest("毛泽东住在北京", "毛澤東住在北京", CjfNormalizer.Direction.TO_TRADITIONAL);
    }

    public void runTest(String inputText, String normalizedText, Direction aDirection)
        throws Exception
    {
        AnalysisEngineDescription normalizer = createEngineDescription(CjfNormalizer.class,
                CjfNormalizer.PARAM_DIRECTION, aDirection);

        assertTransformedText(normalizedText, inputText, "zh", normalizer);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
