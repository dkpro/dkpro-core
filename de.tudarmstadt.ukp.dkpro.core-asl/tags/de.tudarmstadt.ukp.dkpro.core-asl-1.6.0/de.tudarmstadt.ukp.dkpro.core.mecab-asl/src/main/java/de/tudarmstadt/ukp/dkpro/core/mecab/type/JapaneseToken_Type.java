
/* First created by JCasGen Sat Jul 14 18:17:22 CEST 2012 */
package de.tudarmstadt.ukp.dkpro.core.mecab.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token_Type;

/** 
 * Updated by JCasGen Sat Jul 14 18:17:24 CEST 2012
 * @generated */
public class JapaneseToken_Type extends Token_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (JapaneseToken_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = JapaneseToken_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new JapaneseToken(addr, JapaneseToken_Type.this);
  			   JapaneseToken_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new JapaneseToken(addr, JapaneseToken_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JapaneseToken.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken");
 
  /** @generated */
  final Feature casFeat_kana;
  /** @generated */
  final int     casFeatCode_kana;
  /** @generated */ 
  public String getKana(int addr) {
        if (featOkTst && casFeat_kana == null)
      jcas.throwFeatMissing("kana", "de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken");
    return ll_cas.ll_getStringValue(addr, casFeatCode_kana);
  }
  /** @generated */    
  public void setKana(int addr, String v) {
        if (featOkTst && casFeat_kana == null)
      jcas.throwFeatMissing("kana", "de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken");
    ll_cas.ll_setStringValue(addr, casFeatCode_kana, v);}
    
  
 
  /** @generated */
  final Feature casFeat_ibo;
  /** @generated */
  final int     casFeatCode_ibo;
  /** @generated */ 
  public String getIbo(int addr) {
        if (featOkTst && casFeat_ibo == null)
      jcas.throwFeatMissing("ibo", "de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken");
    return ll_cas.ll_getStringValue(addr, casFeatCode_ibo);
  }
  /** @generated */    
  public void setIbo(int addr, String v) {
        if (featOkTst && casFeat_ibo == null)
      jcas.throwFeatMissing("ibo", "de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken");
    ll_cas.ll_setStringValue(addr, casFeatCode_ibo, v);}
    
  
 
  /** @generated */
  final Feature casFeat_kei;
  /** @generated */
  final int     casFeatCode_kei;
  /** @generated */ 
  public String getKei(int addr) {
        if (featOkTst && casFeat_kei == null)
      jcas.throwFeatMissing("kei", "de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken");
    return ll_cas.ll_getStringValue(addr, casFeatCode_kei);
  }
  /** @generated */    
  public void setKei(int addr, String v) {
        if (featOkTst && casFeat_kei == null)
      jcas.throwFeatMissing("kei", "de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken");
    ll_cas.ll_setStringValue(addr, casFeatCode_kei, v);}
    
  
 
  /** @generated */
  final Feature casFeat_dan;
  /** @generated */
  final int     casFeatCode_dan;
  /** @generated */ 
  public String getDan(int addr) {
        if (featOkTst && casFeat_dan == null)
      jcas.throwFeatMissing("dan", "de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken");
    return ll_cas.ll_getStringValue(addr, casFeatCode_dan);
  }
  /** @generated */    
  public void setDan(int addr, String v) {
        if (featOkTst && casFeat_dan == null)
      jcas.throwFeatMissing("dan", "de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken");
    ll_cas.ll_setStringValue(addr, casFeatCode_dan, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public JapaneseToken_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_kana = jcas.getRequiredFeatureDE(casType, "kana", "uima.cas.String", featOkTst);
    casFeatCode_kana  = (null == casFeat_kana) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_kana).getCode();

 
    casFeat_ibo = jcas.getRequiredFeatureDE(casType, "ibo", "uima.cas.String", featOkTst);
    casFeatCode_ibo  = (null == casFeat_ibo) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_ibo).getCode();

 
    casFeat_kei = jcas.getRequiredFeatureDE(casType, "kei", "uima.cas.String", featOkTst);
    casFeatCode_kei  = (null == casFeat_kei) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_kei).getCode();

 
    casFeat_dan = jcas.getRequiredFeatureDE(casType, "dan", "uima.cas.String", featOkTst);
    casFeatCode_dan  = (null == casFeat_dan) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_dan).getCode();

  }
}



    