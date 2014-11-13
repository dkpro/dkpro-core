

/* First created by JCasGen Wed Dec 18 10:03:08 CET 2013 */
package de.tudarmstadt.ukp.dkpro.core.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Wed Dec 18 10:03:08 CET 2013
 * XML source: /home/zesch/workspace_new/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.readability-asl/src/main/resources/desc/type/ReadabilityScore.xml
 * @generated */
public class ReadabilityScore extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(ReadabilityScore.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated  */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected ReadabilityScore() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public ReadabilityScore(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public ReadabilityScore(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public ReadabilityScore(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: measureName

  /** getter for measureName - gets 
   * @generated */
  public String getMeasureName() {
    if (ReadabilityScore_Type.featOkTst && ((ReadabilityScore_Type)jcasType).casFeat_measureName == null)
      jcasType.jcas.throwFeatMissing("measureName", "de.tudarmstadt.ukp.dkpro.core.type.ReadabilityScore");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ReadabilityScore_Type)jcasType).casFeatCode_measureName);}
    
  /** setter for measureName - sets  
   * @generated */
  public void setMeasureName(String v) {
    if (ReadabilityScore_Type.featOkTst && ((ReadabilityScore_Type)jcasType).casFeat_measureName == null)
      jcasType.jcas.throwFeatMissing("measureName", "de.tudarmstadt.ukp.dkpro.core.type.ReadabilityScore");
    jcasType.ll_cas.ll_setStringValue(addr, ((ReadabilityScore_Type)jcasType).casFeatCode_measureName, v);}    
   
    
  //*--------------*
  //* Feature: score

  /** getter for score - gets 
   * @generated */
  public double getScore() {
    if (ReadabilityScore_Type.featOkTst && ((ReadabilityScore_Type)jcasType).casFeat_score == null)
      jcasType.jcas.throwFeatMissing("score", "de.tudarmstadt.ukp.dkpro.core.type.ReadabilityScore");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((ReadabilityScore_Type)jcasType).casFeatCode_score);}
    
  /** setter for score - sets  
   * @generated */
  public void setScore(double v) {
    if (ReadabilityScore_Type.featOkTst && ((ReadabilityScore_Type)jcasType).casFeat_score == null)
      jcasType.jcas.throwFeatMissing("score", "de.tudarmstadt.ukp.dkpro.core.type.ReadabilityScore");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((ReadabilityScore_Type)jcasType).casFeatCode_score, v);}    
  }

    