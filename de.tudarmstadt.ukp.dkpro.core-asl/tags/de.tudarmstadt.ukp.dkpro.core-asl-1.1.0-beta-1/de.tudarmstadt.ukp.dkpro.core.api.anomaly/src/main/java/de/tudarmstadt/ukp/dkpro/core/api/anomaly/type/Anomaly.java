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
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Sun Nov 21 13:25:15 CET 2010
 * XML source: /Users/bluefire/UKP/Workspaces/dkpro-primary/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.api.anomaly/src/main/resources/desc/type/Anomalies.xml
 * @generated */
public class Anomaly extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Anomaly.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Anomaly() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Anomaly(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Anomaly(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Anomaly(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: description

  /** getter for description - gets 
   * @generated */
  public String getDescription() {
    if (Anomaly_Type.featOkTst && ((Anomaly_Type)jcasType).casFeat_description == null)
      jcasType.jcas.throwFeatMissing("description", "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.Anomaly");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Anomaly_Type)jcasType).casFeatCode_description);}
    
  /** setter for description - sets  
   * @generated */
  public void setDescription(String v) {
    if (Anomaly_Type.featOkTst && ((Anomaly_Type)jcasType).casFeat_description == null)
      jcasType.jcas.throwFeatMissing("description", "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.Anomaly");
    jcasType.ll_cas.ll_setStringValue(addr, ((Anomaly_Type)jcasType).casFeatCode_description, v);}    
   
    
  //*--------------*
  //* Feature: suggestion

  /** getter for suggestion - gets 
   * @generated */
  public String getSuggestion() {
    if (Anomaly_Type.featOkTst && ((Anomaly_Type)jcasType).casFeat_suggestion == null)
      jcasType.jcas.throwFeatMissing("suggestion", "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.Anomaly");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Anomaly_Type)jcasType).casFeatCode_suggestion);}
    
  /** setter for suggestion - sets  
   * @generated */
  public void setSuggestion(String v) {
    if (Anomaly_Type.featOkTst && ((Anomaly_Type)jcasType).casFeat_suggestion == null)
      jcasType.jcas.throwFeatMissing("suggestion", "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.Anomaly");
    jcasType.ll_cas.ll_setStringValue(addr, ((Anomaly_Type)jcasType).casFeatCode_suggestion, v);}    
   
    
  //*--------------*
  //* Feature: category

  /** getter for category - gets 
   * @generated */
  public String getCategory() {
    if (Anomaly_Type.featOkTst && ((Anomaly_Type)jcasType).casFeat_category == null)
      jcasType.jcas.throwFeatMissing("category", "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.Anomaly");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Anomaly_Type)jcasType).casFeatCode_category);}
    
  /** setter for category - sets  
   * @generated */
  public void setCategory(String v) {
    if (Anomaly_Type.featOkTst && ((Anomaly_Type)jcasType).casFeat_category == null)
      jcasType.jcas.throwFeatMissing("category", "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.Anomaly");
    jcasType.ll_cas.ll_setStringValue(addr, ((Anomaly_Type)jcasType).casFeatCode_category, v);}    
  }

    
