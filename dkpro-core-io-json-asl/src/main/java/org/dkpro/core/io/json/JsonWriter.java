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
package org.dkpro.core.io.json;

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.json.JsonCasSerializer;
import org.apache.uima.json.JsonCasSerializer.JsonContextFormat;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.TypeSystemUtil;
import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.api.resources.CompressionUtils;
import org.xml.sax.SAXException;

import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * UIMA JSON format writer.
 */
@ResourceMetaData(name = "UIMA JSON CAS Writer")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({MimeTypes.APPLICATION_X_UIMA_JSON})
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData"})
public class JsonWriter
    extends JCasFileWriter_ImplBase
{
    /**
     * Location to write the type system to. If this is not set, a file called typesystem.xml will
     * be written to the XMI output path. If this is set, it is expected to be a file relative
     * to the current work directory or an absolute file.
     * <br>
     * If this parameter is set, the {@link #PARAM_COMPRESSION} parameter has no effect on the
     * type system. Instead, if the file name ends in ".gz", the file will be compressed,
     * otherwise not.
     */
    public static final String PARAM_TYPE_SYSTEM_FILE = "typeSystemFile";
    @ConfigurationParameter(name = PARAM_TYPE_SYSTEM_FILE, mandatory = false)
    private File typeSystemFile;
    
    /**
     * Whether to pretty-print the JSON output.
     */
    public static final String PARAM_PRETTY_PRINT = "prettyPrint";
    @ConfigurationParameter(name = PARAM_PRETTY_PRINT, mandatory = true, defaultValue = "true")
    private boolean prettyPrint;
    
    /**
     * Whether to fields that have their default values from the JSON output.
     */
    public static final String PARAM_OMIT_DEFAULT_VALUES = "omitDefaultValues";
    @ConfigurationParameter(name = PARAM_OMIT_DEFAULT_VALUES, mandatory = true, defaultValue = "true")
    private boolean omitDefaultValues;

    /**
     * The level of detail to use for the context (i.e. type system) information. 
     */
    public static final String PARAM_JSON_CONTEXT_FORMAT = "jsonContextFormat";
    @ConfigurationParameter(name = PARAM_JSON_CONTEXT_FORMAT, mandatory = true, defaultValue = "omitExpandedTypeNames")
    private String jsonContextFormat;

    private boolean typeSystemWritten;

    private JsonCasSerializer jcs;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);

        typeSystemWritten = false;
        jcs = new JsonCasSerializer();
        jcs.setPrettyPrint(prettyPrint);
        jcs.setOmit0Values(omitDefaultValues);
        jcs.setJsonContext(JsonContextFormat.valueOf(jsonContextFormat));
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        try (OutputStream docOS = getOutputStream(aJCas, ".json")) {
            jcs.serialize(aJCas.getCas(), docOS);
            
            if (!typeSystemWritten) {
                writeTypeSystem(aJCas);
                typeSystemWritten = true;
            }
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private void writeTypeSystem(JCas aJCas)
        throws IOException, CASRuntimeException, SAXException
    {
        @SuppressWarnings("resource")
        OutputStream typeOS = null;
        
        try {
            if (typeSystemFile != null) {
                typeOS = CompressionUtils.getOutputStream(typeSystemFile);
            }
            else {
                typeOS = getOutputStream("TypeSystem", ".xml");
            }

            TypeSystemUtil.typeSystem2TypeSystemDescription(aJCas.getTypeSystem()).toXML(typeOS);
        }
        finally {
            closeQuietly(typeOS);
        }
    }
}
