

/* First created by JCasGen Wed Aug 21 10:12:09 CEST 2013 */
package de.tudarmstadt.ukp.dkpro.core.api.semantics.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** The semantic field a noun, verb or adjective belongs to. This is a coarse-grained semantic category and this information is available, e.g., in WordNet.
 * Updated by JCasGen Wed Aug 21 10:12:09 CEST 2013
 * XML source: /home/user-ukp/workspace/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.api.semantics-asl/src/main/resources/desc/type/Semantics.xml
 * @generated */
public class SemanticField extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(SemanticField.class);
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
  protected SemanticField() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public SemanticField(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public SemanticField(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public SemanticField(JCas jcas, int begin, int end) {
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
  //* Feature: value

  /** getter for value - gets The value or name of the semantic field. Examples of semantic field values are: location, artifact, event, communication, attribute
   * @generated */
  public String getValue() {
    if (SemanticField_Type.featOkTst && ((SemanticField_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticField");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SemanticField_Type)jcasType).casFeatCode_value);}
    
  /** setter for value - sets The value or name of the semantic field. Examples of semantic field values are: location, artifact, event, communication, attribute 
   * @generated */
  public void setValue(String v) {
    if (SemanticField_Type.featOkTst && ((SemanticField_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemanticField");
    jcasType.ll_cas.ll_setStringValue(addr, ((SemanticField_Type)jcasType).casFeatCode_value, v);}    
  }

    