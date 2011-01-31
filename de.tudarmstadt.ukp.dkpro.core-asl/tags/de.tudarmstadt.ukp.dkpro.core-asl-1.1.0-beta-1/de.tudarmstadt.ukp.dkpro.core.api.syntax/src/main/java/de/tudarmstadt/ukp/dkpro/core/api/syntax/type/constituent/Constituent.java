

/* First created by JCasGen Mon Nov 22 18:16:12 CET 2010 */
package de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Mon Nov 22 18:16:12 CET 2010
 * XML source: /Users/bluefire/UKP/Workspaces/dkpro-primary/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.api.syntax/src/main/resources/desc/type/Constituent.xml
 * @generated */
public class Constituent extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Constituent.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Constituent() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Constituent(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Constituent(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Constituent(JCas jcas, int begin, int end) {
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
  //* Feature: ConstituentType

  /** getter for ConstituentType - gets 
   * @generated */
  public String getConstituentType() {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_ConstituentType == null)
      jcasType.jcas.throwFeatMissing("ConstituentType", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Constituent_Type)jcasType).casFeatCode_ConstituentType);}
    
  /** setter for ConstituentType - sets  
   * @generated */
  public void setConstituentType(String v) {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_ConstituentType == null)
      jcasType.jcas.throwFeatMissing("ConstituentType", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    jcasType.ll_cas.ll_setStringValue(addr, ((Constituent_Type)jcasType).casFeatCode_ConstituentType, v);}    
   
    
  //*--------------*
  //* Feature: Parent

  /** getter for Parent - gets The parent constituent
   * @generated */
  public Annotation getParent() {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_Parent == null)
      jcasType.jcas.throwFeatMissing("Parent", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    return (Annotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Constituent_Type)jcasType).casFeatCode_Parent)));}
    
  /** setter for Parent - sets The parent constituent 
   * @generated */
  public void setParent(Annotation v) {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_Parent == null)
      jcasType.jcas.throwFeatMissing("Parent", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    jcasType.ll_cas.ll_setRefValue(addr, ((Constituent_Type)jcasType).casFeatCode_Parent, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: Children

  /** getter for Children - gets 
   * @generated */
  public FSArray getChildren() {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_Children == null)
      jcasType.jcas.throwFeatMissing("Children", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Constituent_Type)jcasType).casFeatCode_Children)));}
    
  /** setter for Children - sets  
   * @generated */
  public void setChildren(FSArray v) {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_Children == null)
      jcasType.jcas.throwFeatMissing("Children", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    jcasType.ll_cas.ll_setRefValue(addr, ((Constituent_Type)jcasType).casFeatCode_Children, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for Children - gets an indexed value - 
   * @generated */
  public Annotation getChildren(int i) {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_Children == null)
      jcasType.jcas.throwFeatMissing("Children", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Constituent_Type)jcasType).casFeatCode_Children), i);
    return (Annotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Constituent_Type)jcasType).casFeatCode_Children), i)));}

  /** indexed setter for Children - sets an indexed value - 
   * @generated */
  public void setChildren(int i, Annotation v) { 
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_Children == null)
      jcasType.jcas.throwFeatMissing("Children", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Constituent_Type)jcasType).casFeatCode_Children), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Constituent_Type)jcasType).casFeatCode_Children), i, jcasType.ll_cas.ll_getFSRef(v));}
  }

    