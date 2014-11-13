
/* First created by JCasGen Mon Oct 20 16:34:13 CEST 2014 */
package de.tudarmstadt.ukp.dkpro.core.mallet.type;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Mon Oct 20 16:34:13 CEST 2014
 * @generated */
public class TopicDistribution_Type extends Annotation_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (TopicDistribution_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = TopicDistribution_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new TopicDistribution(addr, TopicDistribution_Type.this);
  			   TopicDistribution_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new TopicDistribution(addr, TopicDistribution_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = TopicDistribution.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution");
 
  /** @generated */
  final Feature casFeat_TopicProportions;
  /** @generated */
  final int     casFeatCode_TopicProportions;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getTopicProportions(int addr) {
        if (featOkTst && casFeat_TopicProportions == null)
      jcas.throwFeatMissing("TopicProportions", "de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution");
    return ll_cas.ll_getRefValue(addr, casFeatCode_TopicProportions);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTopicProportions(int addr, int v) {
        if (featOkTst && casFeat_TopicProportions == null)
      jcas.throwFeatMissing("TopicProportions", "de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution");
    ll_cas.ll_setRefValue(addr, casFeatCode_TopicProportions, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public double getTopicProportions(int addr, int i) {
        if (featOkTst && casFeat_TopicProportions == null)
      jcas.throwFeatMissing("TopicProportions", "de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getDoubleArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_TopicProportions), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_TopicProportions), i);
	return ll_cas.ll_getDoubleArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_TopicProportions), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setTopicProportions(int addr, int i, double v) {
        if (featOkTst && casFeat_TopicProportions == null)
      jcas.throwFeatMissing("TopicProportions", "de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution");
    if (lowLevelTypeChecks)
      ll_cas.ll_setDoubleArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_TopicProportions), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_TopicProportions), i);
    ll_cas.ll_setDoubleArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_TopicProportions), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_TopicAssignment;
  /** @generated */
  final int     casFeatCode_TopicAssignment;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getTopicAssignment(int addr) {
        if (featOkTst && casFeat_TopicAssignment == null)
      jcas.throwFeatMissing("TopicAssignment", "de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution");
    return ll_cas.ll_getRefValue(addr, casFeatCode_TopicAssignment);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTopicAssignment(int addr, int v) {
        if (featOkTst && casFeat_TopicAssignment == null)
      jcas.throwFeatMissing("TopicAssignment", "de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution");
    ll_cas.ll_setRefValue(addr, casFeatCode_TopicAssignment, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public int getTopicAssignment(int addr, int i) {
        if (featOkTst && casFeat_TopicAssignment == null)
      jcas.throwFeatMissing("TopicAssignment", "de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getIntArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_TopicAssignment), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_TopicAssignment), i);
	return ll_cas.ll_getIntArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_TopicAssignment), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setTopicAssignment(int addr, int i, int v) {
        if (featOkTst && casFeat_TopicAssignment == null)
      jcas.throwFeatMissing("TopicAssignment", "de.tudarmstadt.ukp.dkpro.core.mallet.type.TopicDistribution");
    if (lowLevelTypeChecks)
      ll_cas.ll_setIntArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_TopicAssignment), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_TopicAssignment), i);
    ll_cas.ll_setIntArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_TopicAssignment), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public TopicDistribution_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_TopicProportions = jcas.getRequiredFeatureDE(casType, "TopicProportions", "uima.cas.DoubleArray", featOkTst);
    casFeatCode_TopicProportions  = (null == casFeat_TopicProportions) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_TopicProportions).getCode();

 
    casFeat_TopicAssignment = jcas.getRequiredFeatureDE(casType, "TopicAssignment", "uima.cas.IntegerArray", featOkTst);
    casFeatCode_TopicAssignment  = (null == casFeat_TopicAssignment) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_TopicAssignment).getCode();

  }
}



    