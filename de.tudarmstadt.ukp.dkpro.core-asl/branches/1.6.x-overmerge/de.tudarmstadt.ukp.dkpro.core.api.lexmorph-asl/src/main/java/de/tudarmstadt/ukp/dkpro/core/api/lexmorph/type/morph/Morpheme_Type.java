
/* First created by JCasGen Wed Sep 19 10:00:35 CEST 2012 */
package de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph;

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
 * Updated by JCasGen Wed Sep 19 10:00:35 CEST 2012
 * @generated */
public class Morpheme_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Morpheme_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Morpheme_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Morpheme(addr, Morpheme_Type.this);
  			   Morpheme_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Morpheme(addr, Morpheme_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Morpheme.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.Morpheme");
 
  /** @generated */
  final Feature casFeat_morphTag;
  /** @generated */
  final int     casFeatCode_morphTag;
  /** @generated */ 
  public String getMorphTag(int addr) {
        if (featOkTst && casFeat_morphTag == null)
      jcas.throwFeatMissing("morphTag", "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.Morpheme");
    return ll_cas.ll_getStringValue(addr, casFeatCode_morphTag);
  }
  /** @generated */    
  public void setMorphTag(int addr, String v) {
        if (featOkTst && casFeat_morphTag == null)
      jcas.throwFeatMissing("morphTag", "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.Morpheme");
    ll_cas.ll_setStringValue(addr, casFeatCode_morphTag, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Morpheme_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_morphTag = jcas.getRequiredFeatureDE(casType, "morphTag", "uima.cas.String", featOkTst);
    casFeatCode_morphTag  = (null == casFeat_morphTag) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_morphTag).getCode();

  }
}



    