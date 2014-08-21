
/* First created by JCasGen Mon Feb 25 12:43:06 CET 2013 */
package de.tudarmstadt.ukp.dkpro.core.api.frequency.tfidf.type;

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
 * Updated by JCasGen Mon Feb 25 12:43:06 CET 2013
 * @generated */
public class Tfidf_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Tfidf_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Tfidf_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Tfidf(addr, Tfidf_Type.this);
  			   Tfidf_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Tfidf(addr, Tfidf_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Tfidf.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.api.frequency.tfidf.type.Tfidf");
 
  /** @generated */
  final Feature casFeat_tfidfValue;
  /** @generated */
  final int     casFeatCode_tfidfValue;
  /** @generated */ 
  public double getTfidfValue(int addr) {
        if (featOkTst && casFeat_tfidfValue == null)
      jcas.throwFeatMissing("tfidfValue", "de.tudarmstadt.ukp.dkpro.core.api.frequency.tfidf.type.Tfidf");
    return ll_cas.ll_getDoubleValue(addr, casFeatCode_tfidfValue);
  }
  /** @generated */    
  public void setTfidfValue(int addr, double v) {
        if (featOkTst && casFeat_tfidfValue == null)
      jcas.throwFeatMissing("tfidfValue", "de.tudarmstadt.ukp.dkpro.core.api.frequency.tfidf.type.Tfidf");
    ll_cas.ll_setDoubleValue(addr, casFeatCode_tfidfValue, v);}
    
  
 
  /** @generated */
  final Feature casFeat_term;
  /** @generated */
  final int     casFeatCode_term;
  /** @generated */ 
  public String getTerm(int addr) {
        if (featOkTst && casFeat_term == null)
      jcas.throwFeatMissing("term", "de.tudarmstadt.ukp.dkpro.core.api.frequency.tfidf.type.Tfidf");
    return ll_cas.ll_getStringValue(addr, casFeatCode_term);
  }
  /** @generated */    
  public void setTerm(int addr, String v) {
        if (featOkTst && casFeat_term == null)
      jcas.throwFeatMissing("term", "de.tudarmstadt.ukp.dkpro.core.api.frequency.tfidf.type.Tfidf");
    ll_cas.ll_setStringValue(addr, casFeatCode_term, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Tfidf_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_tfidfValue = jcas.getRequiredFeatureDE(casType, "tfidfValue", "uima.cas.Double", featOkTst);
    casFeatCode_tfidfValue  = (null == casFeat_tfidfValue) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_tfidfValue).getCode();

 
    casFeat_term = jcas.getRequiredFeatureDE(casType, "term", "uima.cas.String", featOkTst);
    casFeatCode_term  = (null == casFeat_term) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_term).getCode();

  }
}



    