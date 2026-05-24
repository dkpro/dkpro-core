/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.json;

import java.io.IOException;
import java.io.InputStream;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.jcas.JCas;
import org.apache.uima.json.jsoncas2.JsonCas2Deserializer;
import org.apache.uima.json.jsoncas2.mode.FeatureStructuresMode;
import org.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import org.dkpro.core.api.parameter.MimeTypes;

import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Reader for the UIMA JSON CAS 2 format produced by the {@code uimaj-io-json} module.
 */
@ResourceMetaData(name = "UIMA JSON CAS Reader")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({ MimeTypes.APPLICATION_X_UIMA_JSON })
public class JsonCasReader
    extends JCasResourceCollectionReader_ImplBase
{
    /**
     * Whether feature structures are encoded as JSON arrays or JSON objects in the input.
     */
    public static final String PARAM_FEATURE_STRUCTURES_MODE = "featureStructuresMode";
    @ConfigurationParameter(name = PARAM_FEATURE_STRUCTURES_MODE, mandatory = true, defaultValue = "AS_ARRAY")
    private FeatureStructuresMode featureStructuresMode;

    @Override
    public void getNext(JCas aJCas) throws IOException, CollectionException
    {
        Resource res = nextFile();
        initCas(aJCas, res);

        var deserializer = new JsonCas2Deserializer();
        deserializer.setFsMode(featureStructuresMode);

        try (InputStream is = res.getInputStream()) {
            deserializer.deserialize(is, aJCas.getCas());
        }
    }
}
