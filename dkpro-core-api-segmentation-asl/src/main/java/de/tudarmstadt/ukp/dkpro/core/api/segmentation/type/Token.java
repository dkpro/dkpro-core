

   
/* Apache UIMA v3 - First created by JCasGen Fri Sep 08 10:00:52 EEST 2017 */

package de.tudarmstadt.ukp.dkpro.core.api.segmentation.type;

import org.apache.uima.cas.CASException;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.impl.TypeSystemImpl;
import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;


import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import org.apache.uima.jcas.tcas.Annotation;


/** <p>Token is one of the two types commonly produced by a segmenter (the other being Sentence). A Token usually represents a word, although it may be used to represent multiple tightly connected words (e.g. "New York") or parts of a word (e.g. the possessive "'s"). One may choose to split compound words into multiple tokens, e.g. ("CamelCase" -&gt; "Camel", "Case"; "Zauberstab" -&gt; "Zauber", "stab"). Most processing components operate on Tokens, usually within the limits of the surrounding Sentence. E.g. a part-of-speech tagger analyses each Token in a Sentence and assigns a part-of-speech to each Token.</p>
 * Updated by JCasGen Fri Sep 08 10:00:52 EEST 2017
 * XML source: /Users/bluefire/git/dkpro-core/dkpro-core-api-segmentation-asl/target/jcasgen/typesystem.xml
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
  public final static int _FI_parent = TypeSystemImpl.getAdjustedFeatureOffset("parent");
  public final static int _FI_lemma = TypeSystemImpl.getAdjustedFeatureOffset("lemma");
  public final static int _FI_stem = TypeSystemImpl.getAdjustedFeatureOffset("stem");
  public final static int _FI_pos = TypeSystemImpl.getAdjustedFeatureOffset("pos");
  public final static int _FI_morph = TypeSystemImpl.getAdjustedFeatureOffset("morph");
  public final static int _FI_id = TypeSystemImpl.getAdjustedFeatureOffset("id");
  public final static int _FI_form = TypeSystemImpl.getAdjustedFeatureOffset("form");
  public final static int _FI_syntacticFunction = TypeSystemImpl.getAdjustedFeatureOffset("syntacticFunction");

   
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
  public Annotation getParent() { return (Annotation)(_getFeatureValueNc(_FI_parent));}
    
  /** setter for parent - sets the parent of this token. This feature is meant to be used in when the token participates in a constituency parse and then refers to a constituent containing this token. The type of this feature is {@link Annotation} to avoid adding a dependency on the syntax API module. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setParent(Annotation v) {
    _setFeatureValueNcWj(_FI_parent, v);
  }    
    
   
    
  //*--------------*
  //* Feature: lemma

  /** getter for lemma - gets 
   * @generated
   * @return value of the feature 
   */
  public Lemma getLemma() { return (Lemma)(_getFeatureValueNc(_FI_lemma));}
    
  /** setter for lemma - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setLemma(Lemma v) {
    _setFeatureValueNcWj(_FI_lemma, v);
  }    
    
   
    
  //*--------------*
  //* Feature: stem

  /** getter for stem - gets 
   * @generated
   * @return value of the feature 
   */
  public Stem getStem() { return (Stem)(_getFeatureValueNc(_FI_stem));}
    
  /** setter for stem - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setStem(Stem v) {
    _setFeatureValueNcWj(_FI_stem, v);
  }    
    
   
    
  //*--------------*
  //* Feature: pos

  /** getter for pos - gets 
   * @generated
   * @return value of the feature 
   */
  public POS getPos() { return (POS)(_getFeatureValueNc(_FI_pos));}
    
  /** setter for pos - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPos(POS v) {
    _setFeatureValueNcWj(_FI_pos, v);
  }    
    
   
    
  //*--------------*
  //* Feature: morph

  /** getter for morph - gets The morphological feature associated with this token.
   * @generated
   * @return value of the feature 
   */
  public MorphologicalFeatures getMorph() { return (MorphologicalFeatures)(_getFeatureValueNc(_FI_morph));}
    
  /** setter for morph - sets The morphological feature associated with this token. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setMorph(MorphologicalFeatures v) {
    _setFeatureValueNcWj(_FI_morph, v);
  }    
    
   
    
  //*--------------*
  //* Feature: id

  /** getter for id - gets If this unit had an ID in the source format from which it was imported, it may be stored here. IDs are typically not assigned by DKPro Core components. If an ID is present, it should be respected by writers.
   * @generated
   * @return value of the feature 
   */
  public String getId() { return _getStringValueNc(_FI_id);}
    
  /** setter for id - sets If this unit had an ID in the source format from which it was imported, it may be stored here. IDs are typically not assigned by DKPro Core components. If an ID is present, it should be respected by writers. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setId(String v) {
    _setStringValueNfc(_FI_id, v);
  }    
    
   
    
  //*--------------*
  //* Feature: form

  /** getter for form - gets Potentially normalized form of the token text that should be used instead of the covered text if set.
   * @generated
   * @return value of the feature 
   */
  public TokenForm getForm() { return (TokenForm)(_getFeatureValueNc(_FI_form));}
    
  /** setter for form - sets Potentially normalized form of the token text that should be used instead of the covered text if set. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setForm(TokenForm v) {
    _setFeatureValueNcWj(_FI_form, v);
  }    
    
   
    
  //*--------------*
  //* Feature: syntacticFunction

  /** getter for syntacticFunction - gets 
   * @generated
   * @return value of the feature 
   */
  public String getSyntacticFunction() { return _getStringValueNc(_FI_syntacticFunction);}
    
  /** setter for syntacticFunction - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSyntacticFunction(String v) {
    _setStringValueNfc(_FI_syntacticFunction, v);
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

    