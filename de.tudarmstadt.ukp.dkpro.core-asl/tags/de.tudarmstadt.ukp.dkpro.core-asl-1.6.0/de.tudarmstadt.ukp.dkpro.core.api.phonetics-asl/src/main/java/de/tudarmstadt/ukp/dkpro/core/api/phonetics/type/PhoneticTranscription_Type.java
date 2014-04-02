
/* First created by JCasGen Thu Jun 13 11:42:05 CEST 2013 */
package de.tudarmstadt.ukp.dkpro.core.api.phonetics.type;

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
 * Updated by JCasGen Thu Jun 13 11:42:05 CEST 2013
 * @generated */
public class PhoneticTranscription_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      @Override
    public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (PhoneticTranscription_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = PhoneticTranscription_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new PhoneticTranscription(addr, PhoneticTranscription_Type.this);
  			   PhoneticTranscription_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        }
            else {
                return new PhoneticTranscription(addr, PhoneticTranscription_Type.this);
            }
  	  }
    };
  /** @generated */
  public final static int typeIndexID = PhoneticTranscription.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.api.phonetics.type.PhoneticTranscription");
 
  /** @generated */
  final Feature casFeat_transcription;
  /** @generated */
  final int     casFeatCode_transcription;
  /** @generated */ 
  public String getTranscription(int addr) {
        if (featOkTst && casFeat_transcription == null) {
            jcas.throwFeatMissing("transcription", "de.tudarmstadt.ukp.dkpro.core.api.phonetics.type.PhoneticTranscription");
        }
    return ll_cas.ll_getStringValue(addr, casFeatCode_transcription);
  }
  /** @generated */    
  public void setTranscription(int addr, String v) {
        if (featOkTst && casFeat_transcription == null) {
            jcas.throwFeatMissing("transcription", "de.tudarmstadt.ukp.dkpro.core.api.phonetics.type.PhoneticTranscription");
        }
    ll_cas.ll_setStringValue(addr, casFeatCode_transcription, v);}
    
  
 
  /** @generated */
  final Feature casFeat_name;
  /** @generated */
  final int     casFeatCode_name;
  /** @generated */ 
  public String getName(int addr) {
        if (featOkTst && casFeat_name == null) {
            jcas.throwFeatMissing("name", "de.tudarmstadt.ukp.dkpro.core.api.phonetics.type.PhoneticTranscription");
        }
    return ll_cas.ll_getStringValue(addr, casFeatCode_name);
  }
  /** @generated */    
  public void setName(int addr, String v) {
        if (featOkTst && casFeat_name == null) {
            jcas.throwFeatMissing("name", "de.tudarmstadt.ukp.dkpro.core.api.phonetics.type.PhoneticTranscription");
        }
    ll_cas.ll_setStringValue(addr, casFeatCode_name, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public PhoneticTranscription_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_transcription = jcas.getRequiredFeatureDE(casType, "transcription", "uima.cas.String", featOkTst);
    casFeatCode_transcription  = (null == casFeat_transcription) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_transcription).getCode();

 
    casFeat_name = jcas.getRequiredFeatureDE(casType, "name", "uima.cas.String", featOkTst);
    casFeatCode_name  = (null == casFeat_name) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_name).getCode();

  }
}



    