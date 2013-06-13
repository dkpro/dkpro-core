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

import org.apache.commons.codec.language.Metaphone;
import org.uimafit.descriptor.TypeCapability;

/**
 * Metaphone phonetic transcription based on Apache Commons Codec.
 * Works for English.
 * 
 * @author zesch
 *
 */

@TypeCapability(
        inputs={"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token"},
        outputs={"de.tudarmstadt.ukp.dkpro.core.api.phonetics.type.PhoneticTranscription"})

public class MetaphonePhoneticTranscriptor
    extends PhoneticTranscriptor_ImplBase
{

    public MetaphonePhoneticTranscriptor()
    {
        this.encoder = new Metaphone();
    }
}
