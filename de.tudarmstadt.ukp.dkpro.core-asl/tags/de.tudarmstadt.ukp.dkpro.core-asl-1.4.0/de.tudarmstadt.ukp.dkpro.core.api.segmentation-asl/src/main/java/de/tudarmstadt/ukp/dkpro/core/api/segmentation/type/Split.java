

/* First created by JCasGen Sat Aug 04 18:47:41 CEST 2012 */
package de.tudarmstadt.ukp.dkpro.core.api.segmentation.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;


/** A part of a compound word.
 * Updated by JCasGen Sat Aug 04 18:48:32 CEST 2012
 * XML source: /Users/bluefire/UKP/Workspaces/dkpro-juno/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.api.segmentation-asl/src/main/resources/desc/type/Segmentation.xml
 * @generated */
public class Split extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Split.class);
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
  protected Split() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Split(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Split(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Split(JCas jcas, int begin, int end) {
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
  //* Feature: category

  /** getter for category - gets The type of morpheme. (morpheme or linking-morpheme)
   * @generated */
  public String getCategory() {
    if (Split_Type.featOkTst && ((Split_Type)jcasType).casFeat_category == null)
      jcasType.jcas.throwFeatMissing("category", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Split");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Split_Type)jcasType).casFeatCode_category);}
    
  /** setter for category - sets The type of morpheme. (morpheme or linking-morpheme) 
   * @generated */
  public void setCategory(String v) {
    if (Split_Type.featOkTst && ((Split_Type)jcasType).casFeat_category == null)
      jcasType.jcas.throwFeatMissing("category", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Split");
    jcasType.ll_cas.ll_setStringValue(addr, ((Split_Type)jcasType).casFeatCode_category, v);}    
   
    
  //*--------------*
  //* Feature: splits

  /** getter for splits - gets Sub-splits of the current split.
   * @generated */
  public FSArray getSplits() {
    if (Split_Type.featOkTst && ((Split_Type)jcasType).casFeat_splits == null)
      jcasType.jcas.throwFeatMissing("splits", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Split");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Split_Type)jcasType).casFeatCode_splits)));}
    
  /** setter for splits - sets Sub-splits of the current split. 
   * @generated */
  public void setSplits(FSArray v) {
    if (Split_Type.featOkTst && ((Split_Type)jcasType).casFeat_splits == null)
      jcasType.jcas.throwFeatMissing("splits", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Split");
    jcasType.ll_cas.ll_setRefValue(addr, ((Split_Type)jcasType).casFeatCode_splits, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for splits - gets an indexed value - Sub-splits of the current split.
   * @generated */
  public Split getSplits(int i) {
    if (Split_Type.featOkTst && ((Split_Type)jcasType).casFeat_splits == null)
      jcasType.jcas.throwFeatMissing("splits", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Split");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Split_Type)jcasType).casFeatCode_splits), i);
    return (Split)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Split_Type)jcasType).casFeatCode_splits), i)));}

  /** indexed setter for splits - sets an indexed value - Sub-splits of the current split.
   * @generated */
  public void setSplits(int i, Split v) { 
    if (Split_Type.featOkTst && ((Split_Type)jcasType).casFeat_splits == null)
      jcasType.jcas.throwFeatMissing("splits", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Split");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Split_Type)jcasType).casFeatCode_splits), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Split_Type)jcasType).casFeatCode_splits), i, jcasType.ll_cas.ll_getFSRef(v));}
  }

    