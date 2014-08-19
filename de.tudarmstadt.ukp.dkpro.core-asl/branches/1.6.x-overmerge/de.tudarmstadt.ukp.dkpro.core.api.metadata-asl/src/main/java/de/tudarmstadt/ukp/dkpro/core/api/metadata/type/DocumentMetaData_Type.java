/*******************************************************************************
 * Copyright 2010
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
 ******************************************************************************/
/* First created by JCasGen Mon Nov 08 23:55:50 CET 2010 */
package de.tudarmstadt.ukp.dkpro.core.api.metadata.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.DocumentAnnotation_Type;

/** 
 * Updated by JCasGen Sun Nov 21 13:28:49 CET 2010
 * @generated */
public class DocumentMetaData_Type extends DocumentAnnotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (DocumentMetaData_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = DocumentMetaData_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new DocumentMetaData(addr, DocumentMetaData_Type.this);
  			   DocumentMetaData_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new DocumentMetaData(addr, DocumentMetaData_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = DocumentMetaData.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData");
 
  /** @generated */
  final Feature casFeat_documentTitle;
  /** @generated */
  final int     casFeatCode_documentTitle;
  /** @generated */ 
  public String getDocumentTitle(int addr) {
        if (featOkTst && casFeat_documentTitle == null)
      jcas.throwFeatMissing("documentTitle", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData");
    return ll_cas.ll_getStringValue(addr, casFeatCode_documentTitle);
  }
  /** @generated */    
  public void setDocumentTitle(int addr, String v) {
        if (featOkTst && casFeat_documentTitle == null)
      jcas.throwFeatMissing("documentTitle", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData");
    ll_cas.ll_setStringValue(addr, casFeatCode_documentTitle, v);}
    
  
 
  /** @generated */
  final Feature casFeat_documentId;
  /** @generated */
  final int     casFeatCode_documentId;
  /** @generated */ 
  public String getDocumentId(int addr) {
        if (featOkTst && casFeat_documentId == null)
      jcas.throwFeatMissing("documentId", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData");
    return ll_cas.ll_getStringValue(addr, casFeatCode_documentId);
  }
  /** @generated */    
  public void setDocumentId(int addr, String v) {
        if (featOkTst && casFeat_documentId == null)
      jcas.throwFeatMissing("documentId", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData");
    ll_cas.ll_setStringValue(addr, casFeatCode_documentId, v);}
    
  
 
  /** @generated */
  final Feature casFeat_documentUri;
  /** @generated */
  final int     casFeatCode_documentUri;
  /** @generated */ 
  public String getDocumentUri(int addr) {
        if (featOkTst && casFeat_documentUri == null)
      jcas.throwFeatMissing("documentUri", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData");
    return ll_cas.ll_getStringValue(addr, casFeatCode_documentUri);
  }
  /** @generated */    
  public void setDocumentUri(int addr, String v) {
        if (featOkTst && casFeat_documentUri == null)
      jcas.throwFeatMissing("documentUri", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData");
    ll_cas.ll_setStringValue(addr, casFeatCode_documentUri, v);}
    
  
 
  /** @generated */
  final Feature casFeat_collectionId;
  /** @generated */
  final int     casFeatCode_collectionId;
  /** @generated */ 
  public String getCollectionId(int addr) {
        if (featOkTst && casFeat_collectionId == null)
      jcas.throwFeatMissing("collectionId", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData");
    return ll_cas.ll_getStringValue(addr, casFeatCode_collectionId);
  }
  /** @generated */    
  public void setCollectionId(int addr, String v) {
        if (featOkTst && casFeat_collectionId == null)
      jcas.throwFeatMissing("collectionId", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData");
    ll_cas.ll_setStringValue(addr, casFeatCode_collectionId, v);}
    
  
 
  /** @generated */
  final Feature casFeat_documentBaseUri;
  /** @generated */
  final int     casFeatCode_documentBaseUri;
  /** @generated */ 
  public String getDocumentBaseUri(int addr) {
        if (featOkTst && casFeat_documentBaseUri == null)
      jcas.throwFeatMissing("documentBaseUri", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData");
    return ll_cas.ll_getStringValue(addr, casFeatCode_documentBaseUri);
  }
  /** @generated */    
  public void setDocumentBaseUri(int addr, String v) {
        if (featOkTst && casFeat_documentBaseUri == null)
      jcas.throwFeatMissing("documentBaseUri", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData");
    ll_cas.ll_setStringValue(addr, casFeatCode_documentBaseUri, v);}
    
  
 
  /** @generated */
  final Feature casFeat_isLastSegment;
  /** @generated */
  final int     casFeatCode_isLastSegment;
  /** @generated */ 
  public boolean getIsLastSegment(int addr) {
        if (featOkTst && casFeat_isLastSegment == null)
      jcas.throwFeatMissing("isLastSegment", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_isLastSegment);
  }
  /** @generated */    
  public void setIsLastSegment(int addr, boolean v) {
        if (featOkTst && casFeat_isLastSegment == null)
      jcas.throwFeatMissing("isLastSegment", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_isLastSegment, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public DocumentMetaData_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_documentTitle = jcas.getRequiredFeatureDE(casType, "documentTitle", "uima.cas.String", featOkTst);
    casFeatCode_documentTitle  = (null == casFeat_documentTitle) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_documentTitle).getCode();

 
    casFeat_documentId = jcas.getRequiredFeatureDE(casType, "documentId", "uima.cas.String", featOkTst);
    casFeatCode_documentId  = (null == casFeat_documentId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_documentId).getCode();

 
    casFeat_documentUri = jcas.getRequiredFeatureDE(casType, "documentUri", "uima.cas.String", featOkTst);
    casFeatCode_documentUri  = (null == casFeat_documentUri) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_documentUri).getCode();

 
    casFeat_collectionId = jcas.getRequiredFeatureDE(casType, "collectionId", "uima.cas.String", featOkTst);
    casFeatCode_collectionId  = (null == casFeat_collectionId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_collectionId).getCode();

 
    casFeat_documentBaseUri = jcas.getRequiredFeatureDE(casType, "documentBaseUri", "uima.cas.String", featOkTst);
    casFeatCode_documentBaseUri  = (null == casFeat_documentBaseUri) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_documentBaseUri).getCode();

 
    casFeat_isLastSegment = jcas.getRequiredFeatureDE(casType, "isLastSegment", "uima.cas.Boolean", featOkTst);
    casFeatCode_isLastSegment  = (null == casFeat_isLastSegment) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_isLastSegment).getCode();

  }
}



    
