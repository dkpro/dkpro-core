/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.dkpro.core.io.xmi;

import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testRoundTrip;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class XmiReaderWriterTest
{
    @Test
    public void testRoundtrip() throws Exception {
        testRoundTrip(
                XmiReader.class, 
                XmiWriter.class,
                "xmi/english.xmi");
    }

//    @Test
//    public void generate()
//            throws Exception
//    {
//        SimplePipeline.runPipeline(
//                createReaderDescription(TextReader.class, 
//                        TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/texts/english.txt",
//                        TextReader.PARAM_LANGUAGE, "en"),
//                createEngineDescription(StanfordSegmenter.class),
//                createEngineDescription(StanfordPosTagger.class),
//                createEngineDescription(StanfordLemmatizer.class),
//                createEngineDescription(StanfordParser.class, 
//                        StanfordParser.PARAM_WRITE_CONSTITUENT, false,
//                        StanfordParser.PARAM_WRITE_DEPENDENCY, true),
//                createEngineDescription(StanfordNamedEntityRecognizer.class),
//                createEngineDescription(ClearNlpSemanticRoleLabeler.class),
//                createEngineDescription(TagsetDescriptionStripper.class),
//                createEngineDescription(XmiWriter.class,
//                        XmiWriter.PARAM_STRIP_EXTENSION, true,
//                        XmiWriter.PARAM_TARGET_LOCATION, "target/test-output/"+
//                        testContext.getTestOutputFolderName()));
//    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
