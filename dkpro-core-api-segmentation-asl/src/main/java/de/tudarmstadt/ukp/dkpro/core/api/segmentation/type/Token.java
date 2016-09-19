/*
 * Copyright 2016
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * First created by JCasGen Thu Sep 15 23:03:44 EEST 2016 
 */
package de.tudarmstadt.ukp.dkpro.core.api.segmentation.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.morph.MorphologicalFeatures;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import org.apache.uima.jcas.tcas.Annotation;


/** <p>Token is one of the two types commonly produced by a segmenter (the other being Sentence). A Token usually represents a word, although it may be used to represent multiple tightly connected words (e.g. "New York") or parts of a word (e.g. the possessive "'s"). One may choose to split compound words into multiple tokens, e.g. ("CamelCase" -&gt; "Camel", "Case"; "Zauberstab" -&gt; "Zauber", "stab"). Most processing components operate on Tokens, usually within the limits of the surrounding Sentence. E.g. a part-of-speech tagger analyses each Token in a Sentence and assigns a part-of-speech to each Token.</p>
 * Updated by JCasGen Thu Sep 15 23:08:21 EEST 2016
 * XML source: /Users/bluefire/git/dkpro-core/dkpro-core-api-segmentation-asl/src/main/resources/desc/type/LexicalUnits_customized.xml
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
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Token() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Token(int addr, TOP_Type type) {
    super(addr, type);
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
  public Annotation getParent() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_parent == null) {
        jcasType.jcas.throwFeatMissing("parent", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    }
    return (Annotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_parent)));}
    
  /** setter for parent - sets the parent of this token. This feature is meant to be used in when the token participates in a constituency parse and then refers to a constituent containing this token. The type of this feature is {@link Annotation} to avoid adding a dependency on the syntax API module. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setParent(Annotation v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_parent == null) {
        jcasType.jcas.throwFeatMissing("parent", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    }
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_parent, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: lemma

  /** getter for lemma - gets 
   * @generated
   * @return value of the feature 
   */
  public Lemma getLemma() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_lemma == null) {
        jcasType.jcas.throwFeatMissing("lemma", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    }
    return (Lemma)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_lemma)));}
    
  /** setter for lemma - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setLemma(Lemma v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_lemma == null) {
        jcasType.jcas.throwFeatMissing("lemma", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    }
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_lemma, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: stem

  /** getter for stem - gets 
   * @generated
   * @return value of the feature 
   */
  public Stem getStem() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_stem == null) {
        jcasType.jcas.throwFeatMissing("stem", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    }
    return (Stem)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_stem)));}
    
  /** setter for stem - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setStem(Stem v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_stem == null) {
        jcasType.jcas.throwFeatMissing("stem", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    }
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_stem, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: pos

  /** getter for pos - gets 
   * @generated
   * @return value of the feature 
   */
  public POS getPos() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_pos == null) {
        jcasType.jcas.throwFeatMissing("pos", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    }
    return (POS)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_pos)));}
    
  /** setter for pos - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setPos(POS v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_pos == null) {
        jcasType.jcas.throwFeatMissing("pos", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    }
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_pos, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: morph

  /** getter for morph - gets The morphological feature associated with this token.
   * @generated
   * @return value of the feature 
   */
  public MorphologicalFeatures getMorph() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_morph == null) {
        jcasType.jcas.throwFeatMissing("morph", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    }
    return (MorphologicalFeatures)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_morph)));}
    
  /** setter for morph - sets The morphological feature associated with this token. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setMorph(MorphologicalFeatures v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_morph == null) {
        jcasType.jcas.throwFeatMissing("morph", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    }
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_morph, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: id

  /** getter for id - gets If this unit had an ID in the source format from which it was imported, it may be stored here. IDs are typically not assigned by DKPro Core components. If an ID is present, it should be respected by writers.
   * @generated
   * @return value of the feature 
   */
  public String getId() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_id == null) {
        jcasType.jcas.throwFeatMissing("id", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    }
    return jcasType.ll_cas.ll_getStringValue(addr, ((Token_Type)jcasType).casFeatCode_id);}
    
  /** setter for id - sets If this unit had an ID in the source format from which it was imported, it may be stored here. IDs are typically not assigned by DKPro Core components. If an ID is present, it should be respected by writers. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setId(String v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_id == null) {
        jcasType.jcas.throwFeatMissing("id", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    }
    jcasType.ll_cas.ll_setStringValue(addr, ((Token_Type)jcasType).casFeatCode_id, v);}    
   
    
  //*--------------*
  //* Feature: form

  /** getter for form - gets Potentially normalized form of the token text that should be used instead of the covered text if set.
   * @generated
   * @return value of the feature 
   */
  public TokenForm getForm() {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_form == null) {
        jcasType.jcas.throwFeatMissing("form", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    }
    return (TokenForm)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Token_Type)jcasType).casFeatCode_form)));}
    
  /** setter for form - sets Potentially normalized form of the token text that should be used instead of the covered text if set. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setForm(TokenForm v) {
    if (Token_Type.featOkTst && ((Token_Type)jcasType).casFeat_form == null) {
        jcasType.jcas.throwFeatMissing("form", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
    }
    jcasType.ll_cas.ll_setRefValue(addr, ((Token_Type)jcasType).casFeatCode_form, jcasType.ll_cas.ll_getFSRef(v));}    

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
              form = new TokenForm(jcasType.jcas, getBegin(), getEnd());
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

    