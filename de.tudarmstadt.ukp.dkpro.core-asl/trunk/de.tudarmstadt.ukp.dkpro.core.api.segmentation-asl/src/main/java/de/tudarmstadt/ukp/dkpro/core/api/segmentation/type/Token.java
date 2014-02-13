

/* First created by JCasGen Tue Dec 04 00:43:53 CET 2012 */
package de.tudarmstadt.ukp.dkpro.core.api.segmentation.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import org.apache.uima.jcas.tcas.Annotation;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;


/** Token is one of the two types commonly produced by a segmenter (the other being {@link Sentence}). A Token usually represents a word, although it may be used to represent multiple tightly connected words (e.g. "New York") or parts of a word (e.g. the possessive "'s"). One may choose to split compound words into multiple tokens, e.g. ("CamelCase" -> "Camel", "Case"; "Zauberstab" -> "Zauber", "stab"). Most processing components operate on Tokens, usually within the limits of the surrounding Sentence. E.g. a part-of-speech tagger analyses each Token in a Sentence and assigns a part-of-speech to each Token.
<p>
 * Updated by JCasGen Thu Feb 13 16:04:48 CET 2014
 * XML source: /Users/bluefire/UKP/Workspaces/dkpro-juno/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.api.segmentation-asl/src/main/resources/desc/type/LexicalUnits.xml
 * @generated */
public class Token extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Token.class);
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
  protected Token() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated */
  public Token(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public Token(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public Token(JCas jcas, int begin, int end) {
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
  //* Feature: parent

  /** getter for parent - gets the parent of this token. This feature is meant to be used in when the token participates in a constituency parse and then refers to a constituent containing this token. The type of this feature is {@link Annotation} to avoid adding a dependency on the syntax API module.
   * @generated
   * @return value of the feature 
   */
  public Annotation getParent() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_parent == null)
      jcasType.jcas.throwFeatMissing("parent", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    return (Annotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_parent)));}
    
  /** setter for parent - sets the parent of this token. This feature is meant to be used in when the token participates in a constituency parse and then refers to a constituent containing this token. The type of this feature is {@link Annotation} to avoid adding a dependency on the syntax API module. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setParent(Annotation v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_parent == null)
      jcasType.jcas.throwFeatMissing("parent", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_parent, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: lemma

  /** getter for lemma - gets 
   * @generated
   * @return value of the feature 
   */
  public Lemma getLemma() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_lemma == null)
      jcasType.jcas.throwFeatMissing("lemma", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    return (Lemma)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_lemma)));}
    
  /** setter for lemma - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setLemma(Lemma v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_lemma == null)
      jcasType.jcas.throwFeatMissing("lemma", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_lemma, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: stem

  /** getter for stem - gets 
   * @generated
   * @return value of the feature 
   */
  public Stem getStem() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_stem == null)
      jcasType.jcas.throwFeatMissing("stem", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    return (Stem)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_stem)));}
    
  /** setter for stem - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setStem(Stem v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_stem == null)
      jcasType.jcas.throwFeatMissing("stem", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_stem, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: pos

  /** getter for pos - gets 
   * @generated
   * @return value of the feature 
   */
  public POS getPos() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_pos == null)
      jcasType.jcas.throwFeatMissing("pos", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    return (POS)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_pos)));}
    
  /** setter for pos - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPos(POS v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_pos == null)
      jcasType.jcas.throwFeatMissing("pos", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_pos, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: morphologicalFeatures

  /** getter for morphologicalFeatures - gets The morphological feature associated with this token.
   * @generated
   * @return value of the feature 
   */
  public MorphologicalFeatures getMorphologicalFeatures() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_morphologicalFeatures == null)
      jcasType.jcas.throwFeatMissing("morphologicalFeatures", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    return (MorphologicalFeatures)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_morphologicalFeatures)));}
    
  /** setter for morphologicalFeatures - sets The morphological feature associated with this token. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setMorphologicalFeatures(MorphologicalFeatures v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_morphologicalFeatures == null)
      jcasType.jcas.throwFeatMissing("morphologicalFeatures", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_morphologicalFeatures, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    