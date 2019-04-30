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
package org.dkpro.core.opennlp.internal;

import static java.util.Collections.singletonMap;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.dkpro.core.api.metadata.TagsetBase;

import opennlp.tools.ml.model.MaxentModel;
import opennlp.tools.ml.model.SequenceClassificationModel;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.util.TokenTag;

public class OpenNlpParserTagsetDescriptionProvider
    extends TagsetBase
{
    private String name;
    private String layer;
    private ParserModel model;
    private Properties metadata;
    
    public OpenNlpParserTagsetDescriptionProvider(String aName, Class<?> aLayer, ParserModel aModel,
            Properties aMetadata)
    {
        name = aName;
        layer = aLayer.getName();
        model = aModel;
        metadata = aMetadata;
    }
    
    @Override
    public Map<String, String> getLayers()
    {
        return singletonMap(layer, name);
    }

    public ParserModel getModel()
    {
        return model;
    }

    @Override
    public Set<String> listTags(String aLayer, String aTagsetName)
    {
        Set<String> tagSet = new TreeSet<String>();
        
        SequenceClassificationModel<TokenTag> seqModel = model.getParserChunkerModel()
                .getChunkerSequenceModel();
        collect(seqModel.getOutcomes(), tagSet);
        
        if (model.getBuildModel() != null) {
            collect(model.getBuildModel(), tagSet);
        }
        
        return tagSet;
    }
    
    private void collect(MaxentModel aMaxEnt, Set<String> aTagSet)
    {
        String[] tags = new String[aMaxEnt.getNumOutcomes()];
        
        for (int i = 0; i < aMaxEnt.getNumOutcomes(); i++) {
            tags[i] = aMaxEnt.getOutcome(i);
        }
        
        collect(tags, aTagSet);
    }
    
    private void collect(String[] aOutcomes, Set<String> aTagSet)
    {
        for (String tag : aOutcomes) {
            String t = tag;
            if (tag.startsWith("C-") || tag.startsWith("S-")) {
                t = tag.substring(2);
            }
            
            if (metadata.containsKey("constituent.tag.map." + t)) {
                t = metadata.getProperty("constituent.tag.map." + t);
            }
            
            aTagSet.add(t);
        }
    }
}
