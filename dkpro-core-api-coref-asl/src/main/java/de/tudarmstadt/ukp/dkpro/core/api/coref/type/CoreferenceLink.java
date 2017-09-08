

   
/* Apache UIMA v3 - First created by JCasGen Fri Sep 08 09:57:05 EEST 2017 */

package de.tudarmstadt.ukp.dkpro.core.api.coref.type;

import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.impl.TypeSystemImpl;
import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;


import org.apache.uima.jcas.tcas.Annotation;


/** A link in the coreference chain.
 * Updated by JCasGen Fri Sep 08 09:57:05 EEST 2017
 * XML source: /Users/bluefire/git/dkpro-core/dkpro-core-api-coref-asl/target/jcasgen/typesystem.xml
 * @generated */
public class CoreferenceLink extends Annotation {
 
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static String _TypeName = "de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceLink";
  
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(CoreferenceLink.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
 
  /* *******************
   *   Feature Offsets *
   * *******************/ 
   
  public final static String _FeatName_next = "next";
  public final static String _FeatName_referenceType = "referenceType";
  public final static String _FeatName_referenceRelation = "referenceRelation";


  /* Feature Adjusted Offsets */
  public final static int _FI_next = TypeSystemImpl.getAdjustedFeatureOffset("next");
  public final static int _FI_referenceType = TypeSystemImpl.getAdjustedFeatureOffset("referenceType");
  public final static int _FI_referenceRelation = TypeSystemImpl.getAdjustedFeatureOffset("referenceRelation");

   
  /** Never called.  Disable default constructor
   * @generated */
  protected CoreferenceLink() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param casImpl the CAS this Feature Structure belongs to
   * @param type the type of this Feature Structure 
   */
  public CoreferenceLink(TypeImpl type, CASImpl casImpl) {
    super(type, casImpl);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public CoreferenceLink(JCas jcas) {
    super(jcas);
    readObject();   
  } 


  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public CoreferenceLink(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: next

  /** getter for next - gets If there is one, it is the next coreference link to the current coreference link
   * @generated
   * @return value of the feature 
   */
  public CoreferenceLink getNext() { return (CoreferenceLink)(_getFeatureValueNc(_FI_next));}
    
  /** setter for next - sets If there is one, it is the next coreference link to the current coreference link 
   * @generated
   * @param v value to set into the feature 
   */
  public void setNext(CoreferenceLink v) {
    _setFeatureValueNcWj(_FI_next, v);
  }    
    
   
    
  //*--------------*
  //* Feature: referenceType

  /** getter for referenceType - gets The role or type which the covered text has in the coreference chain.
   * @generated
   * @return value of the feature 
   */
  public String getReferenceType() { return _getStringValueNc(_FI_referenceType);}
    
  /** setter for referenceType - sets The role or type which the covered text has in the coreference chain. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setReferenceType(String v) {
    _setStringValueNfc(_FI_referenceType, v);
  }    
    
   
    
  //*--------------*
  //* Feature: referenceRelation

  /** getter for referenceRelation - gets The type of relation between this link and the next link in the chain.
   * @generated
   * @return value of the feature 
   */
  public String getReferenceRelation() { return _getStringValueNc(_FI_referenceRelation);}
    
  /** setter for referenceRelation - sets The type of relation between this link and the next link in the chain. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setReferenceRelation(String v) {
    _setStringValueNfc(_FI_referenceRelation, v);
  }    
    
  }

    