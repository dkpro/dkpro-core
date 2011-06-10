
/* First created by JCasGen Fri Jun 10 17:36:40 CEST 2011 */
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

/** Represents a revision in Wikipedia.
 * Updated by JCasGen Fri Jun 10 18:26:23 CEST 2011
 * @generated */
public class WikipediaRevision_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (WikipediaRevision_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = WikipediaRevision_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new WikipediaRevision(addr, WikipediaRevision_Type.this);
  			   WikipediaRevision_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new WikipediaRevision(addr, WikipediaRevision_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = WikipediaRevision.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
 
  /** @generated */
  final Feature casFeat_revisionId;
  /** @generated */
  final int     casFeatCode_revisionId;
  /** @generated */ 
  public int getRevisionId(int addr) {
        if (featOkTst && casFeat_revisionId == null)
      jcas.throwFeatMissing("revisionId", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
    return ll_cas.ll_getIntValue(addr, casFeatCode_revisionId);
  }
  /** @generated */    
  public void setRevisionId(int addr, int v) {
        if (featOkTst && casFeat_revisionId == null)
      jcas.throwFeatMissing("revisionId", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
    ll_cas.ll_setIntValue(addr, casFeatCode_revisionId, v);}
    
  
 
  /** @generated */
  final Feature casFeat_pageId;
  /** @generated */
  final int     casFeatCode_pageId;
  /** @generated */ 
  public int getPageId(int addr) {
        if (featOkTst && casFeat_pageId == null)
      jcas.throwFeatMissing("pageId", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
    return ll_cas.ll_getIntValue(addr, casFeatCode_pageId);
  }
  /** @generated */    
  public void setPageId(int addr, int v) {
        if (featOkTst && casFeat_pageId == null)
      jcas.throwFeatMissing("pageId", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
    ll_cas.ll_setIntValue(addr, casFeatCode_pageId, v);}
    
  
 
  /** @generated */
  final Feature casFeat_userId;
  /** @generated */
  final int     casFeatCode_userId;
  /** @generated */ 
  public int getUserId(int addr) {
        if (featOkTst && casFeat_userId == null)
      jcas.throwFeatMissing("userId", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
    return ll_cas.ll_getIntValue(addr, casFeatCode_userId);
  }
  /** @generated */    
  public void setUserId(int addr, int v) {
        if (featOkTst && casFeat_userId == null)
      jcas.throwFeatMissing("userId", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
    ll_cas.ll_setIntValue(addr, casFeatCode_userId, v);}
    
  
 
  /** @generated */
  final Feature casFeat_comment;
  /** @generated */
  final int     casFeatCode_comment;
  /** @generated */ 
  public String getComment(int addr) {
        if (featOkTst && casFeat_comment == null)
      jcas.throwFeatMissing("comment", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
    return ll_cas.ll_getStringValue(addr, casFeatCode_comment);
  }
  /** @generated */    
  public void setComment(int addr, String v) {
        if (featOkTst && casFeat_comment == null)
      jcas.throwFeatMissing("comment", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
    ll_cas.ll_setStringValue(addr, casFeatCode_comment, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public WikipediaRevision_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_revisionId = jcas.getRequiredFeatureDE(casType, "revisionId", "uima.cas.Integer", featOkTst);
    casFeatCode_revisionId  = (null == casFeat_revisionId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_revisionId).getCode();

 
    casFeat_pageId = jcas.getRequiredFeatureDE(casType, "pageId", "uima.cas.Integer", featOkTst);
    casFeatCode_pageId  = (null == casFeat_pageId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_pageId).getCode();

 
    casFeat_userId = jcas.getRequiredFeatureDE(casType, "userId", "uima.cas.Integer", featOkTst);
    casFeatCode_userId  = (null == casFeat_userId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_userId).getCode();

 
    casFeat_comment = jcas.getRequiredFeatureDE(casType, "comment", "uima.cas.String", featOkTst);
    casFeatCode_comment  = (null == casFeat_comment) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_comment).getCode();

  }
}



    