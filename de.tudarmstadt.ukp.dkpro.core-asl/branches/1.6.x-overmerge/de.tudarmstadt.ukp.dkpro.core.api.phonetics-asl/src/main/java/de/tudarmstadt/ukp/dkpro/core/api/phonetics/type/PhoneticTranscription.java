

/* First created by JCasGen Thu Jun 13 11:42:05 CEST 2013 */
package de.tudarmstadt.ukp.dkpro.core.api.phonetics.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Thu Jun 13 11:42:05 CEST 2013
 * XML source: /home/zesch/workspace_new/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.api.phonetics-asl/src/main/resources/desc/type/PhoneticTranscription.xml
 * @generated */
public class PhoneticTranscription extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(PhoneticTranscription.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected PhoneticTranscription() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public PhoneticTranscription(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public PhoneticTranscription(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public PhoneticTranscription(JCas jcas, int begin, int end) {
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
  //* Feature: transcription

  /** getter for transcription - gets The actual transcription
   * @generated */
  public String getTranscription() {
    if (PhoneticTranscription_Type.featOkTst && ((PhoneticTranscription_Type)jcasType).casFeat_transcription == null) {
        jcasType.jcas.throwFeatMissing("transcription", "de.tudarmstadt.ukp.dkpro.core.api.phonetics.type.PhoneticTranscription");
    }
    return jcasType.ll_cas.ll_getStringValue(addr, ((PhoneticTranscription_Type)jcasType).casFeatCode_transcription);}
    
  /** setter for transcription - sets The actual transcription 
   * @generated */
  public void setTranscription(String v) {
    if (PhoneticTranscription_Type.featOkTst && ((PhoneticTranscription_Type)jcasType).casFeat_transcription == null) {
        jcasType.jcas.throwFeatMissing("transcription", "de.tudarmstadt.ukp.dkpro.core.api.phonetics.type.PhoneticTranscription");
    }
    jcasType.ll_cas.ll_setStringValue(addr, ((PhoneticTranscription_Type)jcasType).casFeatCode_transcription, v);}    
   
    
  //*--------------*
  //* Feature: name

  /** getter for name - gets The name of the transcription process that was used
   * @generated */
  public String getName() {
    if (PhoneticTranscription_Type.featOkTst && ((PhoneticTranscription_Type)jcasType).casFeat_name == null) {
        jcasType.jcas.throwFeatMissing("name", "de.tudarmstadt.ukp.dkpro.core.api.phonetics.type.PhoneticTranscription");
    }
    return jcasType.ll_cas.ll_getStringValue(addr, ((PhoneticTranscription_Type)jcasType).casFeatCode_name);}
    
  /** setter for name - sets The name of the transcription process that was used 
   * @generated */
  public void setName(String v) {
    if (PhoneticTranscription_Type.featOkTst && ((PhoneticTranscription_Type)jcasType).casFeat_name == null) {
        jcasType.jcas.throwFeatMissing("name", "de.tudarmstadt.ukp.dkpro.core.api.phonetics.type.PhoneticTranscription");
    }
    jcasType.ll_cas.ll_setStringValue(addr, ((PhoneticTranscription_Type)jcasType).casFeatCode_name, v);}    
  }

    