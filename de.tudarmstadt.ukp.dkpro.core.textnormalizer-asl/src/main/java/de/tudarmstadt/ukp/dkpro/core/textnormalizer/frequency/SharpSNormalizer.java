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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.textnormalizer.frequency;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.fit.descriptor.TypeCapability;

/**
 * Takes a text and replaces sharp s
 * 
 * @author Sebastian Kneise, nico.erbs@gmail.com
 * 
 */
@TypeCapability(
        inputs = { "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token" }, 
        outputs = { "de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation" })
public class SharpSNormalizer
    extends ReplacementFrequencyNormalizer_ImplBase
{

    @Override
    public Map<String, String> getReplacementMap()
    {
        Map<String,String> replacementMap = new HashMap<String,String>();
        
        replacementMap.put("ss", "ß");
        replacementMap.put("ß", "ss");

        return replacementMap;
    }
}