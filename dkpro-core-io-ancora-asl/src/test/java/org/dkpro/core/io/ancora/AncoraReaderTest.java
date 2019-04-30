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
package org.dkpro.core.io.ancora;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.dkpro.core.testing.IOTestRunner.testOneWay;

import org.dkpro.core.io.ancora.AncoraReader;
import org.dkpro.core.io.conll.Conll2006Writer;
import org.dkpro.core.testing.DkproTestContext;
import org.junit.Rule;
import org.junit.Test;

public class AncoraReaderTest
{
    @Test
    public void test() throws Exception
    {
        testOneWay(
                createReaderDescription(AncoraReader.class,
                        AncoraReader.PARAM_SPLIT_MULTI_WORD_TOKENS ,false),
                createEngineDescription(Conll2006Writer.class),
                "ancora/19004_20000725.tbf.conll", // the reference file for the output
                "ancora/19004_20000725.tbf.xml"); // the input file for the test
    }

    @Test
    public void testWithMultiWordSplitting() throws Exception
    {
        testOneWay(
                createReaderDescription(AncoraReader.class,
                        AncoraReader.PARAM_SPLIT_MULTI_WORD_TOKENS, true),
                createEngineDescription(Conll2006Writer.class),
                "ancora/19004_20000725.tbf-split.conll", // the reference file for the output
                "ancora/19004_20000725.tbf.xml"); // the input file for the test
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
