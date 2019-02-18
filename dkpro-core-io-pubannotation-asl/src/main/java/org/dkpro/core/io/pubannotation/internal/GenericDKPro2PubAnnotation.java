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

import static org.apache.uima.fit.util.JCasUtil.selectAll;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.io.pubannotation.internal.model.PAAttribute;
import org.dkpro.core.io.pubannotation.internal.model.PADenotation;
import org.dkpro.core.io.pubannotation.internal.model.PADocument;
import org.dkpro.core.io.pubannotation.internal.model.PAIdentifiableObject;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class GenericDKPro2PubAnnotation
{
    private boolean writeNullAttributes = true;

    private Set<String> warnings = new LinkedHashSet<String>();

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

        List<FeatureStructure> relationFS = new ArrayList<>();

        Map<PAIdentifiableObject, FeatureStructure> eventFS = new LinkedHashMap<>();

        // Go through all the annotations but only handle the ones that have no references to
        // other annotations.
        for (FeatureStructure fs : selectAll(aJCas)) {
            // Skip document annotation
            if (fs == aJCas.getDocumentAnnotationFs()) {
                continue;
            }
            
            if (fs instanceof AnnotationFS) {
                warnings.add("Assuming annotation type [" + fs.getType().getName() + "] is span");
                
                AnnotationFS spanAnnotation = (AnnotationFS) fs;
                
                String id = Integer.toString(((CASImpl) cas).ll_getFSRef(spanAnnotation));
                PADenotation denotation = new PADenotation(id, fs.getType().getName(),
                        spanAnnotation.getBegin(), spanAnnotation.getEnd());
                
                aDoc.addDenotation(denotation);
                
                writeAttributes(aDoc, denotation, spanAnnotation);
            }
            else {
                warnings.add("Skipping annotation with type [" + fs.getType().getName() + "]");
            }
        }

        // Handle relations now since now we can resolve their targets to IDs.
//        for (FeatureStructure fs : relationFS) {
//            writeRelationAnnotation(doc, fs);
//        }
    }
    
    private void writeAttributes(PADocument aDoc, PAIdentifiableObject aSubject,
            FeatureStructure aFS)
    {
        for (Feature feat : aFS.getType().getFeatures()) {
            // Skip Sofa feature
            if (isInternalFeature(feat)) {
                continue;
            }
            
            // No need to write begin / end, they are already on the text annotation
            if (CAS.FEATURE_FULL_NAME_BEGIN.equals(feat.getName()) || 
                CAS.FEATURE_FULL_NAME_END.equals(feat.getName())) {
                continue;
            }
            
//            // No need to write link endpoints again, they are already on the relation annotation
//            RelationParam relParam = parsedRelationTypes.get(aFS.getType().getName());
//            if (relParam != null) {
//                if (relParam.getArg1().equals(feat.getShortName())
//                        || relParam.getArg2().equals(feat.getShortName())) {
//                    continue;
//                }
//            }
            
            if (feat.getRange().isPrimitive()) {
                writePrimitiveAttribute(aDoc, aSubject, aFS, feat);
            }
//            // The following warning is not relevant for event annotations because these render
//            // such features as slots.
//            else if (!(aAnno instanceof BratEventAnnotation)) {
//                warnings.add(
//                        "Unable to render feature [" + feat.getName() + "] with range ["
//                                + feat.getRange().getName() + "] as attribute");
//            }
        }
    }    
    private void writePrimitiveAttribute(PADocument aDoc, PAIdentifiableObject aSubject,
            FeatureStructure aFS, Feature feat)
    {
        String featureValue = aFS.getFeatureValueAsString(feat);

        // Do not write attributes with null values unless this is explicitly enabled
        if (featureValue == null && !writeNullAttributes) {
            return;
        }

        aDoc.addAttribute(new PAAttribute(aSubject.getId(), feat.getShortName(), featureValue));
    }
    
    private boolean isInternalFeature(Feature aFeature)
    {
        // https://issues.apache.org/jira/browse/UIMA-4565
        return "uima.cas.AnnotationBase:sofa".equals(aFeature.getName());
        // return CAS.FEATURE_FULL_NAME_SOFA.equals(aFeature.getName());
    }
    
    /**
     * Some feature values do not need to be registered or cannot be registered because brat does
     * not support them.
     */
    private boolean isValidFeatureValue(String aFeatureValue)
    {
        // https://github.com/nlplab/brat/issues/1149
        return !(aFeatureValue == null || aFeatureValue.length() == 0 || aFeatureValue.equals(","));
    }

    /**
     * Checks if the feature structure has non-default non-primitive properties.
     */
    private boolean hasNonPrimitiveFeatures(FeatureStructure aFS)
    {
        for (Feature f : aFS.getType().getFeatures()) {
            if (CAS.FEATURE_BASE_NAME_SOFA.equals(f.getShortName())) {
                continue;
            }
            
            if (!f.getRange().isPrimitive()) {
                return true;
            }
        }
        
        return false;
    }
}
