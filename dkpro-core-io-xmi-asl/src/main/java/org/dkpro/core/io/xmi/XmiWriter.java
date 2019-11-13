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
package org.dkpro.core.io.xmi;

import static java.util.Arrays.asList;
import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.OutputKeys;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.TypeSystemUtil;
import org.apache.uima.util.XMLSerializer;
import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.api.resources.CompressionUtils;
import org.xml.sax.SAXException;

import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * UIMA XMI format writer.
 */
@ResourceMetaData(name = "UIMA XMI CAS Writer")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({MimeTypes.APPLICATION_VND_XMI_XML, MimeTypes.APPLICATION_X_UIMA_XMI})
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData"})
public class XmiWriter
    extends JCasFileWriter_ImplBase
{
    /**
     * Format and indent the XML.
     */
    public static final String PARAM_PRETTY_PRINT = "prettyPrint";
    @ConfigurationParameter(name = PARAM_PRETTY_PRINT, mandatory = true, defaultValue = "true")
    private boolean prettyPrint;
    
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
     * Specify the suffix of output files. Default value <code>.xmi</code>. If the suffix is not
     * needed, provide an empty string as value.
     */
    public static final String PARAM_FILENAME_EXTENSION = 
            ComponentParameters.PARAM_FILENAME_EXTENSION;
    @ConfigurationParameter(name = PARAM_FILENAME_EXTENSION, mandatory = true, defaultValue = ".xmi")
    private String filenameSuffix;

    /**
     * Defines the XML version used for serializing the data. The default is XML {@code "1.0"}. 
     * However, XML 1.0 does not support certain Unicode characters. To support a wider range of 
     * characters, you can switch this parameter to {@code "1.1"}.
     */
    public static final String PARAM_VERSION = "version";
    @ConfigurationParameter(name = PARAM_VERSION, mandatory = true, defaultValue = "1.0")
    private String version;
    
    
    private boolean typeSystemWritten;

    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        if (!asList("1.0", "1.1").contains(version)) {
            throw new ResourceInitializationException(new IllegalArgumentException(
                    "Invalid value for parameter version: [" + version + "]"));
        }

        typeSystemWritten = false;
    }

    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        try (OutputStream docOS = getOutputStream(aJCas, filenameSuffix)) {
            XmiCasSerializer xmiCasSerializer = new XmiCasSerializer(null);
            XMLSerializer sax2xml = new XMLSerializer(docOS, prettyPrint);
            sax2xml.setOutputProperty(OutputKeys.VERSION, version);
            xmiCasSerializer.serialize(aJCas.getCas(), sax2xml.getContentHandler(), null, null,
                    null);

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
