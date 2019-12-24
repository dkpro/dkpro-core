/*
 * Copyright 2014
 * Ubiquitous Knowledge Processing (UKP) Lab and FG Language Technology
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.imscwb;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;
import static org.dkpro.core.testing.IOTestRunner.testOneWay;
import static org.dkpro.core.testing.IOTestRunner.testRoundTrip;

import java.io.File;

import org.dkpro.core.testing.DkproTestContext;
import org.junit.Rule;
import org.junit.Test;

public class ImsCwbReaderWriterTest
{
    @Test
    public void testTuebadz()
        throws Exception
    {
        testRoundTrip(
                createReaderDescription(ImsCwbReader.class,
                        ImsCwbReader.PARAM_LANGUAGE, "de",
                        ImsCwbReader.PARAM_POS_TAG_SET, "stts"), 
                createEngineDescription(ImsCwbWriter.class,
                        ImsCwbWriter.PARAM_TARGET_LOCATION,
                            new File(testContext.getTestOutputFolder(), "corpus-sample-ref.txt"),
                        ImsCwbWriter.PARAM_SINGULAR_TARGET, true), 
                "tuebadz/corpus-sample-ref.txt");
    }

    @Test
    public void testWacky()
        throws Exception
    {
        testOneWay(
                createReaderDescription(ImsCwbReader.class,
                        ImsCwbReader.PARAM_LANGUAGE, "de",
                        ImsCwbReader.PARAM_POS_TAG_SET, "stts",
                        ImsCwbReader.PARAM_SOURCE_ENCODING, "iso8859-1"), 
                createEngineDescription(ImsCwbWriter.class,
                        ImsCwbWriter.PARAM_TARGET_LOCATION,
                            new File(testContext.getTestOutputFolder(), "test.txt"),
                        ImsCwbWriter.PARAM_SINGULAR_TARGET, true),
                "wacky/test-ref.txt",
                "wacky/test.txt");
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
