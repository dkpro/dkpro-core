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
package org.dkpro.core.io.pubannotation.internal;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Optional;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.io.pubannotation.internal.model.PADenotation;
import org.dkpro.core.io.pubannotation.internal.model.PADocument;
import org.dkpro.core.io.pubannotation.internal.model.PANamespace;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class PubAnnotation2DKPro
{
    private String spanTypeName;
    private String spanIdFeatureName;
    private String spanLabelFeatureName;
    private boolean resolveNamespaces;
    
    public void convert(PADocument aDoc, JCas aCAS)
    {
        aCAS.setDocumentText(aDoc.getText());

        // If source DB and/or source ID are set, put them into the collection/document IDs
        DocumentMetaData dmd = DocumentMetaData.get(aCAS);
        if (isNotBlank(aDoc.getSourceDb())) {
            dmd.setCollectionId(aDoc.getSourceDb());
        }
        if (isNotBlank(aDoc.getSourceId())) {
            dmd.setDocumentId(aDoc.getSourceId());
        }
        
        // If the target is set in PubAnnotation, treat it as the document URI and clear the
        // documentBaseUri since we do not know the base URI for PubAnnotation files.
        // REC: not sure if this is a great idea...
        if (isNotBlank(aDoc.getTarget())) {
            dmd.setDocumentBaseUri(null);
            dmd.setDocumentUri(aDoc.getTarget());
        }

        // Map span annotations
        CAS cas = aCAS.getCas();
        Type spanType = CasUtil.getAnnotationType(cas, spanTypeName);
        Feature spanIdFeature = spanType.getFeatureByBaseName(spanIdFeatureName);
        Feature spanLabelFeature = spanType.getFeatureByBaseName(spanLabelFeatureName);
        Optional<PANamespace> baseNS = aDoc.getNamespace(PANamespace.PREFIX_BASE);
        for (PADenotation span : aDoc.getDenotations()) {
            AnnotationFS spanAnnotation = cas.createAnnotation(spanType, span.getSpan().getBegin(),
                    span.getSpan().getEnd());

            // If an ID feature was set, then we set its value
            if (spanIdFeature != null) {
                spanAnnotation.setFeatureValueFromString(spanIdFeature, span.getId());
            }

            // If a label feature was set, then we set its value
            if (spanLabelFeature != null) {
                String value = span.getObj();
                if (resolveNamespaces && baseNS.isPresent()) {
                    value = baseNS.get().getUri() + value;
                }
                spanAnnotation.setFeatureValueFromString(spanLabelFeature, value);
            }
            
            cas.addFsToIndexes(spanAnnotation);
        }
    }

    public void setResolveNamespaces(boolean aResolveNamespaces)
    {
        resolveNamespaces = aResolveNamespaces;
    }
    
    public boolean getResolveNamespaces()
    {
        return resolveNamespaces;
    }
    
    public void setSpanMapping(String aSpanType, String aSpanIdFeature, String aSpanLabelFeature)
    {
        spanTypeName = aSpanType;
        spanIdFeatureName = aSpanIdFeature;
        spanLabelFeatureName = aSpanLabelFeature;
    }
}
