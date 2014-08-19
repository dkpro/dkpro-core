

/* First created by JCasGen Thu Jan 17 11:01:34 CET 2013 */
package de.tudarmstadt.ukp.dkpro.core.io.jwpl.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** Wikipedia link
 * Updated by JCasGen Thu Jan 17 17:26:02 CET 2013
 * XML source: /srv/workspace42/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.io.jwpl-asl/src/main/resources/desc/type/wikipediaLink.xml
 * @generated */
public class WikipediaLink extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(WikipediaLink.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated  */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected WikipediaLink() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public WikipediaLink(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public WikipediaLink(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public WikipediaLink(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: LinkType

  /** getter for LinkType - gets The type of the link, e.g. internal, external, image, ...
   * @generated */
  public String getLinkType() {
    if (WikipediaLink_Type.featOkTst && ((WikipediaLink_Type)jcasType).casFeat_LinkType == null)
      jcasType.jcas.throwFeatMissing("LinkType", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaLink");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WikipediaLink_Type)jcasType).casFeatCode_LinkType);}
    
  /** setter for LinkType - sets The type of the link, e.g. internal, external, image, ... 
   * @generated */
  public void setLinkType(String v) {
    if (WikipediaLink_Type.featOkTst && ((WikipediaLink_Type)jcasType).casFeat_LinkType == null)
      jcasType.jcas.throwFeatMissing("LinkType", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaLink");
    jcasType.ll_cas.ll_setStringValue(addr, ((WikipediaLink_Type)jcasType).casFeatCode_LinkType, v);}    
   
    
  //*--------------*
  //* Feature: Target

  /** getter for Target - gets The link target url
   * @generated */
  public String getTarget() {
    if (WikipediaLink_Type.featOkTst && ((WikipediaLink_Type)jcasType).casFeat_Target == null)
      jcasType.jcas.throwFeatMissing("Target", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaLink");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WikipediaLink_Type)jcasType).casFeatCode_Target);}
    
  /** setter for Target - sets The link target url 
   * @generated */
  public void setTarget(String v) {
    if (WikipediaLink_Type.featOkTst && ((WikipediaLink_Type)jcasType).casFeat_Target == null)
      jcasType.jcas.throwFeatMissing("Target", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaLink");
    jcasType.ll_cas.ll_setStringValue(addr, ((WikipediaLink_Type)jcasType).casFeatCode_Target, v);}    
   
    
  //*--------------*
  //* Feature: Anchor

  /** getter for Anchor - gets The anchor of the link
   * @generated */
  public String getAnchor() {
    if (WikipediaLink_Type.featOkTst && ((WikipediaLink_Type)jcasType).casFeat_Anchor == null)
      jcasType.jcas.throwFeatMissing("Anchor", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaLink");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WikipediaLink_Type)jcasType).casFeatCode_Anchor);}
    
  /** setter for Anchor - sets The anchor of the link 
   * @generated */
  public void setAnchor(String v) {
    if (WikipediaLink_Type.featOkTst && ((WikipediaLink_Type)jcasType).casFeat_Anchor == null)
      jcasType.jcas.throwFeatMissing("Anchor", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.WikipediaLink");
    jcasType.ll_cas.ll_setStringValue(addr, ((WikipediaLink_Type)jcasType).casFeatCode_Anchor, v);}    
  }

    