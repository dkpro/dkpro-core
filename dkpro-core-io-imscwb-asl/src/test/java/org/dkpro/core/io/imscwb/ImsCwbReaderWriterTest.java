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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Files.contentOf;

import java.io.File;

import org.dkpro.core.testing.ReaderAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ImsCwbReaderWriterTest
{
    @Test
    public void thatRoundTripWithTuebaDzWorks(@TempDir File tempDir)
        throws Exception
    {
        ReaderAssert.assertThat(//
                        ImsCwbReader.class,//
                        ImsCwbReader.PARAM_SOURCE_LOCATION, 
                                "src/test/resources/tuebadz/corpus-sample-ref.txt",//
                        ImsCwbReader.PARAM_POS_TAG_SET, "stts",//
                        ImsCwbReader.PARAM_LANGUAGE, "de")//
                .usingWriter(//
                        ImsCwbWriter.class)//
                .writingToSingular(new File(tempDir, "/corpus-sample-ref.txt").toString())//
                .outputAsString()//
                .isEqualToNormalizingNewlines(contentOf(
                        new File("src/test/resources/tuebadz/corpus-sample-ref.txt"), UTF_8));
    }

    @Test
    public void thatRoundTripWithMultipleInputsWorks(@TempDir File tempDir)
        throws Exception
    {
        ReaderAssert.assertThat( //
                        ImsCwbReader.class,//
                        ImsCwbReader.PARAM_SOURCE_LOCATION, 
                                "src/test/resources/multiple/*.vrt")//
                .usingWriter(//
                        ImsCwbWriter.class)//
                .writingTo(tempDir)//
                .keepOriginalExtension()//
                .asFiles()//
                .allSatisfy(file -> assertThat(contentOf(file, UTF_8)).isEqualToNormalizingNewlines(
                        contentOf(new File("src/test/resources/multiple", file.getName()), UTF_8)));
    }

    @Test
    public void thatOneWayWithWackyWorks(@TempDir File tempDir) throws Exception
    {
        ReaderAssert.assertThat(//
                        ImsCwbReader.class,//
                        ImsCwbReader.PARAM_SOURCE_LOCATION, 
                                "src/test/resources/wacky/test.txt",//
                        ImsCwbReader.PARAM_POS_TAG_SET, "stts",//
                        ImsCwbReader.PARAM_LANGUAGE, "de",//
                        ImsCwbReader.PARAM_SOURCE_ENCODING, "iso8859-1")//
                .usingWriter(//
                        ImsCwbWriter.class)//
                .writingToSingular(new File(tempDir, "/test.txt").toString())//
                .outputAsString()//
                .isEqualToNormalizingNewlines(contentOf(//
                        new File("src/test/resources/wacky/test-ref.txt"), UTF_8));
    }
}
