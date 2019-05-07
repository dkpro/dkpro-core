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
import org.dkpro.core.api.io.JCasFileWriter_ImplBase;
import org.dkpro.core.api.parameter.ComponentParameters;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.io.pubannotation.internal.DKPro2PubAnnotation;
import org.dkpro.core.io.pubannotation.internal.model.PADocument;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

/**
 * Writer for the PubAnnotation format.
 * 
 * Since the PubAnnotation format only associates spans/relations with simple values and since
 * annotations are not typed, it is necessary to define target types and features via
 * {@link #PARAM_SPAN_TYPE} and {@link #PARAM_SPAN_LABEL_FEATURE}. In PubAnnotation, every
 * annotation has an ID. If the annotation type has an ID feature, it can be configured via
 * {@link #PARAM_SPAN_ID_FEATURE}. If this parameter is not set, the IDs are generated
 * automatically.
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
public class PubAnnotationWriter
    extends JCasFileWriter_ImplBase
{
    /**
     * The span annotation type to which the PubAnnotation spans are mapped.
     */
    public static final String PARAM_SPAN_TYPE = "spanType";
    @ConfigurationParameter(name = PARAM_SPAN_TYPE, mandatory = true)
    private String spanType;

    /**
     * The feature on the span annotation type which receives the ID.
     */
    public static final String PARAM_SPAN_ID_FEATURE = "spanIdFeature";
    @ConfigurationParameter(name = PARAM_SPAN_ID_FEATURE, mandatory = false)
    private String spanIdFeature;
    
    /**
     * The feature on the span annotation type which receives the label.
     */
    public static final String PARAM_SPAN_LABEL_FEATURE = "spanLabelFeature";
    @ConfigurationParameter(name = PARAM_SPAN_LABEL_FEATURE, mandatory = false)
    private String spanLabelFeature;
    
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
        
        DKPro2PubAnnotation converter = new DKPro2PubAnnotation();
        converter.setSpanMapping(spanType, spanIdFeature, spanLabelFeature);
        converter.convert(aJCas, doc);
        
        try (OutputStream docOS = getOutputStream(aJCas, filenameSuffix)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(docOS, doc);
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
    }
}
