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
package org.dkpro.core.io.bioc;

import static java.util.Arrays.asList;

import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import static org.apache.commons.io.IOUtils.closeQuietly;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.io.bioc.internal.DKPro2BioC;
import org.dkpro.core.io.bioc.internal.model.BioCCollection;
import org.dkpro.core.io.bioc.internal.model.BioCDocument;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import eu.openminted.share.annotations.api.DocumentationResource;
import javanet.staxutils.IndentingXMLEventWriter;

/**
 * UIMA CAS consumer writing the CAS document text as plain text file.
 */
@ResourceMetaData(name = "BioC Writer")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability({MimeTypes.APPLICATION_X_BIOC})
@TypeCapability(
        inputs = {
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData"})
public class BioCWriter
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
     * Character encoding of the output data.
     */
    public static final String PARAM_TARGET_ENCODING = ComponentParameters.PARAM_TARGET_ENCODING;
    @ConfigurationParameter(name = PARAM_TARGET_ENCODING, mandatory = true, 
            defaultValue = ComponentParameters.DEFAULT_ENCODING)
    private String targetEncoding;
    
    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        BioCDocument document = new BioCDocument();
        DKPro2BioC.convert(aJCas, document);

        BioCCollection collection = new BioCCollection();
        DocumentMetaData dmd = DocumentMetaData.get(aJCas);
        collection.setSource(dmd.getCollectionId());
        collection.setDocuments(asList(document));
        
        OutputStream docOS = null;
        XMLEventWriter xmlEventWriter = null;
        try {
            docOS = getOutputStream(aJCas, filenameSuffix);

            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            xmlEventWriter = new IndentingXMLEventWriter(
                    xmlOutputFactory.createXMLEventWriter(docOS, targetEncoding));
            
            JAXBContext context = JAXBContext.newInstance(BioCCollection.class);
            Marshaller marshaller = context.createMarshaller();
            
            marshaller.marshal(collection, xmlEventWriter);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
        finally {
            if (xmlEventWriter != null) {
                try {
                    xmlEventWriter.close();
                }
                catch (XMLStreamException e) {
                    getLogger().warn("Error closing the XML event writer", e);
                }
            }
            
            closeQuietly(docOS);
        }
    }
}
