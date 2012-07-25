

/* First created by JCasGen Sun Nov 21 13:42:50 CET 2010 */
package de.tudarmstadt.ukp.dkpro.core.api.segmentation.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Thu Dec 09 22:13:11 CET 2010
 * XML source: /Users/bluefire/UKP/Workspaces/dkpro-primary/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.api.segmentation/src/main/resources/desc/type/LexicalUnits.xml
 * @generated */
public class Compound extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Compound.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Compound() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Compound(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Compound(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Compound(JCas jcas, int begin, int end) {
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
  //* Feature: part1

  /** getter for part1 - gets 
   * @generated */
  public String getPart1() {
    if (Compound_Type.featOkTst && ((Compound_Type)jcasType).casFeat_part1 == null)
      jcasType.jcas.throwFeatMissing("part1", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Compound_Type)jcasType).casFeatCode_part1);}
    
  /** setter for part1 - sets  
   * @generated */
  public void setPart1(String v) {
    if (Compound_Type.featOkTst && ((Compound_Type)jcasType).casFeat_part1 == null)
      jcasType.jcas.throwFeatMissing("part1", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound");
    jcasType.ll_cas.ll_setStringValue(addr, ((Compound_Type)jcasType).casFeatCode_part1, v);}    
   
    
  //*--------------*
  //* Feature: part2

  /** getter for part2 - gets 
   * @generated */
  public String getPart2() {
    if (Compound_Type.featOkTst && ((Compound_Type)jcasType).casFeat_part2 == null)
      jcasType.jcas.throwFeatMissing("part2", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Compound_Type)jcasType).casFeatCode_part2);}
    
  /** setter for part2 - sets  
   * @generated */
  public void setPart2(String v) {
    if (Compound_Type.featOkTst && ((Compound_Type)jcasType).casFeat_part2 == null)
      jcasType.jcas.throwFeatMissing("part2", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound");
    jcasType.ll_cas.ll_setStringValue(addr, ((Compound_Type)jcasType).casFeatCode_part2, v);}    
  }

    