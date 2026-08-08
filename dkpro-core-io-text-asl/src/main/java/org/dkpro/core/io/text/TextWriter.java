/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.io.text;

import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;

import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * UIMA CAS consumer writing the CAS document text as plain text file.
 */
@ResourceMetaData(name = "Text Writer")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({ MimeTypes.TEXT_PLAIN })
@TypeCapability(inputs = { "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData" })
public class TextWriter
    extends JCasFileWriter_ImplBase
{
    /**
     * Specify the suffix of output files. Default value <code>.txt</code>. If the suffix is not
     * needed, provide an empty string as value.
     */
    public static final String PARAM_FILENAME_EXTENSION = ComponentParameters.PARAM_FILENAME_EXTENSION;
    @ConfigurationParameter(name = PARAM_FILENAME_EXTENSION, defaultValue = ".txt")
    private String filenameSuffix;

    /**
     * Character encoding of the output data.
     */
    public static final String PARAM_TARGET_ENCODING = "targetEncoding";
    @ConfigurationParameter(name = PARAM_TARGET_ENCODING, defaultValue = ComponentParameters.DEFAULT_ENCODING)
    private String targetEncoding;

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        try (OutputStream docOS = getOutputStream(aJCas, filenameSuffix)) {
            IOUtils.write(aJCas.getDocumentText(), docOS, targetEncoding);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
