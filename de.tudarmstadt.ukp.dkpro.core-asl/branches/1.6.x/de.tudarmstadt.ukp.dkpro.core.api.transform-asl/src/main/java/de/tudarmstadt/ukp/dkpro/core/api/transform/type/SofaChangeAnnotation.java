

/* First created by JCasGen Sun Nov 21 13:11:41 CET 2010 */
package de.tudarmstadt.ukp.dkpro.core.api.transform.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** Describes a change to the Sofa.
 * Updated by JCasGen Sun Nov 21 13:11:41 CET 2010
 * XML source: /Users/bluefire/UKP/Workspaces/dkpro-primary/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.api.transform/src/main/resources/desc/type/SofaChangeAnnotation.xml
 * @generated */
public class SofaChangeAnnotation extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(SofaChangeAnnotation.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected SofaChangeAnnotation() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public SofaChangeAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public SofaChangeAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public SofaChangeAnnotation(JCas jcas, int begin, int end) {
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
  //* Feature: value

  /** getter for value - gets In case of an "insert" or "replace" operation, this feature indicates the value to be inserted or replaced.
   * @generated */
  public String getValue() {
    if (SofaChangeAnnotation_Type.featOkTst && ((SofaChangeAnnotation_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SofaChangeAnnotation_Type)jcasType).casFeatCode_value);}
    
  /** setter for value - sets In case of an "insert" or "replace" operation, this feature indicates the value to be inserted or replaced. 
   * @generated */
  public void setValue(String v) {
    if (SofaChangeAnnotation_Type.featOkTst && ((SofaChangeAnnotation_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((SofaChangeAnnotation_Type)jcasType).casFeatCode_value, v);}    
   
    
  //*--------------*
  //* Feature: operation

  /** getter for operation - gets Operation to perform: "insert", "replace", "delete"
   * @generated */
  public String getOperation() {
    if (SofaChangeAnnotation_Type.featOkTst && ((SofaChangeAnnotation_Type)jcasType).casFeat_operation == null)
      jcasType.jcas.throwFeatMissing("operation", "de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SofaChangeAnnotation_Type)jcasType).casFeatCode_operation);}
    
  /** setter for operation - sets Operation to perform: "insert", "replace", "delete" 
   * @generated */
  public void setOperation(String v) {
    if (SofaChangeAnnotation_Type.featOkTst && ((SofaChangeAnnotation_Type)jcasType).casFeat_operation == null)
      jcasType.jcas.throwFeatMissing("operation", "de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((SofaChangeAnnotation_Type)jcasType).casFeatCode_operation, v);}    
   
    
  //*--------------*
  //* Feature: reason

  /** getter for reason - gets The reason for the change.
   * @generated */
  public String getReason() {
    if (SofaChangeAnnotation_Type.featOkTst && ((SofaChangeAnnotation_Type)jcasType).casFeat_reason == null)
      jcasType.jcas.throwFeatMissing("reason", "de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SofaChangeAnnotation_Type)jcasType).casFeatCode_reason);}
    
  /** setter for reason - sets The reason for the change. 
   * @generated */
  public void setReason(String v) {
    if (SofaChangeAnnotation_Type.featOkTst && ((SofaChangeAnnotation_Type)jcasType).casFeat_reason == null)
      jcasType.jcas.throwFeatMissing("reason", "de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((SofaChangeAnnotation_Type)jcasType).casFeatCode_reason, v);}    
  }

    