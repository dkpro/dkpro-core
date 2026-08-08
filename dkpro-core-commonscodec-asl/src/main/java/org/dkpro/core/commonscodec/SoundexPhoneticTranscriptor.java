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
package org.dkpro.core.commonscodec;

import org.apache.commons.codec.language.Soundex;
import org.apache.uima.fit.descriptor.LanguageCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;

import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Soundex phonetic transcription based on Apache Commons Codec. Works for English.
 */
@ResourceMetaData(name = "Commons Codec Soundex Phonetic Transcriptor")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@LanguageCapability("en")
@TypeCapability(inputs = {
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" }, outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.phonetics.type.PhoneticTranscription" })
public class SoundexPhoneticTranscriptor
    extends PhoneticTranscriptor_ImplBase
{
    public SoundexPhoneticTranscriptor()
    {
        this.encoder = new Soundex();
    }
}
