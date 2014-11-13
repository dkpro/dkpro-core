

/* First created by JCasGen Sun Nov 02 17:37:00 CET 2014 */
package de.tudarmstadt.ukp.dkpro.core.mecab.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;


/** 
 * Updated by JCasGen Sun Nov 02 17:37:00 CET 2014
 * XML source: /Users/bluefire/UKP/Workspaces/dkpro-juno/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.mecab-asl/src/main/resources/desc/type/JapaneseToken.xml
 * @generated */
public class JapaneseToken extends Token {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(JapaneseToken.class);
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
 
  /** Never called.  Disable default constructor
   * @generated */
  protected JapaneseToken() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public JapaneseToken(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public JapaneseToken(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public JapaneseToken(JCas jcas, int begin, int end) {
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
  //* Feature: kana

  /** getter for kana - gets 
   * @generated
   * @return value of the feature 
   */
  public String getKana() {
    if (JapaneseToken_Type.featOkTst && ((JapaneseToken_Type)jcasType).casFeat_kana == null)
      jcasType.jcas.throwFeatMissing("kana", "de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken");
    return jcasType.ll_cas.ll_getStringValue(addr, ((JapaneseToken_Type)jcasType).casFeatCode_kana);}
    
  /** setter for kana - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setKana(String v) {
    if (JapaneseToken_Type.featOkTst && ((JapaneseToken_Type)jcasType).casFeat_kana == null)
      jcasType.jcas.throwFeatMissing("kana", "de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken");
    jcasType.ll_cas.ll_setStringValue(addr, ((JapaneseToken_Type)jcasType).casFeatCode_kana, v);}    
   
    
  //*--------------*
  //* Feature: ibo

  /** getter for ibo - gets 
   * @generated
   * @return value of the feature 
   */
  public String getIbo() {
    if (JapaneseToken_Type.featOkTst && ((JapaneseToken_Type)jcasType).casFeat_ibo == null)
      jcasType.jcas.throwFeatMissing("ibo", "de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken");
    return jcasType.ll_cas.ll_getStringValue(addr, ((JapaneseToken_Type)jcasType).casFeatCode_ibo);}
    
  /** setter for ibo - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setIbo(String v) {
    if (JapaneseToken_Type.featOkTst && ((JapaneseToken_Type)jcasType).casFeat_ibo == null)
      jcasType.jcas.throwFeatMissing("ibo", "de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken");
    jcasType.ll_cas.ll_setStringValue(addr, ((JapaneseToken_Type)jcasType).casFeatCode_ibo, v);}    
   
    
  //*--------------*
  //* Feature: kei

  /** getter for kei - gets 
   * @generated
   * @return value of the feature 
   */
  public String getKei() {
    if (JapaneseToken_Type.featOkTst && ((JapaneseToken_Type)jcasType).casFeat_kei == null)
      jcasType.jcas.throwFeatMissing("kei", "de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken");
    return jcasType.ll_cas.ll_getStringValue(addr, ((JapaneseToken_Type)jcasType).casFeatCode_kei);}
    
  /** setter for kei - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setKei(String v) {
    if (JapaneseToken_Type.featOkTst && ((JapaneseToken_Type)jcasType).casFeat_kei == null)
      jcasType.jcas.throwFeatMissing("kei", "de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken");
    jcasType.ll_cas.ll_setStringValue(addr, ((JapaneseToken_Type)jcasType).casFeatCode_kei, v);}    
   
    
  //*--------------*
  //* Feature: dan

  /** getter for dan - gets Specifies the kind of the verb if the current token is a verb. Either it is a vowel stem verb (ichi-dan) or a consonant stem verb (go-dan). Blank if not a verb.
   * @generated
   * @return value of the feature 
   */
  public String getDan() {
    if (JapaneseToken_Type.featOkTst && ((JapaneseToken_Type)jcasType).casFeat_dan == null)
      jcasType.jcas.throwFeatMissing("dan", "de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken");
    return jcasType.ll_cas.ll_getStringValue(addr, ((JapaneseToken_Type)jcasType).casFeatCode_dan);}
    
  /** setter for dan - sets Specifies the kind of the verb if the current token is a verb. Either it is a vowel stem verb (ichi-dan) or a consonant stem verb (go-dan). Blank if not a verb. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setDan(String v) {
    if (JapaneseToken_Type.featOkTst && ((JapaneseToken_Type)jcasType).casFeat_dan == null)
      jcasType.jcas.throwFeatMissing("dan", "de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken");
    jcasType.ll_cas.ll_setStringValue(addr, ((JapaneseToken_Type)jcasType).casFeatCode_dan, v);}    
  }

    