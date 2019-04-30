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
package org.dkpro.core.ixa;

import static org.dkpro.core.api.resources.ResourceObjectProviderBase.LOCATION;

import org.apache.uima.UimaContext;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.opennlp.OpenNlpPosTagger;

import eu.openminted.share.annotations.api.Component;
import eu.openminted.share.annotations.api.DocumentationResource;
import eu.openminted.share.annotations.api.constants.OperationType;

/**
 * Part-of-Speech annotator using OpenNLP with IXA extensions.
 */
@Component(OperationType.PART_OF_SPEECH_TAGGER)
@ResourceMetaData(name = "IXA POS-Tagger")
@DocumentationResource("${docbase}/component-reference.html#engine-${shortClassName}")
public class IxaPosTagger
    extends OpenNlpPosTagger
{
    // The IXA POS tagger models make use of IXA classes. But they do so from within OpenNLP.
    // From the outside, it looks and works exactly like an OpenNLP POS tagger. So we just
    // derive from the OpenNlpPosTagger in side this module. This has the effect that through
    // the module dependencies, we have the required IXA JARs on the classpath. It also has
    // the effect that the package for the models changes from ...opennlp.lib to ...ixa.lib.
    
    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        modelProvider.setDefault(LOCATION,
                "classpath:/de/tudarmstadt/ukp/dkpro/core/ixa/lib/tagger-${language}-${variant}.properties");
    }
}
