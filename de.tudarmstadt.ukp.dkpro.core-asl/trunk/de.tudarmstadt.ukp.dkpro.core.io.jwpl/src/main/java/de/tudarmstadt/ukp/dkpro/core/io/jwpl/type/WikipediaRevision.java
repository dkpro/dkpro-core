

/* First created by JCasGen Fri Jun 10 17:36:40 CEST 2011 */
package de.tudarmstadt.ukp.dkpro.core.io.jwpl.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** Represents a revision in Wikipedia.
 * Updated by JCasGen Fri Jun 10 18:26:23 CEST 2011
 * XML source: /home/zesch/workspace/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.io.jwpl/src/main/resources/desc/type/WikipediaRevision.xml
 * @generated */
public class WikipediaRevision extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(WikipediaRevision.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected WikipediaRevision() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public WikipediaRevision(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public WikipediaRevision(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public WikipediaRevision(JCas jcas, int begin, int end) {
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
  //* Feature: revisionId

  /** getter for revisionId - gets The ID of the revision.
   * @generated */
  public int getRevisionId() {
    if (WikipediaRevision_Type.featOkTst && ((WikipediaRevision_Type)jcasType).casFeat_revisionId == null)
      jcasType.jcas.throwFeatMissing("revisionId", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
    return jcasType.ll_cas.ll_getIntValue(addr, ((WikipediaRevision_Type)jcasType).casFeatCode_revisionId);}
    
  /** setter for revisionId - sets The ID of the revision. 
   * @generated */
  public void setRevisionId(int v) {
    if (WikipediaRevision_Type.featOkTst && ((WikipediaRevision_Type)jcasType).casFeat_revisionId == null)
      jcasType.jcas.throwFeatMissing("revisionId", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
    jcasType.ll_cas.ll_setIntValue(addr, ((WikipediaRevision_Type)jcasType).casFeatCode_revisionId, v);}    
   
    
  //*--------------*
  //* Feature: pageId

  /** getter for pageId - gets The pageId of the Wikipedia page of this revision.
   * @generated */
  public int getPageId() {
    if (WikipediaRevision_Type.featOkTst && ((WikipediaRevision_Type)jcasType).casFeat_pageId == null)
      jcasType.jcas.throwFeatMissing("pageId", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
    return jcasType.ll_cas.ll_getIntValue(addr, ((WikipediaRevision_Type)jcasType).casFeatCode_pageId);}
    
  /** setter for pageId - sets The pageId of the Wikipedia page of this revision. 
   * @generated */
  public void setPageId(int v) {
    if (WikipediaRevision_Type.featOkTst && ((WikipediaRevision_Type)jcasType).casFeat_pageId == null)
      jcasType.jcas.throwFeatMissing("pageId", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
    jcasType.ll_cas.ll_setIntValue(addr, ((WikipediaRevision_Type)jcasType).casFeatCode_pageId, v);}    
   
    
  //*--------------*
  //* Feature: userId

  /** getter for userId - gets The id of the user who edited this revision.
   * @generated */
  public int getUserId() {
    if (WikipediaRevision_Type.featOkTst && ((WikipediaRevision_Type)jcasType).casFeat_userId == null)
      jcasType.jcas.throwFeatMissing("userId", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
    return jcasType.ll_cas.ll_getIntValue(addr, ((WikipediaRevision_Type)jcasType).casFeatCode_userId);}
    
  /** setter for userId - sets The id of the user who edited this revision. 
   * @generated */
  public void setUserId(int v) {
    if (WikipediaRevision_Type.featOkTst && ((WikipediaRevision_Type)jcasType).casFeat_userId == null)
      jcasType.jcas.throwFeatMissing("userId", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
    jcasType.ll_cas.ll_setIntValue(addr, ((WikipediaRevision_Type)jcasType).casFeatCode_userId, v);}    
   
    
  //*--------------*
  //* Feature: comment

  /** getter for comment - gets The comment that the editor entered for this revision.
   * @generated */
  public String getComment() {
    if (WikipediaRevision_Type.featOkTst && ((WikipediaRevision_Type)jcasType).casFeat_comment == null)
      jcasType.jcas.throwFeatMissing("comment", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WikipediaRevision_Type)jcasType).casFeatCode_comment);}
    
  /** setter for comment - sets The comment that the editor entered for this revision. 
   * @generated */
  public void setComment(String v) {
    if (WikipediaRevision_Type.featOkTst && ((WikipediaRevision_Type)jcasType).casFeat_comment == null)
      jcasType.jcas.throwFeatMissing("comment", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaRevision");
    jcasType.ll_cas.ll_setStringValue(addr, ((WikipediaRevision_Type)jcasType).casFeatCode_comment, v);}    
  }

    