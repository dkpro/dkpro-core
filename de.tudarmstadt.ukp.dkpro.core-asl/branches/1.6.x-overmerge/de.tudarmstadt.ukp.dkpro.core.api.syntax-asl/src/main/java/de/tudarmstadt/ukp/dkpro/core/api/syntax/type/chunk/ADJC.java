

/* First created by JCasGen Fri Nov 19 23:18:25 CET 2010 */
package de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Fri Nov 19 23:18:25 CET 2010
 * XML source: /Users/bluefire/UKP/Workspaces/dkpro-primary/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.type.synunit/src/main/resources/desc/type/SyntacticUnits.xml
 * @generated */
public class ADJC extends Chunk {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(ADJC.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected ADJC() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public ADJC(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public ADJC(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public ADJC(JCas jcas, int begin, int end) {
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
     
}

    