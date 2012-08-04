

/* First created by JCasGen Thu Dec 09 22:13:11 CET 2010 */
package de.tudarmstadt.ukp.dkpro.core.api.segmentation.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Sat Aug 04 18:48:32 CEST 2012
 * XML source: /Users/bluefire/UKP/Workspaces/dkpro-juno/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.api.segmentation-asl/src/main/resources/desc/type/Segmentation.xml
 * @generated */
public class NGram extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(NGram.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected NGram() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public NGram(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public NGram(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public NGram(JCas jcas, int begin, int end) {
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
  //* Feature: text

  /** getter for text - gets 
   * @generated */
  public String getText() {
    if (NGram_Type.featOkTst && ((NGram_Type)jcasType).casFeat_text == null)
      jcasType.jcas.throwFeatMissing("text", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.NGram");
    return jcasType.ll_cas.ll_getStringValue(addr, ((NGram_Type)jcasType).casFeatCode_text);}
    
  /** setter for text - sets  
   * @generated */
  public void setText(String v) {
    if (NGram_Type.featOkTst && ((NGram_Type)jcasType).casFeat_text == null)
      jcasType.jcas.throwFeatMissing("text", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.NGram");
    jcasType.ll_cas.ll_setStringValue(addr, ((NGram_Type)jcasType).casFeatCode_text, v);}    
  }

    