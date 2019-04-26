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

import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.io.lif.internal.DKPro2Lif;
import org.lappsgrid.serialization.DataContainer;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Writer for the LIF format.
 */
@ResourceMetaData(name = "LAPPS Grid LIF Writer")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({MimeTypes.APPLICATION_X_LIF_JSON})
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
                "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
                "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
                "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent",
                "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency"})
public class LifWriter
    extends JCasFileWriter_ImplBase
{
    /**
     * Character encoding of the output data.
     */
    public static final String PARAM_TARGET_ENCODING = ComponentParameters.PARAM_TARGET_ENCODING;
    @ConfigurationParameter(name = PARAM_TARGET_ENCODING, mandatory = true, 
            defaultValue = ComponentParameters.DEFAULT_ENCODING)
    private String targetEncoding;
    
    /**
     * Specify the suffix of output files. Default value <code>.lif</code>. If the suffix is not
     * needed, provide an empty string as value.
     */
    public static final String PARAM_FILENAME_EXTENSION = 
            ComponentParameters.PARAM_FILENAME_EXTENSION;
    @ConfigurationParameter(name = PARAM_FILENAME_EXTENSION, mandatory = true, defaultValue = ".lif")
    private String filenameSuffix;

    /**
     * Write timestamp to view.
     */
    public static final String PARAM_WRITE_TIMESTAMP = "writeTimestamp";
    @ConfigurationParameter(name = PARAM_WRITE_TIMESTAMP, mandatory = true, defaultValue = "true")
    private boolean writeTimestamp;


    /**
     * Wrap as data object.
     */
    public static final String PARAM_ADD_ENVELOPE = "wrapAsDataObject";
    @ConfigurationParameter(name = PARAM_ADD_ENVELOPE, mandatory = true, defaultValue = "false")
    private boolean wrapAsDataObject;

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        // Convert UIMA to LIF Container
        Container container = new Container();

        new DKPro2Lif().convert(aJCas, container);
        
        // Clear timestamp if requested.
        if (!writeTimestamp) {
            for (View view : container.getViews()) {
                view.setTimestamp(null);
            }
        }
        
        Object finalOutputObject = container;
        if (wrapAsDataObject) {
            finalOutputObject = new DataContainer(container);
        }
        
        try (OutputStream docOS = getOutputStream(aJCas, filenameSuffix)) {
            String json = Serializer.toPrettyJson(finalOutputObject);
            IOUtils.write(json, docOS, targetEncoding);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
