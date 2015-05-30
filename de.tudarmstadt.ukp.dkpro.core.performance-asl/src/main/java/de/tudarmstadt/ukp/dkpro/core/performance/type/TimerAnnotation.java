

/* First created by JCasGen Sat May 30 20:50:45 CEST 2015 */
package de.tudarmstadt.ukp.dkpro.core.performance.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Sat May 30 20:50:45 CEST 2015
 * XML source: /Users/zesch/git/dkpro/dkpro-core/de.tudarmstadt.ukp.dkpro.core.performance-asl/src/main/resources/desc/type/TimerAnnotation.xml
 * @generated */
public class TimerAnnotation extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(TimerAnnotation.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected TimerAnnotation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public TimerAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public TimerAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public TimerAnnotation(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: startTime

  /** getter for startTime - gets 
   * @generated
   * @return value of the feature 
   */
  public long getStartTime() {
    if (TimerAnnotation_Type.featOkTst && ((TimerAnnotation_Type)jcasType).casFeat_startTime == null)
      jcasType.jcas.throwFeatMissing("startTime", "de.tudarmstadt.ukp.dkpro.core.performance.type.TimerAnnotation");
    return jcasType.ll_cas.ll_getLongValue(addr, ((TimerAnnotation_Type)jcasType).casFeatCode_startTime);}
    
  /** setter for startTime - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setStartTime(long v) {
    if (TimerAnnotation_Type.featOkTst && ((TimerAnnotation_Type)jcasType).casFeat_startTime == null)
      jcasType.jcas.throwFeatMissing("startTime", "de.tudarmstadt.ukp.dkpro.core.performance.type.TimerAnnotation");
    jcasType.ll_cas.ll_setLongValue(addr, ((TimerAnnotation_Type)jcasType).casFeatCode_startTime, v);}    
   
    
  //*--------------*
  //* Feature: endTime

  /** getter for endTime - gets 
   * @generated
   * @return value of the feature 
   */
  public long getEndTime() {
    if (TimerAnnotation_Type.featOkTst && ((TimerAnnotation_Type)jcasType).casFeat_endTime == null)
      jcasType.jcas.throwFeatMissing("endTime", "de.tudarmstadt.ukp.dkpro.core.performance.type.TimerAnnotation");
    return jcasType.ll_cas.ll_getLongValue(addr, ((TimerAnnotation_Type)jcasType).casFeatCode_endTime);}
    
  /** setter for endTime - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setEndTime(long v) {
    if (TimerAnnotation_Type.featOkTst && ((TimerAnnotation_Type)jcasType).casFeat_endTime == null)
      jcasType.jcas.throwFeatMissing("endTime", "de.tudarmstadt.ukp.dkpro.core.performance.type.TimerAnnotation");
    jcasType.ll_cas.ll_setLongValue(addr, ((TimerAnnotation_Type)jcasType).casFeatCode_endTime, v);}    
   
    
  //*--------------*
  //* Feature: name

  /** getter for name - gets The name of the timer.
Used to automatically determine whether this is an upstream or downstream timer.
   * @generated
   * @return value of the feature 
   */
  public String getName() {
    if (TimerAnnotation_Type.featOkTst && ((TimerAnnotation_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "de.tudarmstadt.ukp.dkpro.core.performance.type.TimerAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((TimerAnnotation_Type)jcasType).casFeatCode_name);}
    
  /** setter for name - sets The name of the timer.
Used to automatically determine whether this is an upstream or downstream timer. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setName(String v) {
    if (TimerAnnotation_Type.featOkTst && ((TimerAnnotation_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "de.tudarmstadt.ukp.dkpro.core.performance.type.TimerAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((TimerAnnotation_Type)jcasType).casFeatCode_name, v);}    
  }

    