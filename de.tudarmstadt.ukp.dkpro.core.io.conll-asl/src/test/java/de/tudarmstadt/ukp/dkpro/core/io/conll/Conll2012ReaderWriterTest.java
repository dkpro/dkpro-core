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
package de.tudarmstadt.ukp.dkpro.core.io.conll;

import static de.tudarmstadt.ukp.dkpro.core.testing.IOTestRunner.testRoundTrip;

import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;

public class Conll2012ReaderWriterTest
{
    @Test
    public void test()
        throws Exception
    {
        testRoundTrip(Conll2012Reader.class, Conll2012Writer.class, "conll/2012/en-orig.conll",
                Conll2012Reader.PARAM_READ_LEMMA, true);
    }
    
//    @Test
//    public void generate()
//            throws Exception
//    {
//        SimplePipeline.runPipeline(
//                createReaderDescription(TextReader.class, 
//                        TextReader.PARAM_SOURCE_LOCATION, "src/test/resources/conll/text/*.txt",
//                        TextReader.PARAM_LANGUAGE, "en"),
//                createEngineDescription(StanfordSegmenter.class),
//                createEngineDescription(StanfordLemmatizer.class),
//                createEngineDescription(StanfordPosTagger.class),
//                createEngineDescription(StanfordParser.class),
//                createEngineDescription(StanfordCoreferenceResolver.class),
//                createEngineDescription(StanfordNamedEntityRecognizer.class),
//                createEngineDescription(Conll2012Writer.class,
//                        Conll2012Writer.PARAM_TARGET_LOCATION, testContext.getTestOutputFolderName()));
//    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
