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

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.TypeCapability;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.phonetics.type.PhoneticTranscription;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

/**
 * Base class for all kinds of phonetic transcriptors based on Apache Commons Codec.
 * 
 * @author zesch
 *
 */

@TypeCapability(
        inputs={"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token"},
        outputs={"de.tudarmstadt.ukp.dkpro.core.api.phonetics.type.PhoneticTranscription"})

public abstract class PhoneticTranscriptor_ImplBase
    extends JCasAnnotator_ImplBase
{
    protected StringEncoder encoder;
    
    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        for (Token token : JCasUtil.select(jcas, Token.class)) {
            PhoneticTranscription transcription = new PhoneticTranscription(jcas, token.getBegin(), token.getEnd());
            transcription.setTranscription(encode(token.getCoveredText()));
            transcription.setName(encoder.getClass().getName());  
            transcription.addToIndexes();
        }       
    }

    protected String encode(String string)
            throws AnalysisEngineProcessException
    {       
        try {
            String encodedString = encoder.encode(string);
            return encodedString;

        }
        catch (EncoderException e) {
            throw new AnalysisEngineProcessException(e);
        }  
    }
}
