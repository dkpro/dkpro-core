

/* First created by JCasGen Sun Nov 21 16:57:51 CET 2010 */
package de.tudarmstadt.ukp.dkpro.core.api.structure.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Sun Nov 21 16:57:51 CET 2010
 * XML source: /Users/bluefire/UKP/Workspaces/dkpro-primary/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.api.structure/src/main/resources/desc/type/Structure.xml
 * @generated */
public class Field extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Field.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Field() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Field(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Field(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Field(JCas jcas, int begin, int end) {
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
  //* Feature: name

  /** getter for name - gets the name of the tag
   * @generated */
  public String getName() {
    if (Field_Type.featOkTst && ((Field_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "de.tudarmstadt.ukp.dkpro.core.api.structure.type.Field");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Field_Type)jcasType).casFeatCode_name);}
    
  /** setter for name - sets the name of the tag 
   * @generated */
  public void setName(String v) {
    if (Field_Type.featOkTst && ((Field_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "de.tudarmstadt.ukp.dkpro.core.api.structure.type.Field");
    jcasType.ll_cas.ll_setStringValue(addr, ((Field_Type)jcasType).casFeatCode_name, v);}    
  }

    