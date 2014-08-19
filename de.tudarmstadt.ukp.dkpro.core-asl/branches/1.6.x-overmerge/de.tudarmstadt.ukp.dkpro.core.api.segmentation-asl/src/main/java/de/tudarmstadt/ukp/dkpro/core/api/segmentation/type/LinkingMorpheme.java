

/* First created by JCasGen Mon May 13 18:03:33 CEST 2013 */
package de.tudarmstadt.ukp.dkpro.core.api.segmentation.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/** A part of a decompouding word which is a Linking Morpheme.
 * Updated by JCasGen Mon May 13 18:03:33 CEST 2013
 * XML source: /home/likewise-open/UKP/santos/workspace/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.api.segmentation-asl/src/main/resources/desc/type/LexicalUnits.xml
 * @generated */
public class LinkingMorpheme extends Split {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(LinkingMorpheme.class);
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
  protected LinkingMorpheme() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public LinkingMorpheme(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public LinkingMorpheme(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public LinkingMorpheme(JCas jcas, int begin, int end) {
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

    