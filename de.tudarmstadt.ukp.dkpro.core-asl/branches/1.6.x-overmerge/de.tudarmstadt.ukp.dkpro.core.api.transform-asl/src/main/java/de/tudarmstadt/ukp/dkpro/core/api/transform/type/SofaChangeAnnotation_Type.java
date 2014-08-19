
/* First created by JCasGen Sun Nov 21 13:11:41 CET 2010 */
package de.tudarmstadt.ukp.dkpro.core.api.transform.type;

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

/** Describes a change to the Sofa.
 * Updated by JCasGen Sun Nov 21 13:11:41 CET 2010
 * @generated */
public class SofaChangeAnnotation_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (SofaChangeAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = SofaChangeAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new SofaChangeAnnotation(addr, SofaChangeAnnotation_Type.this);
  			   SofaChangeAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new SofaChangeAnnotation(addr, SofaChangeAnnotation_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = SofaChangeAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation");
 
  /** @generated */
  final Feature casFeat_value;
  /** @generated */
  final int     casFeatCode_value;
  /** @generated */ 
  public String getValue(int addr) {
        if (featOkTst && casFeat_value == null)
      jcas.throwFeatMissing("value", "de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_value);
  }
  /** @generated */    
  public void setValue(int addr, String v) {
        if (featOkTst && casFeat_value == null)
      jcas.throwFeatMissing("value", "de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_value, v);}
    
  
 
  /** @generated */
  final Feature casFeat_operation;
  /** @generated */
  final int     casFeatCode_operation;
  /** @generated */ 
  public String getOperation(int addr) {
        if (featOkTst && casFeat_operation == null)
      jcas.throwFeatMissing("operation", "de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_operation);
  }
  /** @generated */    
  public void setOperation(int addr, String v) {
        if (featOkTst && casFeat_operation == null)
      jcas.throwFeatMissing("operation", "de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_operation, v);}
    
  
 
  /** @generated */
  final Feature casFeat_reason;
  /** @generated */
  final int     casFeatCode_reason;
  /** @generated */ 
  public String getReason(int addr) {
        if (featOkTst && casFeat_reason == null)
      jcas.throwFeatMissing("reason", "de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_reason);
  }
  /** @generated */    
  public void setReason(int addr, String v) {
        if (featOkTst && casFeat_reason == null)
      jcas.throwFeatMissing("reason", "de.tudarmstadt.ukp.dkpro.core.api.transform.type.SofaChangeAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_reason, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public SofaChangeAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_value = jcas.getRequiredFeatureDE(casType, "value", "uima.cas.String", featOkTst);
    casFeatCode_value  = (null == casFeat_value) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_value).getCode();

 
    casFeat_operation = jcas.getRequiredFeatureDE(casType, "operation", "uima.cas.String", featOkTst);
    casFeatCode_operation  = (null == casFeat_operation) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_operation).getCode();

 
    casFeat_reason = jcas.getRequiredFeatureDE(casType, "reason", "uima.cas.String", featOkTst);
    casFeatCode_reason  = (null == casFeat_reason) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_reason).getCode();

  }
}



    