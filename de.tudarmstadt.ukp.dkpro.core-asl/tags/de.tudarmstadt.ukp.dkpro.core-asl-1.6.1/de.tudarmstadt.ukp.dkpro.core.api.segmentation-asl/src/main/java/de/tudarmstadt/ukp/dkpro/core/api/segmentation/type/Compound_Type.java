
/* First created by JCasGen Sat Aug 04 18:47:40 CEST 2012 */
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

/** 
 * Updated by JCasGen Sat Aug 04 18:48:32 CEST 2012
 * @generated */
public class Compound_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Compound_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Compound_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Compound(addr, Compound_Type.this);
  			   Compound_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Compound(addr, Compound_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Compound.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound");
 
  /** @generated */
  final Feature casFeat_splits;
  /** @generated */
  final int     casFeatCode_splits;
  /** @generated */ 
  public int getSplits(int addr) {
        if (featOkTst && casFeat_splits == null)
      jcas.throwFeatMissing("splits", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound");
    return ll_cas.ll_getRefValue(addr, casFeatCode_splits);
  }
  /** @generated */    
  public void setSplits(int addr, int v) {
        if (featOkTst && casFeat_splits == null)
      jcas.throwFeatMissing("splits", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound");
    ll_cas.ll_setRefValue(addr, casFeatCode_splits, v);}
    
   /** @generated */
  public int getSplits(int addr, int i) {
        if (featOkTst && casFeat_splits == null)
      jcas.throwFeatMissing("splits", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_splits), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_splits), i);
  return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_splits), i);
  }
   
  /** @generated */ 
  public void setSplits(int addr, int i, int v) {
        if (featOkTst && casFeat_splits == null)
      jcas.throwFeatMissing("splits", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_splits), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_splits), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_splits), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Compound_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_splits = jcas.getRequiredFeatureDE(casType, "splits", "uima.cas.FSArray", featOkTst);
    casFeatCode_splits  = (null == casFeat_splits) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_splits).getCode();

  }
}



    