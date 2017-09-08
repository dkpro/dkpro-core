

   
/* Apache UIMA v3 - First created by JCasGen Fri Sep 08 09:57:05 EEST 2017 */

package de.tudarmstadt.ukp.dkpro.core.api.coref.type;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.impl.TypeSystemImpl;
import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;


import org.apache.uima.jcas.cas.AnnotationBase;


/** Marks the beginning of a chain.
 * Updated by JCasGen Fri Sep 08 09:57:05 EEST 2017
 * XML source: /Users/bluefire/git/dkpro-core/dkpro-core-api-coref-asl/target/jcasgen/typesystem.xml
 * @generated */
public class CoreferenceChain extends AnnotationBase {
 
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static String _TypeName = "de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain";
  
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(CoreferenceChain.class);
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
 
 
  /* *******************
   *   Feature Offsets *
   * *******************/ 
   
  public final static String _FeatName_first = "first";


  /* Feature Adjusted Offsets */
  public final static int _FI_first = TypeSystemImpl.getAdjustedFeatureOffset("first");

   
  /** Never called.  Disable default constructor
   * @generated */
  protected CoreferenceChain() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param casImpl the CAS this Feature Structure belongs to
   * @param type the type of this Feature Structure 
   */
  public CoreferenceChain(TypeImpl type, CASImpl casImpl) {
    super(type, casImpl);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public CoreferenceChain(JCas jcas) {
    super(jcas);
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
  //* Feature: first

  /** getter for first - gets This is the first corefernce link in coreference chain
   * @generated
   * @return value of the feature 
   */
  public CoreferenceLink getFirst() { return (CoreferenceLink)(_getFeatureValueNc(_FI_first));}
    
  /** setter for first - sets This is the first corefernce link in coreference chain 
   * @generated
   * @param v value to set into the feature 
   */
  public void setFirst(CoreferenceLink v) {
    _setFeatureValueNcWj(_FI_first, v);
  }    
    
  public List<CoreferenceLink> links() {
    List<CoreferenceLink> links = new ArrayList<CoreferenceLink>();  
    CoreferenceLink l = getFirst();
    while (l != null) {
      links.add(l);
      l = l.getNext();
    }
    return links;
  }
}

    