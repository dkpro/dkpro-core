/*
 * Copyright 2011
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universit√§t Darmstadt
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
package org.dkpro.core.io.bnc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

import java.io.File;
import java.io.IOException;

import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.ReaderAssert;
import org.dkpro.core.testing.WriterAssert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class BncReaderTest
{
    @Before
    public void setUp() throws IOException {
        DkproTestContext.get().initializeTestWorkspace();
    }
    
    @Test
    public void test() throws Exception
    {   
        ReaderAssert
            .assertThat(BncReader.class,
                  BncReader.PARAM_LANGUAGE, "en")
            .readingFrom("src/test/resources/FX8.xml")
            .usingWriter(WriterAssert.simpleJCasDumper(new File("FX8.xml")))
            .asFiles()
            .allSatisfy(file -> {
                assertThat(contentOf(file)).isEqualToNormalizingNewlines(
                        contentOf(new File("src/test/resources/", 
                                file.getName() + ".dump")));
            })
            .extracting(File::getName)
            .containsExactlyInAnyOrder("FX8.xml");
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
