

/* First created by JCasGen Thu Jul 14 19:05:55 CEST 2011 */
package de.tudarmstadt.ukp.dkpro.core.io.jwpl.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** Contains basic information about the article.
 * Updated by JCasGen Thu Jul 14 19:05:55 CEST 2011
 * XML source: E:/eclipse/workspace/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.io.jwpl/src/main/resources/desc/type/WikipediaArticleInfo.xml
 * @generated */
public class ArticleInfo extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(ArticleInfo.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected ArticleInfo() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public ArticleInfo(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public ArticleInfo(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public ArticleInfo(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: Authors

  /** getter for Authors - gets Number of unique authors of this article
   * @generated */
  public int getAuthors() {
    if (ArticleInfo_Type.featOkTst && ((ArticleInfo_Type)jcasType).casFeat_Authors == null)
      jcasType.jcas.throwFeatMissing("Authors", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.ArticleInfo");
    return jcasType.ll_cas.ll_getIntValue(addr, ((ArticleInfo_Type)jcasType).casFeatCode_Authors);}
    
  /** setter for Authors - sets Number of unique authors of this article 
   * @generated */
  public void setAuthors(int v) {
    if (ArticleInfo_Type.featOkTst && ((ArticleInfo_Type)jcasType).casFeat_Authors == null)
      jcasType.jcas.throwFeatMissing("Authors", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.ArticleInfo");
    jcasType.ll_cas.ll_setIntValue(addr, ((ArticleInfo_Type)jcasType).casFeatCode_Authors, v);}    
   
    
  //*--------------*
  //* Feature: Revisions

  /** getter for Revisions - gets Number of revisions of this article.
   * @generated */
  public int getRevisions() {
    if (ArticleInfo_Type.featOkTst && ((ArticleInfo_Type)jcasType).casFeat_Revisions == null)
      jcasType.jcas.throwFeatMissing("Revisions", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.ArticleInfo");
    return jcasType.ll_cas.ll_getIntValue(addr, ((ArticleInfo_Type)jcasType).casFeatCode_Revisions);}
    
  /** setter for Revisions - sets Number of revisions of this article. 
   * @generated */
  public void setRevisions(int v) {
    if (ArticleInfo_Type.featOkTst && ((ArticleInfo_Type)jcasType).casFeat_Revisions == null)
      jcasType.jcas.throwFeatMissing("Revisions", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.ArticleInfo");
    jcasType.ll_cas.ll_setIntValue(addr, ((ArticleInfo_Type)jcasType).casFeatCode_Revisions, v);}    
  }

    