/*
 * Copyright 2017
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
 */
package org.dkpro.core.textnormalizer.frequency;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.fit.descriptor.LanguageCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;

import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Takes a text and replaces sharp s
 */
@Component(OperationType.NORMALIZER)
@ResourceMetaData(name = "Sharp S (ß) Normalizer")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
@LanguageCapability("de")
public class SharpSNormalizer
    extends ReplacementFrequencyNormalizer_ImplBase
{
    @Override
    public Map<String, String> getReplacementMap()
    {
        Map<String, String> map = new HashMap<String, String>();

        map.put("ss", "ß");
        map.put("ß", "ss");

        return map;
    }
}
