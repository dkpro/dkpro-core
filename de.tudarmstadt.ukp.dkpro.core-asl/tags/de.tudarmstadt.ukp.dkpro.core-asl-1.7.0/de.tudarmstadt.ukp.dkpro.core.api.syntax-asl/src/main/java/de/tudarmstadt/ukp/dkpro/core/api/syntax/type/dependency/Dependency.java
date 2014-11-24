

/* First created by JCasGen Sun Nov 02 10:50:03 CET 2014 */
package de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.jcas.tcas.Annotation;


/** A dependency relation between two tokens. The dependency annotation begin and end offsets correspond to those of the dependent.
 * Updated by JCasGen Sun Nov 02 10:50:03 CET 2014
 * XML source: /Users/bluefire/UKP/Workspaces/dkpro-juno/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.api.syntax-asl/src/main/resources/desc/type/Dependency_customized.xml
 * @generated */
public class Dependency extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Dependency.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Dependency() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Dependency(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Dependency(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Dependency(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: Governor

  /** getter for Governor - gets The governor word
   * @generated
   * @return value of the feature 
   */
  public Token getGovernor() {
    if (Dependency_Type.featOkTst && ((Dependency_Type)jcasType).casFeat_Governor == null)
      jcasType.jcas.throwFeatMissing("Governor", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency");
    return (Token)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Dependency_Type)jcasType).casFeatCode_Governor)));}
    
  /** setter for Governor - sets The governor word 
   * @generated
   * @param v value to set into the feature 
   */
  public void setGovernor(Token v) {
    if (Dependency_Type.featOkTst && ((Dependency_Type)jcasType).casFeat_Governor == null)
      jcasType.jcas.throwFeatMissing("Governor", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency");
    jcasType.ll_cas.ll_setRefValue(addr, ((Dependency_Type)jcasType).casFeatCode_Governor, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: Dependent

  /** getter for Dependent - gets The dependent word
   * @generated
   * @return value of the feature 
   */
  public Token getDependent() {
    if (Dependency_Type.featOkTst && ((Dependency_Type)jcasType).casFeat_Dependent == null)
      jcasType.jcas.throwFeatMissing("Dependent", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency");
    return (Token)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Dependency_Type)jcasType).casFeatCode_Dependent)));}
    
  /** setter for Dependent - sets The dependent word 
   * @generated
   * @param v value to set into the feature 
   */
  public void setDependent(Token v) {
    if (Dependency_Type.featOkTst && ((Dependency_Type)jcasType).casFeat_Dependent == null)
      jcasType.jcas.throwFeatMissing("Dependent", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency");
    jcasType.ll_cas.ll_setRefValue(addr, ((Dependency_Type)jcasType).casFeatCode_Dependent, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: DependencyType

  /** getter for DependencyType - gets The dependency type
   * @generated
   * @return value of the feature 
   */
  public String getDependencyType() {
    if (Dependency_Type.featOkTst && ((Dependency_Type)jcasType).casFeat_DependencyType == null)
      jcasType.jcas.throwFeatMissing("DependencyType", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Dependency_Type)jcasType).casFeatCode_DependencyType);}
    
  /** setter for DependencyType - sets The dependency type 
   * @generated
   * @param v value to set into the feature 
   */
  public void setDependencyType(String v) {
    if (Dependency_Type.featOkTst && ((Dependency_Type)jcasType).casFeat_DependencyType == null)
      jcasType.jcas.throwFeatMissing("DependencyType", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency");
    jcasType.ll_cas.ll_setStringValue(addr, ((Dependency_Type)jcasType).casFeatCode_DependencyType, v);}    
  }

    