

/* First created by JCasGen Sat Aug 04 18:47:40 CEST 2012 */
package de.tudarmstadt.ukp.dkpro.core.api.segmentation.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Sat Aug 04 18:48:32 CEST 2012
 * XML source: /Users/bluefire/UKP/Workspaces/dkpro-juno/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.api.segmentation-asl/src/main/resources/desc/type/Segmentation.xml
 * @generated */
public class Compound extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Compound.class);
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
  protected Compound() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Compound(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Compound(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Compound(JCas jcas, int begin, int end) {
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
  //* Feature: splits

  /** getter for splits - gets A word that can be decomposed into different parts.
   * @generated */
  public FSArray getSplits() {
    if (Compound_Type.featOkTst && ((Compound_Type)jcasType).casFeat_splits == null)
      jcasType.jcas.throwFeatMissing("splits", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Compound_Type)jcasType).casFeatCode_splits)));}
    
  /** setter for splits - sets A word that can be decomposed into different parts. 
   * @generated */
  public void setSplits(FSArray v) {
    if (Compound_Type.featOkTst && ((Compound_Type)jcasType).casFeat_splits == null)
      jcasType.jcas.throwFeatMissing("splits", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound");
    jcasType.ll_cas.ll_setRefValue(addr, ((Compound_Type)jcasType).casFeatCode_splits, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for splits - gets an indexed value - A word that can be decomposed into different parts.
   * @generated */
  public Split getSplits(int i) {
    if (Compound_Type.featOkTst && ((Compound_Type)jcasType).casFeat_splits == null)
      jcasType.jcas.throwFeatMissing("splits", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Compound_Type)jcasType).casFeatCode_splits), i);
    return (Split)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Compound_Type)jcasType).casFeatCode_splits), i)));}

  /** indexed setter for splits - sets an indexed value - A word that can be decomposed into different parts.
   * @generated */
  public void setSplits(int i, Split v) { 
    if (Compound_Type.featOkTst && ((Compound_Type)jcasType).casFeat_splits == null)
      jcasType.jcas.throwFeatMissing("splits", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Compound_Type)jcasType).casFeatCode_splits), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Compound_Type)jcasType).casFeatCode_splits), i, jcasType.ll_cas.ll_getFSRef(v));}
  }

    