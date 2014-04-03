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
/* First created by JCasGen Sun Nov 21 13:25:15 CET 2010 */
package de.tudarmstadt.ukp.dkpro.core.api.anomaly.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Tue Aug 09 17:57:32 CEST 2011
 * @generated */
public class Anomaly_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Anomaly_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Anomaly_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Anomaly(addr, Anomaly_Type.this);
  			   Anomaly_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Anomaly(addr, Anomaly_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Anomaly.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.Anomaly");
 
  /** @generated */
  final Feature casFeat_description;
  /** @generated */
  final int     casFeatCode_description;
  /** @generated */ 
  public String getDescription(int addr) {
        if (featOkTst && casFeat_description == null)
      jcas.throwFeatMissing("description", "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.Anomaly");
    return ll_cas.ll_getStringValue(addr, casFeatCode_description);
  }
  /** @generated */    
  public void setDescription(int addr, String v) {
        if (featOkTst && casFeat_description == null)
      jcas.throwFeatMissing("description", "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.Anomaly");
    ll_cas.ll_setStringValue(addr, casFeatCode_description, v);}
    
  
 
  /** @generated */
  final Feature casFeat_suggestions;
  /** @generated */
  final int     casFeatCode_suggestions;
  /** @generated */ 
  public int getSuggestions(int addr) {
        if (featOkTst && casFeat_suggestions == null)
      jcas.throwFeatMissing("suggestions", "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.Anomaly");
    return ll_cas.ll_getRefValue(addr, casFeatCode_suggestions);
  }
  /** @generated */    
  public void setSuggestions(int addr, int v) {
        if (featOkTst && casFeat_suggestions == null)
      jcas.throwFeatMissing("suggestions", "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.Anomaly");
    ll_cas.ll_setRefValue(addr, casFeatCode_suggestions, v);}
    
   /** @generated */
  public int getSuggestions(int addr, int i) {
        if (featOkTst && casFeat_suggestions == null)
      jcas.throwFeatMissing("suggestions", "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.Anomaly");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_suggestions), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_suggestions), i);
  return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_suggestions), i);
  }
   
  /** @generated */ 
  public void setSuggestions(int addr, int i, int v) {
        if (featOkTst && casFeat_suggestions == null)
      jcas.throwFeatMissing("suggestions", "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.Anomaly");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_suggestions), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_suggestions), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_suggestions), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_category;
  /** @generated */
  final int     casFeatCode_category;
  /** @generated */ 
  public String getCategory(int addr) {
        if (featOkTst && casFeat_category == null)
      jcas.throwFeatMissing("category", "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.Anomaly");
    return ll_cas.ll_getStringValue(addr, casFeatCode_category);
  }
  /** @generated */    
  public void setCategory(int addr, String v) {
        if (featOkTst && casFeat_category == null)
      jcas.throwFeatMissing("category", "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.Anomaly");
    ll_cas.ll_setStringValue(addr, casFeatCode_category, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Anomaly_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_description = jcas.getRequiredFeatureDE(casType, "description", "uima.cas.String", featOkTst);
    casFeatCode_description  = (null == casFeat_description) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_description).getCode();

 
    casFeat_suggestions = jcas.getRequiredFeatureDE(casType, "suggestions", "uima.cas.FSArray", featOkTst);
    casFeatCode_suggestions  = (null == casFeat_suggestions) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_suggestions).getCode();

 
    casFeat_category = jcas.getRequiredFeatureDE(casType, "category", "uima.cas.String", featOkTst);
    casFeatCode_category  = (null == casFeat_category) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_category).getCode();

  }
}



    
