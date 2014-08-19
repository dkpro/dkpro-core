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
 ******************************************************************************/
package de.tudarmstadt.ukp.dkpro.core.opennlp.internal;

import java.util.Set;
import java.util.TreeSet;

import opennlp.model.AbstractModel;

public class OpenNlpChunkerTagsetDescriptionProvider
    extends OpenNlpTagsetDescriptionProvider
{
    public OpenNlpChunkerTagsetDescriptionProvider(String aName, Class<?> aLayer, AbstractModel aModel)
    {
        super(aName, aLayer, aModel);
    }

    @Override
    public Set<String> listTags(String aLayer, String aTagsetName)
    {
        Set<String> tagSet = new TreeSet<String>();
        for (int i = 0; i < getModel().getNumOutcomes(); i++) {
            String t = getModel().getOutcome(i);
            if (t.startsWith("B-") || t.startsWith("I-")) {
                tagSet.add(t.substring(2));
            }
            else {
                tagSet.add(t);
            }
        }

        return tagSet;
    }
}
