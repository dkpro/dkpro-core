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

import org.apache.uima.fit.descriptor.ExternalResource;

import de.tudarmstadt.ukp.dkpro.core.api.frequency.provider.FrequencyCountProvider;
import de.tudarmstadt.ukp.dkpro.core.textnormalizer.Normalizer_ImplBase;

public abstract class FrequencyNormalizer_ImplBase
    extends Normalizer_ImplBase
{
    public static final String FREQUENCY_PROVIDER = "FrequencyProvider";
    @ExternalResource(key = FREQUENCY_PROVIDER, mandatory = true)
    protected FrequencyCountProvider frequencyProvider;

}