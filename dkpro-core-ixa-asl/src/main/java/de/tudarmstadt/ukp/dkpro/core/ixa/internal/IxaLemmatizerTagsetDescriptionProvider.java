/*
 * Copyright 2016
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
package de.tudarmstadt.ukp.dkpro.core.ixa.internal;

import static java.util.Collections.singletonMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.TagsetBase;
import opennlp.tools.ml.model.AbstractModel;
import opennlp.tools.ml.model.SequenceClassificationModel;

public class IxaLemmatizerTagsetDescriptionProvider
    extends TagsetBase
{
    private String name;
    private String layer;
    private SequenceClassificationModel<String> model;
    private String feature;
    private String separator = "=";

    public IxaLemmatizerTagsetDescriptionProvider(String aName, Class<?> aLayer,
            SequenceClassificationModel<String> aModel, String aFeature)
    {
        name = aName;
        layer = aLayer.getName();
        model = aModel;
        feature = aFeature;
    }
    
    @Override
    public Map<String, String> getLayers()
    {
        return singletonMap(layer, name);
    }

    @Override
    public Set<String> listTags(String aLayer, String aTagsetName)
    {
        try {
            AbstractModel innerModel = (AbstractModel) FieldUtils.readField(model, "model", true);
            HashMap<String, Integer> pmap = (HashMap<String, Integer>) FieldUtils
                    .readField(innerModel, "pmap", true);
            
            Set<String> tagSet = new TreeSet<String>();
            String prefix = feature + separator;
            for (Object key : pmap.keySet()) {
                if (key instanceof String && ((String) key).startsWith(prefix)) {
                    tagSet.add(StringUtils.substringAfter(((String) key), separator));
                }
            }
    
            return tagSet;
        }
        catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public SequenceClassificationModel<String> getModel()
    {
        return model;
    }
}
