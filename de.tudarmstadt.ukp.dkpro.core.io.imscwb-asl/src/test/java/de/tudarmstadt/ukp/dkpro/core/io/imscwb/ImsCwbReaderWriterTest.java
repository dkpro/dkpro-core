/*******************************************************************************
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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.io.imscwb;

import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testOneWay2;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class ImsCwbReaderWriterTest
{
    @Test
    public void testTuebadz()
        throws Exception
    {
        testOneWay2(ImsCwbReader.class, ImsCwbWriter.class, "tuebadz/corpus-sample-ref.txt",
                "corpus-sample-ref.txt", "tuebadz/corpus-sample-ref.txt",
                ComponentParameters.PARAM_TARGET_LOCATION, new File("target/test-output/"
                        + DkproTestContext.get().getTestOutputFolderName(), "corpus-sample-ref.txt"),
                ImsCwbReader.PARAM_LANGUAGE, "de",
                ImsCwbReader.PARAM_POS_TAG_SET, "stts");
    }

    @Test
    public void testWacky()
        throws Exception
    {
        testOneWay2(ImsCwbReader.class, ImsCwbWriter.class, "wacky/test-ref.txt",
                "test.txt", "wacky/test.txt",
                ComponentParameters.PARAM_TARGET_LOCATION, new File("target/test-output/"
                        + DkproTestContext.get().getTestOutputFolderName(), "test.txt"),
                ImsCwbReader.PARAM_SOURCE_ENCODING, "iso8859-1");
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
