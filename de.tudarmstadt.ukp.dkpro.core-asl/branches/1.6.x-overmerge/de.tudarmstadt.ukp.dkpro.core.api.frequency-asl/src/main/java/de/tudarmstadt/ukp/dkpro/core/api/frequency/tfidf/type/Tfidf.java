

/* First created by JCasGen Mon Feb 25 12:43:06 CET 2013 */
package de.tudarmstadt.ukp.dkpro.core.api.frequency.tfidf.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Mon Feb 25 12:43:06 CET 2013
 * XML source: /srv/workspace42/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.api.frequency-asl/src/main/resources/desc/type/Tfidf.xml
 * @generated */
public class Tfidf extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Tfidf.class);
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
  protected Tfidf() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Tfidf(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Tfidf(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Tfidf(JCas jcas, int begin, int end) {
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
  //* Feature: tfidfValue

  /** getter for tfidfValue - gets The tf.idf score.
   * @generated */
  public double getTfidfValue() {
    if (Tfidf_Type.featOkTst && ((Tfidf_Type)jcasType).casFeat_tfidfValue == null)
      jcasType.jcas.throwFeatMissing("tfidfValue", "de.tudarmstadt.ukp.dkpro.core.api.frequency.tfidf.type.Tfidf");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((Tfidf_Type)jcasType).casFeatCode_tfidfValue);}
    
  /** setter for tfidfValue - sets The tf.idf score. 
   * @generated */
  public void setTfidfValue(double v) {
    if (Tfidf_Type.featOkTst && ((Tfidf_Type)jcasType).casFeat_tfidfValue == null)
      jcasType.jcas.throwFeatMissing("tfidfValue", "de.tudarmstadt.ukp.dkpro.core.api.frequency.tfidf.type.Tfidf");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((Tfidf_Type)jcasType).casFeatCode_tfidfValue, v);}    
   
    
  //*--------------*
  //* Feature: term

  /** getter for term - gets The string that was used to compute this tf.idf score.
If a stem or lemma was used, the covered text of this annotation does not need to be equal to this string.

This string can be used to construct a vector space with the right terms without having to access the indexes again.
   * @generated */
  public String getTerm() {
    if (Tfidf_Type.featOkTst && ((Tfidf_Type)jcasType).casFeat_term == null)
      jcasType.jcas.throwFeatMissing("term", "de.tudarmstadt.ukp.dkpro.core.api.frequency.tfidf.type.Tfidf");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Tfidf_Type)jcasType).casFeatCode_term);}
    
  /** setter for term - sets The string that was used to compute this tf.idf score.
If a stem or lemma was used, the covered text of this annotation does not need to be equal to this string.

This string can be used to construct a vector space with the right terms without having to access the indexes again. 
   * @generated */
  public void setTerm(String v) {
    if (Tfidf_Type.featOkTst && ((Tfidf_Type)jcasType).casFeat_term == null)
      jcasType.jcas.throwFeatMissing("term", "de.tudarmstadt.ukp.dkpro.core.api.frequency.tfidf.type.Tfidf");
    jcasType.ll_cas.ll_setStringValue(addr, ((Tfidf_Type)jcasType).casFeatCode_term, v);}    
  }

    