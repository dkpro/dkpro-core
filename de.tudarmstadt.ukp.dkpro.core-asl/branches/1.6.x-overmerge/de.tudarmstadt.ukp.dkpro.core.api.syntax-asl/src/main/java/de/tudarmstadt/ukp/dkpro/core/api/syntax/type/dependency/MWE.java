

/* First created by JCasGen Thu Nov 24 12:01:15 CET 2011 */
package de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Thu Nov 24 12:01:21 CET 2011
 * XML source: /home/c_toprak/workspace/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.api.syntax/src/main/resources/desc/type/Dependency.xml
 * @generated */
public class MWE extends Dependency {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(MWE.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected MWE() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public MWE(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public MWE(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public MWE(JCas jcas, int begin, int end) {
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

    