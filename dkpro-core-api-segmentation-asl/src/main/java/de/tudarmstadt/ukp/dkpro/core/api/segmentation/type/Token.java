

   
/* Apache UIMA v3 - First created by JCasGen Sun Jan 28 11:38:37 CET 2018 */

package de.tudarmstadt.ukp.dkpro.core.api.segmentation.type;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;

import org.apache.uima.cas.CASException;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.impl.TypeSystemImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.tcas.Annotation;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;


/** <p>Token is one of the two types commonly produced by a segmenter (the other being Sentence). A Token usually represents a word, although it may be used to represent multiple tightly connected words (e.g. "New York") or parts of a word (e.g. the possessive "'s"). One may choose to split compound words into multiple tokens, e.g. ("CamelCase" -&gt; "Camel", "Case"; "Zauberstab" -&gt; "Zauber", "stab"). Most processing components operate on Tokens, usually within the limits of the surrounding Sentence. E.g. a part-of-speech tagger analyses each Token in a Sentence and assigns a part-of-speech to each Token.</p>
 * Updated by JCasGen Sun Jan 28 11:38:37 CET 2018
 * XML source: /Users/bluefire/git/dkpro-core/dkpro-core-api-segmentation-asl/src/main/resources/desc/type/LexicalUnits_customized.xml
 * @generated */
public class Token extends Annotation {
 
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static String _TypeName = "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token";
  
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
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
 
  /* *******************
   *   Feature Offsets *
   * *******************/ 
   
  public final static String _FeatName_parent = "parent";
  public final static String _FeatName_lemma = "lemma";
  public final static String _FeatName_stem = "stem";
  public final static String _FeatName_pos = "pos";
  public final static String _FeatName_morph = "morph";
  public final static String _FeatName_id = "id";
  public final static String _FeatName_form = "form";
  public final static String _FeatName_syntacticFunction = "syntacticFunction";


