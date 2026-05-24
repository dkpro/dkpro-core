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

import java.io.OutputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.jcas.JCas;
import org.apache.uima.json.jsoncas2.JsonCas2Serializer;
import org.apache.uima.json.jsoncas2.mode.FeatureStructuresMode;
import org.apache.uima.json.jsoncas2.mode.OffsetConversionMode;
import org.apache.uima.json.jsoncas2.mode.SofaMode;
import org.apache.uima.json.jsoncas2.mode.TypeSystemMode;
import org.apache.uima.json.jsoncas2.ref.FullyQualifiedTypeRefGenerator;
import org.apache.uima.json.jsoncas2.ref.SequentialIdRefGenerator;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.dkpro.core.api.parameter.MimeTypes;

import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Writer for the UIMA JSON CAS 2 format produced by the {@code uimaj-io-json} module.
 */
@ResourceMetaData(name = "UIMA JSON CAS Writer")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({ MimeTypes.APPLICATION_X_UIMA_JSON })
public class JsonCasWriter
    extends JCasFileWriter_ImplBase
{
    /**
     * Whether feature structures are encoded as JSON arrays or JSON objects.
     */
    public static final String PARAM_FEATURE_STRUCTURES_MODE = "featureStructuresMode";
    @ConfigurationParameter(name = PARAM_FEATURE_STRUCTURES_MODE, mandatory = true, defaultValue = "AS_ARRAY")
    private FeatureStructuresMode featureStructuresMode;

    /**
     * How to represent the SOFA in the output.
     */
    public static final String PARAM_SOFA_MODE = "sofaMode";
    @ConfigurationParameter(name = PARAM_SOFA_MODE, mandatory = true, defaultValue = "AS_REGULAR_FEATURE_STRUCTURE")
    private SofaMode sofaMode;

    /**
     * Offset units used for annotation begin/end values.
     */
    public static final String PARAM_OFFSET_CONVERSION_MODE = "offsetConversionMode";
    @ConfigurationParameter(name = PARAM_OFFSET_CONVERSION_MODE, mandatory = true, defaultValue = "UTF_16")
    private OffsetConversionMode offsetConversionMode;

    /**
     * How much of the type system to include in the output.
     */
    public static final String PARAM_TYPE_SYSTEM_MODE = "typeSystemMode";
    @ConfigurationParameter(name = PARAM_TYPE_SYSTEM_MODE, mandatory = true, defaultValue = "MINIMAL")
    private TypeSystemMode typeSystemMode;

    private JsonCas2Serializer serializer;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException
    {
        super.initialize(aContext);

        serializer = new JsonCas2Serializer();
        serializer.setFsMode(featureStructuresMode);
        serializer.setSofaMode(sofaMode);
        serializer.setOffsetConversionMode(offsetConversionMode);
        serializer.setTypeSystemMode(typeSystemMode);
        serializer.setTypeRefGeneratorSupplier(FullyQualifiedTypeRefGenerator::new);
        serializer.setIdRefGeneratorSupplier(SequentialIdRefGenerator::new);
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        try (OutputStream docOS = getOutputStream(aJCas, ".json")) {
            serializer.serialize(aJCas.getCas(), docOS);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
