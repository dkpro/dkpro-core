/*******************************************************************************
 * Copyright 2013
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

import static org.junit.Assert.assertEquals;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.uimafit.testing.factory.TokenBuilder;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.phonetics.type.PhoneticTranscription;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class PhoneticTranscriptorTestUtil
{
    public static void runTest(AnalysisEngineDescription desc, String text, String ... goldTranscriptions) throws Exception {
   
        AnalysisEngine engine = createPrimitive(desc);
        JCas jcas = engine.newJCas();
        TokenBuilder<Token, Sentence> tb = new TokenBuilder<Token, Sentence>(Token.class, Sentence.class);
        tb.buildTokens(jcas, text);
        engine.process(jcas);
        
        int i=0;
        for (PhoneticTranscription transcription : JCasUtil.select(jcas, PhoneticTranscription.class)) {
            assertEquals(goldTranscriptions[i], transcription.getTranscription());
            i++;
        }
    }
}
