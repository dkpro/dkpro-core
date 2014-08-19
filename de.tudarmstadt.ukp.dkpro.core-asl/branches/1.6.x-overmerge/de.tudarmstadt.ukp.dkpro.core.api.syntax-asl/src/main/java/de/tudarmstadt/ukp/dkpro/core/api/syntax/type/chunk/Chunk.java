

/* First created by JCasGen Fri Nov 19 23:18:25 CET 2010 */
package de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Fri Nov 19 23:18:25 CET 2010
 * XML source: /Users/bluefire/UKP/Workspaces/dkpro-primary/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.type.synunit/src/main/resources/desc/type/SyntacticUnits.xml
 * @generated */
public class Chunk extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(Chunk.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Chunk() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Chunk(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Chunk(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Chunk(JCas jcas, int begin, int end) {
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
  //* Feature: chunkValue

  /** getter for chunkValue - gets 
   * @generated */
  public String getChunkValue() {
    if (Chunk_Type.featOkTst && ((Chunk_Type)jcasType).casFeat_chunkValue == null)
      jcasType.jcas.throwFeatMissing("chunkValue", "de.tudarmstadt.ukp.dkpro.core.type.synunit.chunk.Chunk");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Chunk_Type)jcasType).casFeatCode_chunkValue);}
    
  /** setter for chunkValue - sets  
   * @generated */
  public void setChunkValue(String v) {
    if (Chunk_Type.featOkTst && ((Chunk_Type)jcasType).casFeat_chunkValue == null)
      jcasType.jcas.throwFeatMissing("chunkValue", "de.tudarmstadt.ukp.dkpro.core.type.synunit.chunk.Chunk");
    jcasType.ll_cas.ll_setStringValue(addr, ((Chunk_Type)jcasType).casFeatCode_chunkValue, v);}    
  }

    