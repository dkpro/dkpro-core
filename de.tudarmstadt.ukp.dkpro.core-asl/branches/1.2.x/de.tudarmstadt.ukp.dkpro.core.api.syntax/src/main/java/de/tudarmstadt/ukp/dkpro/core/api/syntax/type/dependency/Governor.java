

/* First created by JCasGen Mon Nov 22 18:23:21 CET 2010 */
package de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;


/** 
 * Updated by JCasGen Mon Nov 22 18:23:21 CET 2010
 * XML source: /Users/bluefire/UKP/Workspaces/dkpro-primary/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.api.syntax/src/main/resources/desc/type/Dependency.xml
 * @generated */
public class Governor extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Governor.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Governor() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Governor(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Governor(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Governor(JCas jcas, int begin, int end) {
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
  //* Feature: Dependent

  /** getter for Dependent - gets 
   * @generated */
  public Token getDependent() {
    if (Governor_Type.featOkTst && ((Governor_Type)jcasType).casFeat_Dependent == null)
      jcasType.jcas.throwFeatMissing("Dependent", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Governor");
    return (Token)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Governor_Type)jcasType).casFeatCode_Dependent)));}
    
  /** setter for Dependent - sets  
   * @generated */
  public void setDependent(Token v) {
    if (Governor_Type.featOkTst && ((Governor_Type)jcasType).casFeat_Dependent == null)
      jcasType.jcas.throwFeatMissing("Dependent", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Governor");
    jcasType.ll_cas.ll_setRefValue(addr, ((Governor_Type)jcasType).casFeatCode_Dependent, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: Dependency

  /** getter for Dependency - gets 
   * @generated */
  public Dependency getDependency() {
    if (Governor_Type.featOkTst && ((Governor_Type)jcasType).casFeat_Dependency == null)
      jcasType.jcas.throwFeatMissing("Dependency", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Governor");
    return (Dependency)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Governor_Type)jcasType).casFeatCode_Dependency)));}
    
  /** setter for Dependency - sets  
   * @generated */
  public void setDependency(Dependency v) {
    if (Governor_Type.featOkTst && ((Governor_Type)jcasType).casFeat_Dependency == null)
      jcasType.jcas.throwFeatMissing("Dependency", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Governor");
    jcasType.ll_cas.ll_setRefValue(addr, ((Governor_Type)jcasType).casFeatCode_Dependency, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    