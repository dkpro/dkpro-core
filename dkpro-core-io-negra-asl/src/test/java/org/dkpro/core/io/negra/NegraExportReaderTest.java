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
package org.dkpro.core.io.negra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;

import java.io.File;
import java.io.IOException;

import org.dkpro.core.io.negra.NegraExportReader;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.ReaderAssert;
import org.dkpro.core.testing.WriterAssert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Sample is taken from
 * http://www.coli.uni-saarland.de/projects/sfb378/negra-corpus
 * /corpus-sample.export Only the second sentence is used.
 *
 */
public class NegraExportReaderTest
{
    @Before
    public void setUp() throws IOException {
        DkproTestContext.get().initializeTestWorkspace();
    }    
    
    @Test
    public void negraTest()
        throws Exception
    {
        ReaderAssert
        .assertThat(NegraExportReader.class,
              NegraExportReader.PARAM_LANGUAGE, "de",
              NegraExportReader.PARAM_SOURCE_ENCODING, "UTF-8",
              NegraExportReader.PARAM_READ_PENN_TREE, true)
        .readingFrom("src/test/resources/sentence.export")
        .usingWriter(WriterAssert.simpleJCasDumper(new File("sentence.export")))
        .asFiles()
        .allSatisfy(file -> {
            assertThat(contentOf(file)).isEqualToNormalizingNewlines(
                    contentOf(new File("src/test/resources/", 
                            file.getName()+".dump")));
        })
        .extracting(File::getName)
        .containsExactlyInAnyOrder("sentence.export");
    }

    @Test
    public void negraTigerTest()
        throws Exception
    {
        ReaderAssert
        .assertThat(NegraExportReader.class,
              NegraExportReader.PARAM_LANGUAGE, "de",
              NegraExportReader.PARAM_SOURCE_ENCODING, "ISO-8859-15",
              NegraExportReader.PARAM_READ_PENN_TREE, true)
        .readingFrom("src/test/resources/tiger-sample.export")
        .usingWriter(WriterAssert.simpleJCasDumper(new File("tiger-sample.export")))
        .asFiles()
        .allSatisfy(file -> {
            assertThat(contentOf(file)).isEqualToNormalizingNewlines(
                    contentOf(new File("src/test/resources/", 
                            file.getName()+".dump")));
        })
        .extracting(File::getName)
        .containsExactlyInAnyOrder("tiger-sample.export");        
    }

    @Test
    public void tuebaTest()
        throws Exception
    {
        ReaderAssert
        .assertThat(NegraExportReader.class,
              NegraExportReader.PARAM_LANGUAGE, "de",
              NegraExportReader.PARAM_SOURCE_ENCODING, "UTF-8",
              NegraExportReader.PARAM_READ_PENN_TREE, true)
        .readingFrom("src/test/resources/tueba-sample.export")
        .usingWriter(WriterAssert.simpleJCasDumper(new File("tueba-sample.export")))
        .asFiles()
        .allSatisfy(file -> {
            assertThat(contentOf(file)).isEqualToNormalizingNewlines(
                    contentOf(new File("src/test/resources/", 
                            file.getName()+".dump")));
        })
        .extracting(File::getName)
        .containsExactlyInAnyOrder("tueba-sample.export");        
    }

    @Test
    public void testFormat4WithCoref()
        throws Exception
    {
                ReaderAssert
                .assertThat(NegraExportReader.class,
                      NegraExportReader.PARAM_LANGUAGE, "de",
                      NegraExportReader.PARAM_SOURCE_ENCODING, "UTF-8",
                      NegraExportReader.PARAM_READ_PENN_TREE, true)
                .readingFrom("src/test/resources/format4-with-coref-sample.export")
                .usingWriter(WriterAssert.simpleJCasDumper(new File("format4-with-coref-sample.export")))
                .asFiles()
                .allSatisfy(file -> {
                    assertThat(contentOf(file)).isEqualToNormalizingNewlines(
                            contentOf(new File("src/test/resources/", 
                                    file.getName()+".dump")));
                })
                .extracting(File::getName)
                .containsExactlyInAnyOrder("format4-with-coref-sample.export");        
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