  /* Feature Adjusted Offsets */
  private final static CallSite _FC_parent = TypeSystemImpl.createCallSite(Token.class, "parent");
  private final static MethodHandle _FH_parent = _FC_parent.dynamicInvoker();
  private final static CallSite _FC_lemma = TypeSystemImpl.createCallSite(Token.class, "lemma");
  private final static MethodHandle _FH_lemma = _FC_lemma.dynamicInvoker();
  private final static CallSite _FC_stem = TypeSystemImpl.createCallSite(Token.class, "stem");
  private final static MethodHandle _FH_stem = _FC_stem.dynamicInvoker();
  private final static CallSite _FC_pos = TypeSystemImpl.createCallSite(Token.class, "pos");
  private final static MethodHandle _FH_pos = _FC_pos.dynamicInvoker();
  private final static CallSite _FC_morph = TypeSystemImpl.createCallSite(Token.class, "morph");
  private final static MethodHandle _FH_morph = _FC_morph.dynamicInvoker();
  private final static CallSite _FC_id = TypeSystemImpl.createCallSite(Token.class, "id");
  private final static MethodHandle _FH_id = _FC_id.dynamicInvoker();
  private final static CallSite _FC_form = TypeSystemImpl.createCallSite(Token.class, "form");
  private final static MethodHandle _FH_form = _FC_form.dynamicInvoker();
  private final static CallSite _FC_syntacticFunction = TypeSystemImpl.createCallSite(Token.class, "syntacticFunction");
  private final static MethodHandle _FH_syntacticFunction = _FC_syntacticFunction.dynamicInvoker();

   
  /** Never called.  Disable default constructor
   * @generated */
  protected Token() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param casImpl the CAS this Feature Structure belongs to
   * @param type the type of this Feature Structure 
   */
  public Token(TypeImpl type, CASImpl casImpl) {
    super(type, casImpl);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Token(JCas jcas) {
    super(jcas);
    readObject();   
  } 


  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
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
  public Annotation getParent() { return (Annotation)(_getFeatureValueNc(wrapGetIntCatchException(_FH_parent)));}
    
  /** setter for parent - sets the parent of this token. This feature is meant to be used in when the token participates in a constituency parse and then refers to a constituent containing this token. The type of this feature is {@link Annotation} to avoid adding a dependency on the syntax API module. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setParent(Annotation v) {
    _setFeatureValueNcWj(wrapGetIntCatchException(_FH_parent), v);
  }    
    
   
    
  //*--------------*
  //* Feature: lemma

  /** getter for lemma - gets 
   * @generated
   * @return value of the feature 
   */
  public Lemma getLemma() { return (Lemma)(_getFeatureValueNc(wrapGetIntCatchException(_FH_lemma)));}
    
  /** setter for lemma - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setLemma(Lemma v) {
    _setFeatureValueNcWj(wrapGetIntCatchException(_FH_lemma), v);
  }    
    
   
    
  //*--------------*
  //* Feature: stem

  /** getter for stem - gets 
   * @generated
   * @return value of the feature 
   */
  public Stem getStem() { return (Stem)(_getFeatureValueNc(wrapGetIntCatchException(_FH_stem)));}
    
  /** setter for stem - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setStem(Stem v) {
    _setFeatureValueNcWj(wrapGetIntCatchException(_FH_stem), v);
  }    
    
   
    
  //*--------------*
  //* Feature: pos

  /** getter for pos - gets 
   * @generated
   * @return value of the feature 
   */
  public POS getPos() { return (POS)(_getFeatureValueNc(wrapGetIntCatchException(_FH_pos)));}
    
  /** setter for pos - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPos(POS v) {
    _setFeatureValueNcWj(wrapGetIntCatchException(_FH_pos), v);
  }    
    
   
    
  //*--------------*
  //* Feature: morph

  /** getter for morph - gets The morphological feature associated with this token.
   * @generated
   * @return value of the feature 
   */
  public MorphologicalFeatures getMorph() { return (MorphologicalFeatures)(_getFeatureValueNc(wrapGetIntCatchException(_FH_morph)));}
    
  /** setter for morph - sets The morphological feature associated with this token. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setMorph(MorphologicalFeatures v) {
    _setFeatureValueNcWj(wrapGetIntCatchException(_FH_morph), v);
  }    
    
   
    
  //*--------------*
  //* Feature: id

  /** getter for id - gets If this unit had an ID in the source format from which it was imported, it may be stored here. IDs are typically not assigned by DKPro Core components. If an ID is present, it should be respected by writers.
   * @generated
   * @return value of the feature 
   */
  public String getId() { return _getStringValueNc(wrapGetIntCatchException(_FH_id));}
    
  /** setter for id - sets If this unit had an ID in the source format from which it was imported, it may be stored here. IDs are typically not assigned by DKPro Core components. If an ID is present, it should be respected by writers. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setId(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_id), v);
  }    
    
   
    
  //*--------------*
  //* Feature: form

  /** getter for form - gets Potentially normalized form of the token text that should be used instead of the covered text if set.
   * @generated
   * @return value of the feature 
   */
  public TokenForm getForm() { return (TokenForm)(_getFeatureValueNc(wrapGetIntCatchException(_FH_form)));}
    
  /** setter for form - sets Potentially normalized form of the token text that should be used instead of the covered text if set. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setForm(TokenForm v) {
    _setFeatureValueNcWj(wrapGetIntCatchException(_FH_form), v);
  }    
    
   
    
  //*--------------*
  //* Feature: syntacticFunction

  /** getter for syntacticFunction - gets 
   * @generated
   * @return value of the feature 
   */
  public String getSyntacticFunction() { return _getStringValueNc(wrapGetIntCatchException(_FH_syntacticFunction));}
    
  /** setter for syntacticFunction - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSyntacticFunction(String v) {
    _setStringValueNfc(wrapGetIntCatchException(_FH_syntacticFunction), v);
  }    

  /**
   * @return the POS value if there is a {@link POS} annotation linked to this token.
   */
  public String getPosValue() {
      POS pos = getPos();
      return pos != null ? pos.getPosValue() : null;
  }
  
  /**
   * @return the stem value if there is a {@link Stem} annotation linked to this token.
   */
  public String getStemValue() {
      Stem stem = getStem();
      return stem != null ? stem.getValue() : null;
  }

  /**
   * @return the lemma value if there is a {@link Lemma} annotation linked to this token.
   */
  public String getLemmaValue() {
      Lemma lemma = getLemma();
      return lemma != null ? lemma.getValue() : null;
  }
  
  /**
   * @return the form value if there is a {@link TokenForm} annotation linked to this token.
   */
  public String getFormValue() {
      TokenForm form = getForm();
      return form != null ? form.getValue() : null;
  }
  
  /**
   * @return the token text taking into account a {@link TokenForm} annotation that might exist.
   */
  public String getText() {
      String form = getFormValue();
      return form != null ? form : getCoveredText();
  }

  /**
   * Set the token text. Depending on whether the text is different to the covered text a 
   * {@link TokenForm} annotation is created or not. This method can only be used if the document
   * text has already been set. If a document text is constructed incrementally, 
   * {@link #setForm(TokenForm)} has to be called manually and whether or not a form is necessary
   * needs to be determined based on the current state of the document text being built.
   * 
   * @param aText the token text.
   */
  public void setText(String aText) {
      TokenForm form = getForm();
      if (aText != null && !aText.equals(getCoveredText())) {
          // Create form annotation if none is here yet
          if (form == null) {
              try {
                  form = new TokenForm(getCAS().getJCas(), getBegin(), getEnd());
              }
              catch (CASException e) {
                  // This should actually never happen since a JCas FS class should always be
                  // associated with a JCas.
                  throw new IllegalStateException(e);
              }
              form.addToIndexes();
          }

          // Set/update form
          form.setValue(aText);
          setForm(form);
      }
      else if (form != null) {
          form.removeFromIndexes();
          setForm(null);
      }
  }
}
