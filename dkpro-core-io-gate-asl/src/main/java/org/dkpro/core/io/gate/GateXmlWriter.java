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
package org.dkpro.core.io.gate;

import java.io.OutputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.io.gate.internal.DKPro2Gate;

import eu.openminted.share.annotations.api.DocumentationResource;
import gate.DocumentExporter;
import gate.corpora.DocumentImpl;
import gate.corpora.export.GateXMLExporter;
import gate.util.GateException;

/**
 * Writer for the GATE XML format. This writer uses an explicit mapping from DKPro Core types
 * to typical GATE naming convensions.
 */
@ResourceMetaData(name = "GATE XML Writer")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({MimeTypes.APPLICATION_X_GATE_XML})
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData"})
public class GateXmlWriter
    extends JCasFileWriter_ImplBase
{
    /**
     * Specify the suffix of output files. Default value <code>.xml</code>. If the suffix is not
     * needed, provide an empty string as value.
     */
    public static final String PARAM_FILENAME_EXTENSION = 
            ComponentParameters.PARAM_FILENAME_EXTENSION;
    @ConfigurationParameter(name = PARAM_FILENAME_EXTENSION, mandatory = true, defaultValue = ".xml")
    private String filenameSuffix;

    /**
     * Annotation set name
     */
    public static final String PARAM_ANNOTATION_SET_NAME = "annotationSetName";
    @ConfigurationParameter(name = PARAM_ANNOTATION_SET_NAME, mandatory = false)
    private String annotationSetName;
    
    /**
     * Character encoding used by the output files.
     */
    public static final String PARAM_TARGET_ENCODING = ComponentParameters.PARAM_TARGET_ENCODING;
    @ConfigurationParameter(name = PARAM_TARGET_ENCODING, mandatory = true, 
            defaultValue = ComponentParameters.DEFAULT_ENCODING)
    private String targetEncoding;

    private DocumentExporter exporter;
    
    private DKPro2Gate converter;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        exporter = new GateXMLExporter();
        converter = new DKPro2Gate();
    }
    
    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        DocumentImpl document;
        try {
            document = new DocumentImpl();
            if (annotationSetName != null && annotationSetName.length() > 0) {
                converter.convert(aJCas, document, annotationSetName);
            }
            else {
                converter.convert(aJCas, document);
            }
        }
        catch (GateException e) {
            throw new AnalysisEngineProcessException(e);
        }
        
        try (OutputStream docOS = getOutputStream(aJCas, filenameSuffix)) {
            if (targetEncoding != null) {
                document.setEncoding(targetEncoding);
            }
            
            exporter.export(document, docOS);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
