

/* First created by JCasGen Tue Nov 06 15:58:03 CET 2012 */
package de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** 
 * Updated by JCasGen Tue Nov 06 16:00:54 CET 2012
 * XML source: /Users/bluefire/UKP/Workspaces/dkpro-juno/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.api.syntax-asl/src/main/resources/desc/type/Constituency.xml
 * @generated */
public class NX extends Constituent {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(NX.class);
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
  protected NX() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public NX(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public NX(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public NX(JCas jcas, int begin, int end) {
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
     
}

    