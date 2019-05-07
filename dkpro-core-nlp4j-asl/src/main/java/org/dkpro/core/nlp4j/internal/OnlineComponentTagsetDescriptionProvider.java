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
package org.dkpro.core.nlp4j.internal;

import static java.util.Collections.singletonMap;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.dkpro.core.api.metadata.TagsetBase;

import edu.emory.mathcs.nlp.component.template.OnlineComponent;
import edu.emory.mathcs.nlp.component.template.node.AbstractNLPNode;
import edu.emory.mathcs.nlp.component.template.state.NLPState;
import edu.emory.mathcs.nlp.learning.optimization.OnlineOptimizer;

public class OnlineComponentTagsetDescriptionProvider
    <N extends AbstractNLPNode<N>, S extends NLPState<N>>
    extends TagsetBase
{
    private String name;
    private String layer;
    private OnlineComponent<N, S> model;

    public OnlineComponentTagsetDescriptionProvider(String aName, Class<?> aLayer,
            OnlineComponent<N, S> aModel)
    {
        name = aName;
        layer = aLayer.getName();
        model = aModel;
    }
    
    @Override
    public Map<String, String> getLayers()
    {
        return singletonMap(layer, name);
    }

    @Override
    public Set<String> listTags(String aLayer, String aTagsetName)
    {
        OnlineOptimizer optimizer = model.getOptimizer();
        
        Set<String> tagSet = new TreeSet<String>();
        for (int i = 0; i < optimizer.getLabelSize(); i++) {
            String tag = optimizer.getLabel(i);
            tagSet.add(tag);
        }

        return tagSet;
    }

    public OnlineComponent<N, S> getModel()
    {
        return model;
    }
}
