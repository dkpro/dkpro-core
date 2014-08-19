
/* First created by JCasGen Tue Aug 09 17:52:18 CEST 2011 */
package de.tudarmstadt.ukp.dkpro.core.api.anomaly.type;

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
 * Updated by JCasGen Tue Aug 09 17:57:32 CEST 2011
 * @generated */
public class SuggestedAction_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (SuggestedAction_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = SuggestedAction_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new SuggestedAction(addr, SuggestedAction_Type.this);
  			   SuggestedAction_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new SuggestedAction(addr, SuggestedAction_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = SuggestedAction.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SuggestedAction");



  /** @generated */
  final Feature casFeat_replacement;
  /** @generated */
  final int     casFeatCode_replacement;
  /** @generated */ 
  public String getReplacement(int addr) {
        if (featOkTst && casFeat_replacement == null)
      jcas.throwFeatMissing("replacement", "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SuggestedAction");
    return ll_cas.ll_getStringValue(addr, casFeatCode_replacement);
  }
  /** @generated */    
  public void setReplacement(int addr, String v) {
        if (featOkTst && casFeat_replacement == null)
      jcas.throwFeatMissing("replacement", "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SuggestedAction");
    ll_cas.ll_setStringValue(addr, casFeatCode_replacement, v);}
    
  
 
  /** @generated */
  final Feature casFeat_certainty;
  /** @generated */
  final int     casFeatCode_certainty;
  /** @generated */ 
  public float getCertainty(int addr) {
        if (featOkTst && casFeat_certainty == null)
      jcas.throwFeatMissing("certainty", "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SuggestedAction");
    return ll_cas.ll_getFloatValue(addr, casFeatCode_certainty);
  }
  /** @generated */    
  public void setCertainty(int addr, float v) {
        if (featOkTst && casFeat_certainty == null)
      jcas.throwFeatMissing("certainty", "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SuggestedAction");
    ll_cas.ll_setFloatValue(addr, casFeatCode_certainty, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public SuggestedAction_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_replacement = jcas.getRequiredFeatureDE(casType, "replacement", "uima.cas.String", featOkTst);
    casFeatCode_replacement  = (null == casFeat_replacement) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_replacement).getCode();

 
    casFeat_certainty = jcas.getRequiredFeatureDE(casType, "certainty", "uima.cas.Float", featOkTst);
    casFeatCode_certainty  = (null == casFeat_certainty) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_certainty).getCode();

  }
}



    