

/* First created by JCasGen Wed Nov 28 21:03:03 CET 2012 */
package de.tudarmstadt.ukp.dkpro.core.api.srl.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;


/** The predicate or main verb of a sentence.
 * Updated by JCasGen Wed Nov 28 21:03:03 CET 2012
 * XML source: /Users/bluefire/UKP/Workspaces/dkpro-juno/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.api.srl-asl/src/main/resources/desc/type/SemanticRoleLabeling.xml
 * @generated */
public class SemanticPredicate extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(SemanticPredicate.class);
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
  protected SemanticPredicate() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public SemanticPredicate(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public SemanticPredicate(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public SemanticPredicate(JCas jcas, int begin, int end) {
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
  //* Feature: category

  /** getter for category - gets A more detailed specification of the predicate type depending on the theory being used, e.g. a frame name.
   * @generated */
  public String getCategory() {
    if (SemanticPredicate_Type.featOkTst && ((SemanticPredicate_Type)jcasType).casFeat_category == null)
      jcasType.jcas.throwFeatMissing("category", "de.tudarmstadt.ukp.dkpro.core.api.srl.type.SemanticPredicate");
    return jcasType.ll_cas.ll_getStringValue(addr, ((SemanticPredicate_Type)jcasType).casFeatCode_category);}
    
  /** setter for category - sets A more detailed specification of the predicate type depending on the theory being used, e.g. a frame name. 
   * @generated */
  public void setCategory(String v) {
    if (SemanticPredicate_Type.featOkTst && ((SemanticPredicate_Type)jcasType).casFeat_category == null)
      jcasType.jcas.throwFeatMissing("category", "de.tudarmstadt.ukp.dkpro.core.api.srl.type.SemanticPredicate");
    jcasType.ll_cas.ll_setStringValue(addr, ((SemanticPredicate_Type)jcasType).casFeatCode_category, v);}    
   
    
  //*--------------*
  //* Feature: arguments

  /** getter for arguments - gets The predicate's arguments.
   * @generated */
  public FSArray getArguments() {
    if (SemanticPredicate_Type.featOkTst && ((SemanticPredicate_Type)jcasType).casFeat_arguments == null)
      jcasType.jcas.throwFeatMissing("arguments", "de.tudarmstadt.ukp.dkpro.core.api.srl.type.SemanticPredicate");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((SemanticPredicate_Type)jcasType).casFeatCode_arguments)));}
    
  /** setter for arguments - sets The predicate's arguments. 
   * @generated */
  public void setArguments(FSArray v) {
    if (SemanticPredicate_Type.featOkTst && ((SemanticPredicate_Type)jcasType).casFeat_arguments == null)
      jcasType.jcas.throwFeatMissing("arguments", "de.tudarmstadt.ukp.dkpro.core.api.srl.type.SemanticPredicate");
    jcasType.ll_cas.ll_setRefValue(addr, ((SemanticPredicate_Type)jcasType).casFeatCode_arguments, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for arguments - gets an indexed value - The predicate's arguments.
   * @generated */
  public SemanticArgument getArguments(int i) {
    if (SemanticPredicate_Type.featOkTst && ((SemanticPredicate_Type)jcasType).casFeat_arguments == null)
      jcasType.jcas.throwFeatMissing("arguments", "de.tudarmstadt.ukp.dkpro.core.api.srl.type.SemanticPredicate");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((SemanticPredicate_Type)jcasType).casFeatCode_arguments), i);
    return (SemanticArgument)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((SemanticPredicate_Type)jcasType).casFeatCode_arguments), i)));}

  /** indexed setter for arguments - sets an indexed value - The predicate's arguments.
   * @generated */
  public void setArguments(int i, SemanticArgument v) { 
    if (SemanticPredicate_Type.featOkTst && ((SemanticPredicate_Type)jcasType).casFeat_arguments == null)
      jcasType.jcas.throwFeatMissing("arguments", "de.tudarmstadt.ukp.dkpro.core.api.srl.type.SemanticPredicate");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((SemanticPredicate_Type)jcasType).casFeatCode_arguments), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((SemanticPredicate_Type)jcasType).casFeatCode_arguments), i, jcasType.ll_cas.ll_getFSRef(v));}
  }

    