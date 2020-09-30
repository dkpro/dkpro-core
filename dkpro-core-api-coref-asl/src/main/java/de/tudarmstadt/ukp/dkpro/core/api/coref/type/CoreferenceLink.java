

   
/* Apache UIMA v3 - First created by JCasGen Sun Jan 28 11:36:00 CET 2018 */

package de.tudarmstadt.ukp.dkpro.core.api.coref.type;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;

import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.impl.TypeSystemImpl;
import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;


import org.apache.uima.jcas.tcas.Annotation;


/** A link in the coreference chain.
 * Updated by JCasGen Sun Jan 28 11:36:00 CET 2018
 * XML source: /Users/bluefire/git/dkpro-core/dkpro-core-api-coref-asl/src/main/resources/desc/type/coref.xml
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
  private final static CallSite _FC_next = TypeSystemImpl.createCallSite(CoreferenceLink.class, "next");
  private final static MethodHandle _FH_next = _FC_next.dynamicInvoker();
  private final static CallSite _FC_referenceType = TypeSystemImpl.createCallSite(CoreferenceLink.class, "referenceType");
  private final static MethodHandle _FH_referenceType = _FC_referenceType.dynamicInvoker();
  private final static CallSite _FC_referenceRelation = TypeSystemImpl.createCallSite(CoreferenceLink.class, "referenceRelation");
  private final static MethodHandle _FH_referenceRelation = _FC_referenceRelation.dynamicInvoker();

   
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
  public CoreferenceLink getNext() { return (CoreferenceLink)(_getFeatureValueNc(wrapGetIntCatchException(_FH_next)));}
    
  /** setter for next - sets If there is one, it is the next coreference link to the current coreference link 
   * @generated
   * @param v value to set into the feature 
   */
  public void setNext(CoreferenceLink v) {
    _setFeatureValueNcWj(wrapGetIntCatchException(_FH_next), v);
  }    
    
   
    
  //*--------------*
  //* Feature: referenceType

  /** getter for referenceType - gets The role or type which the covered text has in the coreference chain.
   * @generated
   * @return value of the feature 
   */
  public String getReferenceType() { return _getStringValueNc(wrapGetIntCatchException(_FH_referenceType));}
    
  /** setter for referenceType - sets The role or type which the covered text has in the coreference chain. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setReferenceType(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_referenceType), v);
  }    
    
   
    
  //*--------------*
  //* Feature: referenceRelation

  /** getter for referenceRelation - gets The type of relation between this link and the next link in the chain.
   * @generated
   * @return value of the feature 
   */
  public String getReferenceRelation() { return _getStringValueNc(wrapGetIntCatchException(_FH_referenceRelation));}
    
  /** setter for referenceRelation - sets The type of relation between this link and the next link in the chain. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setReferenceRelation(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_referenceRelation), v);
  }    
    
  }

    