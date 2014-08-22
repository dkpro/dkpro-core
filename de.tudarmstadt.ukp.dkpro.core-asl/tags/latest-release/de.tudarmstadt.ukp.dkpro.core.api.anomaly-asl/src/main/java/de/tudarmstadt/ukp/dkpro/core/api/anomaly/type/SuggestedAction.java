

/* First created by JCasGen Tue Aug 09 17:52:18 CEST 2011 */
package de.tudarmstadt.ukp.dkpro.core.api.anomaly.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Tue Aug 09 17:57:32 CEST 2011
 * XML source: /home/zesch/workspace/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.api.anomaly/src/main/resources/desc/type/Anomalies.xml
 * @generated */
public class SuggestedAction extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(SuggestedAction.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected SuggestedAction() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public SuggestedAction(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public SuggestedAction(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public SuggestedAction(JCas jcas, int begin, int end) {
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
  //* Feature: replacement

  /** getter for replacement - gets The text covered by the Anomaly annotation should be replaced with the contents of this feature.
   * @generated */
  public String getReplacement() {
    if (SuggestedAction_Type.featOkTst && ((SuggestedAction_Type)jcasType).casFeat_replacement == null)
      jcasType.jcas.throwFeatMissing("replacement", "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SuggestedAction");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SuggestedAction_Type)jcasType).casFeatCode_replacement);}
    
  /** setter for replacement - sets The text covered by the Anomaly annotation should be replaced with the contents of this feature. 
   * @generated */
  public void setReplacement(String v) {
    if (SuggestedAction_Type.featOkTst && ((SuggestedAction_Type)jcasType).casFeat_replacement == null)
      jcasType.jcas.throwFeatMissing("replacement", "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SuggestedAction");
    jcasType.ll_cas.ll_setStringValue(addr, ((SuggestedAction_Type)jcasType).casFeatCode_replacement, v);}    
   
    
  //*--------------*
  //* Feature: certainty

  /** getter for certainty - gets A score representing how certain is this suggested action.
Usually in [0,1].
   * @generated */
  public float getCertainty() {
    if (SuggestedAction_Type.featOkTst && ((SuggestedAction_Type)jcasType).casFeat_certainty == null)
      jcasType.jcas.throwFeatMissing("certainty", "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SuggestedAction");
    return jcasType.ll_cas.ll_getFloatValue(addr, ((SuggestedAction_Type)jcasType).casFeatCode_certainty);}
    
  /** setter for certainty - sets A score representing how certain is this suggested action.
Usually in [0,1]. 
   * @generated */
  public void setCertainty(float v) {
    if (SuggestedAction_Type.featOkTst && ((SuggestedAction_Type)jcasType).casFeat_certainty == null)
      jcasType.jcas.throwFeatMissing("certainty", "de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.SuggestedAction");
    jcasType.ll_cas.ll_setFloatValue(addr, ((SuggestedAction_Type)jcasType).casFeatCode_certainty, v);}    
  }

    