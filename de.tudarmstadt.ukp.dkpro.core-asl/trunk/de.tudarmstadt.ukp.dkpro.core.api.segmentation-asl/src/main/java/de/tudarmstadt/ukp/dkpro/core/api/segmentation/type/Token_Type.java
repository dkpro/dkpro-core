
/* First created by JCasGen Tue Dec 04 00:43:53 CET 2012 */
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

/** Token is one of the two types commonly produced by a segmenter (the other being {@link Sentence}). A Token usually represents a word, although it may be used to represent multiple tightly connected words (e.g. "New York") or parts of a word (e.g. the possessive "'s"). One may choose to split compound words into multiple tokens, e.g. ("CamelCase" -> "Camel", "Case"; "Zauberstab" -> "Zauber", "stab"). Most processing components operate on Tokens, usually within the limits of the surrounding Sentence. E.g. a part-of-speech tagger analyses each Token in a Sentence and assigns a part-of-speech to each Token.
<p>
 * Updated by JCasGen Thu Feb 13 16:04:48 CET 2014
 * @generated */
public class Token_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Token_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Token_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Token(addr, Token_Type.this);
  			   Token_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Token(addr, Token_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Token.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
 
  /** @generated */
  final Feature casFeat_parent;
  /** @generated */
  final int     casFeatCode_parent;
  /** @generated */ 
  public int getParent(int addr) {
        if (featOkTst && casFeat_parent == null)
      jcas.throwFeatMissing("parent", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    return ll_cas.ll_getRefValue(addr, casFeatCode_parent);
  }
  /** @generated */    
  public void setParent(int addr, int v) {
        if (featOkTst && casFeat_parent == null)
      jcas.throwFeatMissing("parent", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    ll_cas.ll_setRefValue(addr, casFeatCode_parent, v);}
    
  
 
  /** @generated */
  final Feature casFeat_lemma;
  /** @generated */
  final int     casFeatCode_lemma;
  /** @generated */ 
  public int getLemma(int addr) {
        if (featOkTst && casFeat_lemma == null)
      jcas.throwFeatMissing("lemma", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    return ll_cas.ll_getRefValue(addr, casFeatCode_lemma);
  }
  /** @generated */    
  public void setLemma(int addr, int v) {
        if (featOkTst && casFeat_lemma == null)
      jcas.throwFeatMissing("lemma", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    ll_cas.ll_setRefValue(addr, casFeatCode_lemma, v);}
    
  
 
  /** @generated */
  final Feature casFeat_stem;
  /** @generated */
  final int     casFeatCode_stem;
  /** @generated */ 
  public int getStem(int addr) {
        if (featOkTst && casFeat_stem == null)
      jcas.throwFeatMissing("stem", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    return ll_cas.ll_getRefValue(addr, casFeatCode_stem);
  }
  /** @generated */    
  public void setStem(int addr, int v) {
        if (featOkTst && casFeat_stem == null)
      jcas.throwFeatMissing("stem", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    ll_cas.ll_setRefValue(addr, casFeatCode_stem, v);}
    
  
 
  /** @generated */
  final Feature casFeat_pos;
  /** @generated */
  final int     casFeatCode_pos;
  /** @generated */ 
  public int getPos(int addr) {
        if (featOkTst && casFeat_pos == null)
      jcas.throwFeatMissing("pos", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    return ll_cas.ll_getRefValue(addr, casFeatCode_pos);
  }
  /** @generated */    
  public void setPos(int addr, int v) {
        if (featOkTst && casFeat_pos == null)
      jcas.throwFeatMissing("pos", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    ll_cas.ll_setRefValue(addr, casFeatCode_pos, v);}
    
  
 
  /** @generated */
  final Feature casFeat_morphologicalFeatures;
  /** @generated */
  final int     casFeatCode_morphologicalFeatures;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getMorphologicalFeatures(int addr) {
        if (featOkTst && casFeat_morphologicalFeatures == null)
      jcas.throwFeatMissing("morphologicalFeatures", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    return ll_cas.ll_getRefValue(addr, casFeatCode_morphologicalFeatures);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setMorphologicalFeatures(int addr, int v) {
        if (featOkTst && casFeat_morphologicalFeatures == null)
      jcas.throwFeatMissing("morphologicalFeatures", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    ll_cas.ll_setRefValue(addr, casFeatCode_morphologicalFeatures, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Token_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_parent = jcas.getRequiredFeatureDE(casType, "parent", "uima.tcas.Annotation", featOkTst);
    casFeatCode_parent  = (null == casFeat_parent) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_parent).getCode();

 
    casFeat_lemma = jcas.getRequiredFeatureDE(casType, "lemma", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma", featOkTst);
    casFeatCode_lemma  = (null == casFeat_lemma) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_lemma).getCode();

 
    casFeat_stem = jcas.getRequiredFeatureDE(casType, "stem", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem", featOkTst);
    casFeatCode_stem  = (null == casFeat_stem) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_stem).getCode();

 
    casFeat_pos = jcas.getRequiredFeatureDE(casType, "pos", "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS", featOkTst);
    casFeatCode_pos  = (null == casFeat_pos) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_pos).getCode();

 
    casFeat_morphologicalFeatures = jcas.getRequiredFeatureDE(casType, "morphologicalFeatures", "de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures", featOkTst);
    casFeatCode_morphologicalFeatures  = (null == casFeat_morphologicalFeatures) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_morphologicalFeatures).getCode();

  }
}



    