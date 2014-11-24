
/* First created by JCasGen Sat Nov 01 22:21:22 CET 2014 */
package de.tudarmstadt.ukp.dkpro.core.api.segmentation.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** This type represents a part of a decompounding word. A Split can be either a CompoundPart or a LinkingMorpheme.
 * Updated by JCasGen Sat Nov 01 22:21:22 CET 2014
 * @generated */
public class Split_Type extends Annotation_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Split_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Split_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Split(addr, Split_Type.this);
  			   Split_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Split(addr, Split_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Split.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Split");
 
  /** @generated */
  final Feature casFeat_splits;
  /** @generated */
  final int     casFeatCode_splits;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getSplits(int addr) {
        if (featOkTst && casFeat_splits == null)
      jcas.throwFeatMissing("splits", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Split");
    return ll_cas.ll_getRefValue(addr, casFeatCode_splits);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSplits(int addr, int v) {
        if (featOkTst && casFeat_splits == null)
      jcas.throwFeatMissing("splits", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Split");
    ll_cas.ll_setRefValue(addr, casFeatCode_splits, v);}
    
   /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public int getSplits(int addr, int i) {
        if (featOkTst && casFeat_splits == null)
      jcas.throwFeatMissing("splits", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Split");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_splits), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_splits), i);
	return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_splits), i);
  }
   
  /** @generated
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setSplits(int addr, int i, int v) {
        if (featOkTst && casFeat_splits == null)
      jcas.throwFeatMissing("splits", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Split");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_splits), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_splits), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_splits), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Split_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_splits = jcas.getRequiredFeatureDE(casType, "splits", "uima.cas.FSArray", featOkTst);
    casFeatCode_splits  = (null == casFeat_splits) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_splits).getCode();

  }
}



    