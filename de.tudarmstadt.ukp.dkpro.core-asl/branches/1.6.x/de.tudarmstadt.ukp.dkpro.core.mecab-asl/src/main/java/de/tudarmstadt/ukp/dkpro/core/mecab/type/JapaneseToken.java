

/* First created by JCasGen Sat Jul 14 18:17:22 CEST 2012 */
package de.tudarmstadt.ukp.dkpro.core.mecab.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;


/** 
 * Updated by JCasGen Sat Jul 14 18:17:24 CEST 2012
 * XML source: /Users/bluefire/UKP/Workspaces/dkpro-indigo/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.mecab-asl/src/main/resources/desc/type/JapaneseToken.xml
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
  /** @generated  */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected JapaneseToken() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public JapaneseToken(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public JapaneseToken(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public JapaneseToken(JCas jcas, int begin, int end) {
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
  //* Feature: kana

  /** getter for kana - gets 
   * @generated */
  public String getKana() {
    if (JapaneseToken_Type.featOkTst && ((JapaneseToken_Type)jcasType).casFeat_kana == null)
      jcasType.jcas.throwFeatMissing("kana", "de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken");
    return jcasType.ll_cas.ll_getStringValue(addr, ((JapaneseToken_Type)jcasType).casFeatCode_kana);}
    
  /** setter for kana - sets  
   * @generated */
  public void setKana(String v) {
    if (JapaneseToken_Type.featOkTst && ((JapaneseToken_Type)jcasType).casFeat_kana == null)
      jcasType.jcas.throwFeatMissing("kana", "de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken");
    jcasType.ll_cas.ll_setStringValue(addr, ((JapaneseToken_Type)jcasType).casFeatCode_kana, v);}    
   
    
  //*--------------*
  //* Feature: ibo

  /** getter for ibo - gets 
   * @generated */
  public String getIbo() {
    if (JapaneseToken_Type.featOkTst && ((JapaneseToken_Type)jcasType).casFeat_ibo == null)
      jcasType.jcas.throwFeatMissing("ibo", "de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken");
    return jcasType.ll_cas.ll_getStringValue(addr, ((JapaneseToken_Type)jcasType).casFeatCode_ibo);}
    
  /** setter for ibo - sets  
   * @generated */
  public void setIbo(String v) {
    if (JapaneseToken_Type.featOkTst && ((JapaneseToken_Type)jcasType).casFeat_ibo == null)
      jcasType.jcas.throwFeatMissing("ibo", "de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken");
    jcasType.ll_cas.ll_setStringValue(addr, ((JapaneseToken_Type)jcasType).casFeatCode_ibo, v);}    
   
    
  //*--------------*
  //* Feature: kei

  /** getter for kei - gets 
   * @generated */
  public String getKei() {
    if (JapaneseToken_Type.featOkTst && ((JapaneseToken_Type)jcasType).casFeat_kei == null)
      jcasType.jcas.throwFeatMissing("kei", "de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken");
    return jcasType.ll_cas.ll_getStringValue(addr, ((JapaneseToken_Type)jcasType).casFeatCode_kei);}
    
  /** setter for kei - sets  
   * @generated */
  public void setKei(String v) {
    if (JapaneseToken_Type.featOkTst && ((JapaneseToken_Type)jcasType).casFeat_kei == null)
      jcasType.jcas.throwFeatMissing("kei", "de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken");
    jcasType.ll_cas.ll_setStringValue(addr, ((JapaneseToken_Type)jcasType).casFeatCode_kei, v);}    
   
    
  //*--------------*
  //* Feature: dan

  /** getter for dan - gets Specifies the kind of the verb if the current token is a verb. Either it is a vowel stem verb (ichi-dan) or a consonant stem verb (go-dan). Blank if not a verb.
   * @generated */
  public String getDan() {
    if (JapaneseToken_Type.featOkTst && ((JapaneseToken_Type)jcasType).casFeat_dan == null)
      jcasType.jcas.throwFeatMissing("dan", "de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken");
    return jcasType.ll_cas.ll_getStringValue(addr, ((JapaneseToken_Type)jcasType).casFeatCode_dan);}
    
  /** setter for dan - sets Specifies the kind of the verb if the current token is a verb. Either it is a vowel stem verb (ichi-dan) or a consonant stem verb (go-dan). Blank if not a verb. 
   * @generated */
  public void setDan(String v) {
    if (JapaneseToken_Type.featOkTst && ((JapaneseToken_Type)jcasType).casFeat_dan == null)
      jcasType.jcas.throwFeatMissing("dan", "de.tudarmstadt.ukp.dkpro.core.mecab.type.JapaneseToken");
    jcasType.ll_cas.ll_setStringValue(addr, ((JapaneseToken_Type)jcasType).casFeatCode_dan, v);}    
  }

    