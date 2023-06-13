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

import static org.dkpro.core.io.bioc.BioCComponent.getCollectionMetadataField;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

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
import org.dkpro.core.io.bioc.internal.CasToBioC;
import org.dkpro.core.io.bioc.internal.model.BioCCollection;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import eu.openminted.share.annotations.api.DocumentationResource;

/**
 * Writer for the BioC format.
 */
@ResourceMetaData(name = "BioC XML Writer")
@DocumentationResource("${docbase}/format-reference.html#format-${command}")
@MimeTypeCapability(MimeTypes.APPLICATION_X_BIOC)
@TypeCapability(outputs = { "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData" })
public class BioCWriter
    extends JCasFileWriter_ImplBase
    implements BioCComponent
{
    /**
     * Indent output.
     */
    public static final String PARAM_INDENT = "indent";
    @ConfigurationParameter(name = PARAM_INDENT, mandatory = true, defaultValue = "true")
    private boolean indent;

    /**
     * Specify the suffix of output files. Default value <code>.xml</code>. If the suffix is not
     * needed, provide an empty string as value.
     */
    public static final String PARAM_FILENAME_EXTENSION = ComponentParameters.PARAM_FILENAME_EXTENSION;
    @ConfigurationParameter(name = PARAM_FILENAME_EXTENSION, mandatory = true, defaultValue = ".xml")
    private String filenameSuffix;

    /**
     * Character encoding of the output data.
     */
    public static final String PARAM_TARGET_ENCODING = ComponentParameters.PARAM_TARGET_ENCODING;
    @ConfigurationParameter(name = PARAM_TARGET_ENCODING, mandatory = true, //
            defaultValue = ComponentParameters.DEFAULT_ENCODING)
    private String targetEncoding;

    private JAXBContext context;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException
    {
        super.initialize(aContext);

        try {
            context = JAXBContext.newInstance(BioCCollection.class);
        }
        catch (JAXBException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException
    {
        try (var docOS = getOutputStream(aJCas, filenameSuffix)) {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            // Set to fragment mode to omit XML declaration
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

            var bioCCollection = new BioCCollection();

            // Base-information - may be overwritten by the metadata fields below
            var dmd = DocumentMetaData.get(aJCas);
            bioCCollection.setSource(dmd.getCollectionId());

            // Use BioC metadata fields if available
            getCollectionMetadataField(aJCas.getCas(), E_SOURCE)
                    .ifPresent($ -> bioCCollection.setSource($.getValue()));
            getCollectionMetadataField(aJCas.getCas(), E_KEY)
                    .ifPresent($ -> bioCCollection.setKey($.getValue()));
            getCollectionMetadataField(aJCas.getCas(), E_DATE)
                    .ifPresent($ -> bioCCollection.setDate($.getValue()));

            new CasToBioC().convert(aJCas, bioCCollection);

            marshaller.marshal(bioCCollection, docOS);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
