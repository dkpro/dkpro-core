

/* First created by JCasGen Thu Nov 25 12:03:11 CET 2010 */
package de.tudarmstadt.ukp.dkpro.core.api.syntax.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** A multi purpose tag
 * Updated by JCasGen Thu Nov 25 12:03:11 CET 2010
 * XML source: /Users/bluefire/UKP/Workspaces/dkpro-primary/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.api.syntax/src/main/resources/desc/type/Tag.xml
 * @generated */
public class Tag extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Tag.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Tag() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Tag(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Tag(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Tag(JCas jcas, int begin, int end) {
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
  //* Feature: value

  /** getter for value - gets 
   * @generated */
  public String getValue() {
    if (Tag_Type.featOkTst && ((Tag_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.Tag");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Tag_Type)jcasType).casFeatCode_value);}
    
  /** setter for value - sets  
   * @generated */
  public void setValue(String v) {
    if (Tag_Type.featOkTst && ((Tag_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.Tag");
    jcasType.ll_cas.ll_setStringValue(addr, ((Tag_Type)jcasType).casFeatCode_value, v);}    
  }

    