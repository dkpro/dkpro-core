

/* First created by JCasGen Mon Oct 20 16:34:13 CEST 2014 */
package de.tudarmstadt.ukp.dkpro.core.mallet.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.DoubleArray;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Mon Oct 20 16:34:13 CEST 2014
 * XML source: /home/schnober/workspace/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.mallet/src/main/resources/TopicDistribution.xml
 * @generated */
public class TopicDistribution extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(TopicDistribution.class);
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
  protected TopicDistribution() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public TopicDistribution(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public TopicDistribution(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public TopicDistribution(JCas jcas, int begin, int end) {
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
  //* Feature: TopicProportions

  /** getter for TopicProportions - gets Each topic's proportion in the document.
   * @generated
   * @return value of the feature 
   */
  public DoubleArray getTopicProportions() {
    if (TopicDistribution_Type.featOkTst && ((TopicDistribution_Type)jcasType).casFeat_TopicProportions == null)
      jcasType.jcas.throwFeatMissing("TopicProportions", "de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution");
    return (DoubleArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((TopicDistribution_Type)jcasType).casFeatCode_TopicProportions)));}
    
  /** setter for TopicProportions - sets Each topic's proportion in the document. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setTopicProportions(DoubleArray v) {
    if (TopicDistribution_Type.featOkTst && ((TopicDistribution_Type)jcasType).casFeat_TopicProportions == null)
      jcasType.jcas.throwFeatMissing("TopicProportions", "de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution");
    jcasType.ll_cas.ll_setRefValue(addr, ((TopicDistribution_Type)jcasType).casFeatCode_TopicProportions, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for TopicProportions - gets an indexed value - Each topic's proportion in the document.
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public double getTopicProportions(int i) {
    if (TopicDistribution_Type.featOkTst && ((TopicDistribution_Type)jcasType).casFeat_TopicProportions == null)
      jcasType.jcas.throwFeatMissing("TopicProportions", "de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((TopicDistribution_Type)jcasType).casFeatCode_TopicProportions), i);
    return jcasType.ll_cas.ll_getDoubleArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((TopicDistribution_Type)jcasType).casFeatCode_TopicProportions), i);}

  /** indexed setter for TopicProportions - sets an indexed value - Each topic's proportion in the document.
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setTopicProportions(int i, double v) { 
    if (TopicDistribution_Type.featOkTst && ((TopicDistribution_Type)jcasType).casFeat_TopicProportions == null)
      jcasType.jcas.throwFeatMissing("TopicProportions", "de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((TopicDistribution_Type)jcasType).casFeatCode_TopicProportions), i);
    jcasType.ll_cas.ll_setDoubleArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((TopicDistribution_Type)jcasType).casFeatCode_TopicProportions), i, v);}
   
    
  //*--------------*
  //* Feature: TopicAssignment

  /** getter for TopicAssignment - gets Pointers to topics the document has been assigned to.
   * @generated
   * @return value of the feature 
   */
  public IntegerArray getTopicAssignment() {
    if (TopicDistribution_Type.featOkTst && ((TopicDistribution_Type)jcasType).casFeat_TopicAssignment == null)
      jcasType.jcas.throwFeatMissing("TopicAssignment", "de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution");
    return (IntegerArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((TopicDistribution_Type)jcasType).casFeatCode_TopicAssignment)));}
    
  /** setter for TopicAssignment - sets Pointers to topics the document has been assigned to. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setTopicAssignment(IntegerArray v) {
    if (TopicDistribution_Type.featOkTst && ((TopicDistribution_Type)jcasType).casFeat_TopicAssignment == null)
      jcasType.jcas.throwFeatMissing("TopicAssignment", "de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution");
    jcasType.ll_cas.ll_setRefValue(addr, ((TopicDistribution_Type)jcasType).casFeatCode_TopicAssignment, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for TopicAssignment - gets an indexed value - Pointers to topics the document has been assigned to.
   * @generated
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public int getTopicAssignment(int i) {
    if (TopicDistribution_Type.featOkTst && ((TopicDistribution_Type)jcasType).casFeat_TopicAssignment == null)
      jcasType.jcas.throwFeatMissing("TopicAssignment", "de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((TopicDistribution_Type)jcasType).casFeatCode_TopicAssignment), i);
    return jcasType.ll_cas.ll_getIntArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((TopicDistribution_Type)jcasType).casFeatCode_TopicAssignment), i);}

  /** indexed setter for TopicAssignment - sets an indexed value - Pointers to topics the document has been assigned to.
   * @generated
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setTopicAssignment(int i, int v) { 
    if (TopicDistribution_Type.featOkTst && ((TopicDistribution_Type)jcasType).casFeat_TopicAssignment == null)
      jcasType.jcas.throwFeatMissing("TopicAssignment", "de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((TopicDistribution_Type)jcasType).casFeatCode_TopicAssignment), i);
    jcasType.ll_cas.ll_setIntArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((TopicDistribution_Type)jcasType).casFeatCode_TopicAssignment), i, v);}
  }

    