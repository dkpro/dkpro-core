

/* First created by JCasGen Sun Nov 21 13:42:50 CET 2010 */
package de.tudarmstadt.ukp.dkpro.core.api.segmentation.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Fri Jan 10 09:54:06 CET 2014
 * XML source: C:/Users/Seid/workspace/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.api.segmentation-asl/src/main/resources/desc/type/LexicalUnits.xml
 * @generated */
public class Sentence extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Sentence.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Sentence() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Sentence(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Sentence(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Sentence(JCas jcas, int begin, int end) {
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
  //* Feature: comment

  /** getter for comment - gets Comments to be added to a sentence such as source of the sentence obtained, id of the sentence to be preserved,...
   * @generated */
  public String getComment() {
    if (Sentence_Type.featOkTst && ((Sentence_Type)jcasType).casFeat_comment == null)
      jcasType.jcas.throwFeatMissing("comment", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Sentence_Type)jcasType).casFeatCode_comment);}
    
  /** setter for comment - sets Comments to be added to a sentence such as source of the sentence obtained, id of the sentence to be preserved,... 
   * @generated */
  public void setComment(String v) {
    if (Sentence_Type.featOkTst && ((Sentence_Type)jcasType).casFeat_comment == null)
      jcasType.jcas.throwFeatMissing("comment", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence");
    jcasType.ll_cas.ll_setStringValue(addr, ((Sentence_Type)jcasType).casFeatCode_comment, v);}    
  }

    