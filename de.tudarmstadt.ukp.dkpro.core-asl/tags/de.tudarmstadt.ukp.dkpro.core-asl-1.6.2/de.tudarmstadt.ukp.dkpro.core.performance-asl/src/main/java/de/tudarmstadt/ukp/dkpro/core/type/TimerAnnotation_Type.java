
/* First created by JCasGen Sun Jul 22 20:55:44 CEST 2012 */
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
 * Updated by JCasGen Sun Jul 22 20:55:44 CEST 2012
 * @generated */
public class TimerAnnotation_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (TimerAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = TimerAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new TimerAnnotation(addr, TimerAnnotation_Type.this);
  			   TimerAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new TimerAnnotation(addr, TimerAnnotation_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = TimerAnnotation.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.type.TimerAnnotation");
 
  /** @generated */
  final Feature casFeat_startTime;
  /** @generated */
  final int     casFeatCode_startTime;
  /** @generated */ 
  public long getStartTime(int addr) {
        if (featOkTst && casFeat_startTime == null)
      jcas.throwFeatMissing("startTime", "de.tudarmstadt.ukp.dkpro.core.type.TimerAnnotation");
    return ll_cas.ll_getLongValue(addr, casFeatCode_startTime);
  }
  /** @generated */    
  public void setStartTime(int addr, long v) {
        if (featOkTst && casFeat_startTime == null)
      jcas.throwFeatMissing("startTime", "de.tudarmstadt.ukp.dkpro.core.type.TimerAnnotation");
    ll_cas.ll_setLongValue(addr, casFeatCode_startTime, v);}
    
  
 
  /** @generated */
  final Feature casFeat_endTime;
  /** @generated */
  final int     casFeatCode_endTime;
  /** @generated */ 
  public long getEndTime(int addr) {
        if (featOkTst && casFeat_endTime == null)
      jcas.throwFeatMissing("endTime", "de.tudarmstadt.ukp.dkpro.core.type.TimerAnnotation");
    return ll_cas.ll_getLongValue(addr, casFeatCode_endTime);
  }
  /** @generated */    
  public void setEndTime(int addr, long v) {
        if (featOkTst && casFeat_endTime == null)
      jcas.throwFeatMissing("endTime", "de.tudarmstadt.ukp.dkpro.core.type.TimerAnnotation");
    ll_cas.ll_setLongValue(addr, casFeatCode_endTime, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public TimerAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_startTime = jcas.getRequiredFeatureDE(casType, "startTime", "uima.cas.Long", featOkTst);
    casFeatCode_startTime  = (null == casFeat_startTime) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_startTime).getCode();

 
    casFeat_endTime = jcas.getRequiredFeatureDE(casType, "endTime", "uima.cas.Long", featOkTst);
    casFeatCode_endTime  = (null == casFeat_endTime) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_endTime).getCode();

  }
}



    