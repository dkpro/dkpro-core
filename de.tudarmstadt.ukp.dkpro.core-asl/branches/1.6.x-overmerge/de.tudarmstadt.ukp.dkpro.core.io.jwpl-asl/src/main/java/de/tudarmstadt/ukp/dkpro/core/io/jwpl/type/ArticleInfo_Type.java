
/* First created by JCasGen Thu Jul 14 19:05:55 CEST 2011 */
package de.tudarmstadt.ukp.dkpro.core.io.jwpl.type;

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

/** Contains basic information about the article.
 * Updated by JCasGen Thu Jul 14 22:18:52 CEST 2011
 * @generated */
public class ArticleInfo_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (ArticleInfo_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = ArticleInfo_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new ArticleInfo(addr, ArticleInfo_Type.this);
  			   ArticleInfo_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new ArticleInfo(addr, ArticleInfo_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = ArticleInfo.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.ArticleInfo");
 
  /** @generated */
  final Feature casFeat_Authors;
  /** @generated */
  final int     casFeatCode_Authors;
  /** @generated */ 
  public int getAuthors(int addr) {
        if (featOkTst && casFeat_Authors == null)
      jcas.throwFeatMissing("Authors", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.ArticleInfo");
    return ll_cas.ll_getIntValue(addr, casFeatCode_Authors);
  }
  /** @generated */    
  public void setAuthors(int addr, int v) {
        if (featOkTst && casFeat_Authors == null)
      jcas.throwFeatMissing("Authors", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.ArticleInfo");
    ll_cas.ll_setIntValue(addr, casFeatCode_Authors, v);}
    
  
 
  /** @generated */
  final Feature casFeat_Revisions;
  /** @generated */
  final int     casFeatCode_Revisions;
  /** @generated */ 
  public int getRevisions(int addr) {
        if (featOkTst && casFeat_Revisions == null)
      jcas.throwFeatMissing("Revisions", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.ArticleInfo");
    return ll_cas.ll_getIntValue(addr, casFeatCode_Revisions);
  }
  /** @generated */    
  public void setRevisions(int addr, int v) {
        if (featOkTst && casFeat_Revisions == null)
      jcas.throwFeatMissing("Revisions", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.ArticleInfo");
    ll_cas.ll_setIntValue(addr, casFeatCode_Revisions, v);}
    
  
 
  /** @generated */
  final Feature casFeat_FirstAppearance;
  /** @generated */
  final int     casFeatCode_FirstAppearance;
  /** @generated */ 
  public long getFirstAppearance(int addr) {
        if (featOkTst && casFeat_FirstAppearance == null)
      jcas.throwFeatMissing("FirstAppearance", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.ArticleInfo");
    return ll_cas.ll_getLongValue(addr, casFeatCode_FirstAppearance);
  }
  /** @generated */    
  public void setFirstAppearance(int addr, long v) {
        if (featOkTst && casFeat_FirstAppearance == null)
      jcas.throwFeatMissing("FirstAppearance", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.ArticleInfo");
    ll_cas.ll_setLongValue(addr, casFeatCode_FirstAppearance, v);}
    
  
 
  /** @generated */
  final Feature casFeat_LastAppearance;
  /** @generated */
  final int     casFeatCode_LastAppearance;
  /** @generated */ 
  public long getLastAppearance(int addr) {
        if (featOkTst && casFeat_LastAppearance == null)
      jcas.throwFeatMissing("LastAppearance", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.ArticleInfo");
    return ll_cas.ll_getLongValue(addr, casFeatCode_LastAppearance);
  }
  /** @generated */    
  public void setLastAppearance(int addr, long v) {
        if (featOkTst && casFeat_LastAppearance == null)
      jcas.throwFeatMissing("LastAppearance", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.ArticleInfo");
    ll_cas.ll_setLongValue(addr, casFeatCode_LastAppearance, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public ArticleInfo_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_Authors = jcas.getRequiredFeatureDE(casType, "Authors", "uima.cas.Integer", featOkTst);
    casFeatCode_Authors  = (null == casFeat_Authors) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Authors).getCode();

 
    casFeat_Revisions = jcas.getRequiredFeatureDE(casType, "Revisions", "uima.cas.Integer", featOkTst);
    casFeatCode_Revisions  = (null == casFeat_Revisions) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Revisions).getCode();

 
    casFeat_FirstAppearance = jcas.getRequiredFeatureDE(casType, "FirstAppearance", "uima.cas.Long", featOkTst);
    casFeatCode_FirstAppearance  = (null == casFeat_FirstAppearance) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_FirstAppearance).getCode();

 
    casFeat_LastAppearance = jcas.getRequiredFeatureDE(casType, "LastAppearance", "uima.cas.Long", featOkTst);
    casFeatCode_LastAppearance  = (null == casFeat_LastAppearance) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_LastAppearance).getCode();

  }
}



    