/*******************************************************************************
 * Copyright 2012
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
 *******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.commonscodec;

import static de.tudarmstadt.ukp.dkpro.core.commonscodec.PhoneticTranscriptorTestUtil.runTest;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;

import org.junit.Test;

public class SoundexPhoneticTranscriptorTest
{

    @Test
    public void soundexTest() throws Exception {
  
        runTest(
                createPrimitiveDescription(SoundexPhoneticTranscriptor.class),
                "The knight entered the store in the night .",
                "T000", "K523", "E536", "T000", "S360", "I500", "T000", "N230", ""
        );
    }
}
