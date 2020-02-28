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
package org.dkpro.core.io.conll;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;
import static org.dkpro.core.testing.IOTestRunner.testRoundTrip;

import java.io.File;
import java.io.IOException;

import org.dkpro.core.io.conll.Conll2002Reader.ColumnSeparators;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.ReaderAssert;
import org.dkpro.core.testing.dumper.CasDumpWriter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class Conll2002ReaderWriterTest
{
    @Before
    public void setUp() throws IOException {
        DkproTestContext.get().initializeTestWorkspace();
    }
    
    @Test
    public void roundTrip()
        throws Exception
    {
        testRoundTrip(Conll2002Reader.class, Conll2002Writer.class,
                "conll/2002/ner2002_test.conll");
    }

    @Test
    public void testGermeval2014()
        throws Exception
    {
        ReaderAssert
        .assertThat(Conll2002Reader.class,
                Conll2002Reader.PARAM_LANGUAGE, "de", 
                Conll2002Reader.PARAM_HAS_HEADER, true, 
                Conll2002Reader.PARAM_HAS_TOKEN_NUMBER, true, 
                Conll2002Reader.PARAM_COLUMN_SEPARATOR, ColumnSeparators.TAB.getName(),
                Conll2002Reader.PARAM_HAS_EMBEDDED_NAMED_ENTITY, true)
        .readingFrom("src/test/resources/conll/2002/germeval2014_test.conll")
        .usingWriter(CasDumpWriter.class, 
                CasDumpWriter.PARAM_TARGET_LOCATION, DkproTestContext.get().getTestOutputFile(new File("germeval2014_test.conll.out")),
                CasDumpWriter.PARAM_SORT, true)
        .asFiles()
        .allSatisfy(file -> {
            if (file.getName().endsWith(".conll")) {
                assertThat(contentOf(file)).isEqualToNormalizingNewlines(
                        contentOf(new File("src/test/resources/conll/2002/", 
                                file.getName()+".out")));
            }
        })
        .extracting(File::getName)
        .containsExactlyInAnyOrder("germeval2014_test.conll.out");
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
