

/* First created by JCasGen Mon Nov 22 18:37:44 CET 2010 */
package de.tudarmstadt.ukp.dkpro.core.api.syntax.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** The Penn Treebank-style phrase structure string
 * Updated by JCasGen Mon Nov 22 18:37:44 CET 2010
 * XML source: /Users/bluefire/UKP/Workspaces/dkpro-primary/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.api.syntax/src/main/resources/desc/type/PennTree.xml
 * @generated */
public class PennTree extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(PennTree.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected PennTree() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public PennTree(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public PennTree(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public PennTree(JCas jcas, int begin, int end) {
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
  //* Feature: PennTree

  /** getter for PennTree - gets Contains a Penn Treebank-style representation of a tree.
   * @generated */
  public String getPennTree() {
    if (PennTree_Type.featOkTst && ((PennTree_Type)jcasType).casFeat_PennTree == null)
      jcasType.jcas.throwFeatMissing("PennTree", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree");
    return jcasType.ll_cas.ll_getStringValue(addr, ((PennTree_Type)jcasType).casFeatCode_PennTree);}
    
  /** setter for PennTree - sets Contains a Penn Treebank-style representation of a tree. 
   * @generated */
  public void setPennTree(String v) {
    if (PennTree_Type.featOkTst && ((PennTree_Type)jcasType).casFeat_PennTree == null)
      jcasType.jcas.throwFeatMissing("PennTree", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree");
    jcasType.ll_cas.ll_setStringValue(addr, ((PennTree_Type)jcasType).casFeatCode_PennTree, v);}    
   
    
  //*--------------*
  //* Feature: TransformationNames

  /** getter for TransformationNames - gets The name(s) of the transformation(s) that have been performed on the PennTree
   * @generated */
  public String getTransformationNames() {
    if (PennTree_Type.featOkTst && ((PennTree_Type)jcasType).casFeat_TransformationNames == null)
      jcasType.jcas.throwFeatMissing("TransformationNames", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree");
    return jcasType.ll_cas.ll_getStringValue(addr, ((PennTree_Type)jcasType).casFeatCode_TransformationNames);}
    
  /** setter for TransformationNames - sets The name(s) of the transformation(s) that have been performed on the PennTree 
   * @generated */
  public void setTransformationNames(String v) {
    if (PennTree_Type.featOkTst && ((PennTree_Type)jcasType).casFeat_TransformationNames == null)
      jcasType.jcas.throwFeatMissing("TransformationNames", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree");
    jcasType.ll_cas.ll_setStringValue(addr, ((PennTree_Type)jcasType).casFeatCode_TransformationNames, v);}    
  }

    