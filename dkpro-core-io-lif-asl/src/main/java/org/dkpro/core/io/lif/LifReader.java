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
package org.dkpro.core.io.lif;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.io.lif.internal.Lif2DKPro;
import org.lappsgrid.serialization.DataContainer;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;

import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Reader for the LIF format.
 */
@ResourceMetaData(name = "LAPPS Grid LIF Reader")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({MimeTypes.APPLICATION_X_LIF_JSON})
@TypeCapability(
        outputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
                "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent",
                "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency"})
public class LifReader
    extends JCasResourceCollectionReader_ImplBase
{
    /**
     * Name of configuration parameter that contains the character encoding used by the input files.
     */
    public static final String PARAM_SOURCE_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_SOURCE_ENCODING, mandatory = true, 
            defaultValue = ComponentParameters.DEFAULT_ENCODING)
    private String sourceEncoding;
    
    @Override
    public void getNext(JCas aJCas)
        throws IOException, CollectionException
    {
        Resource res = nextFile();
        initCas(aJCas, res);

        Container container;
        try (InputStream is = res.getInputStream()) {
            String json = IOUtils.toString(res.getInputStream(), sourceEncoding);
            try {
                // First try parsing without the wire wrapper.
                container = Serializer.parse(json, Container.class);
            }
            catch (Exception e) {
                // If that fails, it might be because there is a wire wrapper around the actual
                // data, so let's try that.
                container = (Container) Serializer.parse(json, DataContainer.class).getPayload();
            }
        }
        
        new Lif2DKPro().convert(container, aJCas);
    }
}
