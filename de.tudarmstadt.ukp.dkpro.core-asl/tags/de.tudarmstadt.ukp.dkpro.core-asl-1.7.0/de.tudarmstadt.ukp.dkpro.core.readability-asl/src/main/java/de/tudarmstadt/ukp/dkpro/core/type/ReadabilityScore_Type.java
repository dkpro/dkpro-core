
/* First created by JCasGen Wed Dec 18 10:03:08 CET 2013 */
package de.tudarmstadt.ukp.dkpro.core.type;

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
 * Updated by JCasGen Wed Dec 18 10:03:08 CET 2013
 * @generated */
public class ReadabilityScore_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (ReadabilityScore_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = ReadabilityScore_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new ReadabilityScore(addr, ReadabilityScore_Type.this);
  			   ReadabilityScore_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new ReadabilityScore(addr, ReadabilityScore_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = ReadabilityScore.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.type.ReadabilityScore");
 
  /** @generated */
  final Feature casFeat_measureName;
  /** @generated */
  final int     casFeatCode_measureName;
  /** @generated */ 
  public String getMeasureName(int addr) {
        if (featOkTst && casFeat_measureName == null)
      jcas.throwFeatMissing("measureName", "de.tudarmstadt.ukp.dkpro.core.type.ReadabilityScore");
    return ll_cas.ll_getStringValue(addr, casFeatCode_measureName);
  }
  /** @generated */    
  public void setMeasureName(int addr, String v) {
        if (featOkTst && casFeat_measureName == null)
      jcas.throwFeatMissing("measureName", "de.tudarmstadt.ukp.dkpro.core.type.ReadabilityScore");
    ll_cas.ll_setStringValue(addr, casFeatCode_measureName, v);}
    
  
 
  /** @generated */
  final Feature casFeat_score;
  /** @generated */
  final int     casFeatCode_score;
  /** @generated */ 
  public double getScore(int addr) {
        if (featOkTst && casFeat_score == null)
      jcas.throwFeatMissing("score", "de.tudarmstadt.ukp.dkpro.core.type.ReadabilityScore");
    return ll_cas.ll_getDoubleValue(addr, casFeatCode_score);
  }
  /** @generated */    
  public void setScore(int addr, double v) {
        if (featOkTst && casFeat_score == null)
      jcas.throwFeatMissing("score", "de.tudarmstadt.ukp.dkpro.core.type.ReadabilityScore");
    ll_cas.ll_setDoubleValue(addr, casFeatCode_score, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public ReadabilityScore_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_measureName = jcas.getRequiredFeatureDE(casType, "measureName", "uima.cas.String", featOkTst);
    casFeatCode_measureName  = (null == casFeat_measureName) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_measureName).getCode();

 
    casFeat_score = jcas.getRequiredFeatureDE(casType, "score", "uima.cas.Double", featOkTst);
    casFeatCode_score  = (null == casFeat_score) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_score).getCode();

  }
}



    