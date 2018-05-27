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

import static org.apache.uima.fit.util.CasUtil.select;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.io.pubannotation.internal.model.PADenotation;
import org.dkpro.core.io.pubannotation.internal.model.PADocument;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class DKPro2PubAnnotation
{
    private String spanTypeName;
    private String spanIdFeatureName;
    private String spanLabelFeatureName;
    
    public void convert(JCas aJCas, PADocument aDoc)
    {
        aDoc.setText(aJCas.getDocumentText());
        
        // Map metadata
        DocumentMetaData dmd = DocumentMetaData.get(aJCas);
        aDoc.setTarget(dmd.getDocumentUri());
        aDoc.setSourceDb(dmd.getCollectionId());
        aDoc.setSourceId(dmd.getDocumentId());
        
        // Map span annotations
        CAS cas = aJCas.getCas();
        Type spanType = CasUtil.getAnnotationType(cas, spanTypeName);
        Feature spanIdFeature = spanType.getFeatureByBaseName(spanIdFeatureName);
        Feature spanLabelFeature = spanType.getFeatureByBaseName(spanLabelFeatureName);
        
        for (AnnotationFS spanAnnotation : select(cas, spanType)) {
            PADenotation denotation = new PADenotation(spanAnnotation.getBegin(),
                    spanAnnotation.getEnd());
            
            // Set the ID from the ID feature if one was specified, otherwise set it from the
            // annotation address.
            if (spanIdFeature != null) {
                denotation.setId(spanAnnotation.getFeatureValueAsString(spanIdFeature));
            }
            else {
                denotation.setId(Integer.toString(((CASImpl) cas).ll_getFSRef(spanAnnotation)));
            }
            
            if (spanLabelFeature != null) {
                denotation.setObj(spanAnnotation.getFeatureValueAsString(spanLabelFeature));
            }
            
            aDoc.addDenotation(denotation);
        }
    }
    
    public void setSpanMapping(String aSpanType, String aSpanIdFeature, String aSpanLabelFeature)
    {
        spanTypeName = aSpanType;
        spanIdFeatureName = aSpanIdFeature;
        spanLabelFeatureName = aSpanLabelFeature;
    }
}
