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
 */package org.dkpro.core.io.pubannotation;

import java.io.OutputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.io.pubannotation.internal.GenericDKPro2PubAnnotation;
import org.dkpro.core.io.pubannotation.internal.model.PADocument;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.MimeTypes;

/**
 * Writer for the PubAnnotation format.
 * 
 * The {@code sourcedb} and {@code sourceid} from the PubAnnotation document are exported from
 * {@link DocumentMetaData#setCollectionId(String) collectionId} and
 * {@link DocumentMetaData#setDocumentId(String) documentId} respectively. The {@code target} is
 * exported from {@link DocumentMetaData#setDocumentUri(String) documentUri}.
 * 
 * Currently supports only span annotations, i.e. no relations or modifications. Discontinuous
 * segments are also not supported.
 * 
 * @see <a href="http://www.pubannotation.org/docs/annotation-format/">PubAnnotation format</a>
 */
@ResourceMetaData(name = "PubAnnotation Writer")
@MimeTypeCapability({MimeTypes.APPLICATION_X_PUB_ANNOTATION_JSON})
@TypeCapability(
        inputs = { 
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData" })
public class GenericPubAnnotationWriter
    extends JCasFileWriter_ImplBase
{
    /**
     * Specify the suffix of output files. Default value <code>.json</code>. If the suffix is not
     * needed, provide an empty string as value.
     */
    public static final String PARAM_FILENAME_EXTENSION = 
            ComponentParameters.PARAM_FILENAME_EXTENSION;
    @ConfigurationParameter(name = PARAM_FILENAME_EXTENSION, mandatory = true, defaultValue = ".json")
    private String filenameSuffix;

    private ObjectMapper mapper;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        mapper = new ObjectMapper();
    }
    
    @Override
    public void process(JCas aJCas)
        throws AnalysisEngineProcessException
    {
        PADocument doc = new PADocument();
        
        GenericDKPro2PubAnnotation converter = new GenericDKPro2PubAnnotation();
        converter.convert(aJCas, doc);
        
        try (OutputStream docOS = getOutputStream(aJCas, filenameSuffix)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(docOS, doc);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
