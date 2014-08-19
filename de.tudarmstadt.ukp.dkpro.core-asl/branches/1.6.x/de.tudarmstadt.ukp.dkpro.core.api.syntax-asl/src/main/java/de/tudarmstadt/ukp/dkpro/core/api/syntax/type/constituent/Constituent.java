

/* First created by JCasGen Tue Nov 06 15:58:03 CET 2012 */
package de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Tue Nov 06 16:00:54 CET 2012
 * XML source: /Users/bluefire/UKP/Workspaces/dkpro-juno/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.api.syntax-asl/src/main/resources/desc/type/Constituency.xml
 * @generated */
public class Constituent extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Constituent.class);
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
  protected Constituent() {/* intentionally empty block */}
    
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
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: constituentType

  /** getter for constituentType - gets 
   * @generated */
  public String getConstituentType() {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_constituentType == null)
      jcasType.jcas.throwFeatMissing("constituentType", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Constituent_Type)jcasType).casFeatCode_constituentType);}
    
  /** setter for constituentType - sets  
   * @generated */
  public void setConstituentType(String v) {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_constituentType == null)
      jcasType.jcas.throwFeatMissing("constituentType", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    jcasType.ll_cas.ll_setStringValue(addr, ((Constituent_Type)jcasType).casFeatCode_constituentType, v);}    
   
    
  //*--------------*
  //* Feature: parent

  /** getter for parent - gets The parent constituent
   * @generated */
  public Annotation getParent() {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_parent == null)
      jcasType.jcas.throwFeatMissing("parent", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    return (Annotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Constituent_Type)jcasType).casFeatCode_parent)));}
    
  /** setter for parent - sets The parent constituent 
   * @generated */
  public void setParent(Annotation v) {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_parent == null)
      jcasType.jcas.throwFeatMissing("parent", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    jcasType.ll_cas.ll_setRefValue(addr, ((Constituent_Type)jcasType).casFeatCode_parent, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: children

  /** getter for children - gets 
   * @generated */
  public FSArray getChildren() {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_children == null)
      jcasType.jcas.throwFeatMissing("children", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Constituent_Type)jcasType).casFeatCode_children)));}
    
  /** setter for children - sets  
   * @generated */
  public void setChildren(FSArray v) {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_children == null)
      jcasType.jcas.throwFeatMissing("children", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    jcasType.ll_cas.ll_setRefValue(addr, ((Constituent_Type)jcasType).casFeatCode_children, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for children - gets an indexed value - 
   * @generated */
  public Annotation getChildren(int i) {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_children == null)
      jcasType.jcas.throwFeatMissing("children", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Constituent_Type)jcasType).casFeatCode_children), i);
    return (Annotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Constituent_Type)jcasType).casFeatCode_children), i)));}

  /** indexed setter for children - sets an indexed value - 
   * @generated */
  public void setChildren(int i, Annotation v) { 
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_children == null)
      jcasType.jcas.throwFeatMissing("children", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Constituent_Type)jcasType).casFeatCode_children), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Constituent_Type)jcasType).casFeatCode_children), i, jcasType.ll_cas.ll_getFSRef(v));}
   
    
  //*--------------*
  //* Feature: syntacticFunction

  /** getter for syntacticFunction - gets 
   * @generated */
  public String getSyntacticFunction() {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_syntacticFunction == null)
      jcasType.jcas.throwFeatMissing("syntacticFunction", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Constituent_Type)jcasType).casFeatCode_syntacticFunction);}
    
  /** setter for syntacticFunction - sets  
   * @generated */
  public void setSyntacticFunction(String v) {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_syntacticFunction == null)
      jcasType.jcas.throwFeatMissing("syntacticFunction", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    jcasType.ll_cas.ll_setStringValue(addr, ((Constituent_Type)jcasType).casFeatCode_syntacticFunction, v);}    
  }

    