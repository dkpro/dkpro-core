/*
 * Copyright 2012
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.contentOf;

import java.io.File;

import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.ReaderAssert;
import org.junit.Rule;
import org.junit.Test;

//NOTE: This file contains Asciidoc markers for partial inclusion of this file in the documentation
//Do not remove these tags!
public class Conll2006ReaderWriterTest
{
    @Test
    public void roundTrip()
        throws Exception
    {
// tag::testRoundTrip[]
        ReaderAssert.assertThat(Conll2006Reader.class)                 // the reader
            .readingFrom("src/test/resources/conll/2006/fi-ref.conll") // the test input file
            .usingWriter(Conll2006Writer.class)                        // the writer
            .outputAsString()                                          // access writer output
            .isEqualToNormalizingNewlines(                             // compare to input file
                contentOf(new File("src/test/resources/conll/2006/fi-ref.conll"), UTF_8));
// end::testRoundTrip[]
    }

    @Test
    public void testFinnTreeBank()
        throws Exception
    {
// tag::testOneWay[]
        ReaderAssert.assertThat(Conll2006Reader.class,                  // the reader
                Conll2006Reader.PARAM_SOURCE_ENCODING, "UTF-8")         // reader parameter
            .readingFrom("src/test/resources/conll/2006/fi-orig.conll") // the test input file
            .usingWriter(Conll2006Writer.class,                         // the writer
                Conll2006Writer.PARAM_TARGET_ENCODING, "UTF-8")         // writer parameter
            .outputAsString("fi-orig.conll")                            // access writer output
            .isEqualToNormalizingNewlines(                              // compare to input file
                contentOf(new File("src/test/resources/conll/2006/fi-ref.conll"), UTF_8));
// end::testOneWay[]
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
