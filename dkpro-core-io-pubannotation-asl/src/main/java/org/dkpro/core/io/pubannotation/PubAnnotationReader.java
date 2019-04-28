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
package org.dkpro.core.io.pubannotation;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.MimeTypeCapability;
import org.apache.uima.fit.descriptor.ResourceMetaData;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase;
import org.dkpro.core.api.parameter.MimeTypes;
import org.dkpro.core.io.pubannotation.internal.PubAnnotation2DKPro;
import org.dkpro.core.io.pubannotation.internal.model.PADocument;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

/**
 * Reader for the PubAnnotation format.
 * 
 * Since the PubAnnotation format only associates spans/relations with simple values and since
 * annotations are not typed, it is necessary to define target types and features via
 * {@link #PARAM_SPAN_TYPE} and {@link #PARAM_SPAN_LABEL_FEATURE}. In PubAnnotation, every
 * annotation has an ID. If the target type has a suitable feature to retain the ID, it can be
 * configured via {@link #PARAM_SPAN_ID_FEATURE}.
 * 
 * The {@code sourcedb} and {@code sourceid} from the PubAnnotation document are imported as
 * {@link DocumentMetaData#setCollectionId(String) collectionId} and
 * {@link DocumentMetaData#setDocumentId(String) documentId} respectively. If present, also the
 * {@code target} is imported as {@link DocumentMetaData#setDocumentUri(String) documentUri}. The
 * {@link DocumentMetaData#setDocumentBaseUri(String) documentBaseUri} is cleared in this case.
 * 
 * Currently supports only span annotations, i.e. no relations or modifications. Discontinuous
 * segments are also not supported.
 * 
 * @see <a href="http://www.pubannotation.org/docs/annotation-format/">PubAnnotation format</a>
 */
@ResourceMetaData(name = "PubAnnotation Reader")
@MimeTypeCapability({MimeTypes.APPLICATION_X_PUB_ANNOTATION_JSON})
@TypeCapability(
        outputs = { 
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData" })
public class PubAnnotationReader
    extends JCasResourceCollectionReader_ImplBase
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
     * The feature on the span annotation type which receives the label.
     */
    public static final String PARAM_RESOLVE_NAMESPACES = "resolveNamespaces";
    @ConfigurationParameter(name = PARAM_RESOLVE_NAMESPACES, mandatory = true, defaultValue = "false")
    private boolean resolveNamespaces;

    private ObjectMapper mapper;
    
    @Override
    public void initialize(UimaContext aContext)
        throws ResourceInitializationException
    {
        super.initialize(aContext);
        
        mapper = new ObjectMapper();
        // Hack because LXF dumper presently creates invalid JSON
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }
    
    @Override
    public void getNext(JCas aCAS)
        throws IOException, CollectionException
    {
        Resource res = nextFile();
        initCas(aCAS, res);

        PubAnnotation2DKPro converter = new PubAnnotation2DKPro();
        converter.setSpanMapping(spanType, spanIdFeature, spanLabelFeature);
        converter.setResolveNamespaces(resolveNamespaces);

        try (InputStream is = new BufferedInputStream(res.getInputStream())) {
            PADocument doc = mapper.readValue(is, PADocument.class);
            converter.convert(doc, aCAS);
        }
    }
}
