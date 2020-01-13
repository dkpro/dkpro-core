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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;
import static org.dkpro.core.testing.IOTestRunner.testOneWay2;

import java.io.File;
import java.io.IOException;

import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.io.imscwb.ImsCwbReader;
import org.dkpro.core.io.imscwb.ImsCwbWriter;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.ReaderAssert;
import org.dkpro.core.testing.assertions.AssertFile;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ImsCwbReaderWriterTest
{    
    @Before
    public void setUp() throws IOException {
        DkproTestContext.get().initializeTestWorkspace();
    }    
    
    @Test
    public void testTuebadz()
        throws Exception
    {
        ReaderAssert
        .assertThat(ImsCwbReader.class)
        .readingFrom("src/test/resources/tuebadz/corpus-sample-ref.txt")
        .usingWriter(ImsCwbWriter.class,
              ComponentParameters.PARAM_TARGET_LOCATION,
                  new File(testContext.getTestOutputFolder(), "corpus-sample-ref.txt"),
                  ImsCwbReader.PARAM_LANGUAGE, "de",
                  ImsCwbReader.PARAM_POS_TAG_SET, "stts",
                  JCasFileWriter_ImplBase.PARAM_SINGULAR_TARGET, true)
        .asFiles()
        .allSatisfy(file -> {
            if (!file.getName().endsWith(".conf")) {
                assertThat(contentOf(file)).isEqualToNormalizingNewlines(
                        contentOf(new File("src/test/resources/tuebadz/", 
                                file.getName())));
            }
        })
        .extracting(File::getName)
        .containsExactlyInAnyOrder("corpus-sample-ref.txt");
    }

    @Test
    public void testWacky()
        throws Exception
    {
        ReaderAssert
        .assertThat(ImsCwbReader.class)
        .readingFrom("src/test/resources/wacky/test.txt")
        .usingWriter(ImsCwbWriter.class,
              ComponentParameters.PARAM_TARGET_LOCATION,
                  new File(testContext.getTestOutputFolder(), "test-ref.txt"),
                  ImsCwbReader.PARAM_SOURCE_ENCODING, "iso8859-1",
                  JCasFileWriter_ImplBase.PARAM_SINGULAR_TARGET, true)
        .asFiles()
        .allSatisfy(file -> {
            if (!file.getName().endsWith(".conf")) {
                assertThat(contentOf(file)).isEqualToNormalizingNewlines(
                        contentOf(new File("src/test/resources/wacky/", 
                                file.getName())));
            }
        })
        .extracting(File::getName)
        .containsExactlyInAnyOrder("corpus-sample-ref.txt");        
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
