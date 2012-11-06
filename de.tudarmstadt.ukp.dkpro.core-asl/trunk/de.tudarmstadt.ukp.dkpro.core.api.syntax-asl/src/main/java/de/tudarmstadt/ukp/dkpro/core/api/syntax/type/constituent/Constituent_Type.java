
/* First created by JCasGen Tue Nov 06 15:58:03 CET 2012 */
package de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Tue Nov 06 16:00:54 CET 2012
 * @generated */
public class Constituent_Type extends Annotation_Type {
  /** @generated */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Constituent_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Constituent_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Constituent(addr, Constituent_Type.this);
  			   Constituent_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Constituent(addr, Constituent_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Constituent.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
 
  /** @generated */
  final Feature casFeat_constituentType;
  /** @generated */
  final int     casFeatCode_constituentType;
  /** @generated */ 
  public String getConstituentType(int addr) {
        if (featOkTst && casFeat_constituentType == null)
      jcas.throwFeatMissing("constituentType", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    return ll_cas.ll_getStringValue(addr, casFeatCode_constituentType);
  }
  /** @generated */    
  public void setConstituentType(int addr, String v) {
        if (featOkTst && casFeat_constituentType == null)
      jcas.throwFeatMissing("constituentType", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    ll_cas.ll_setStringValue(addr, casFeatCode_constituentType, v);}
    
  
 
  /** @generated */
  final Feature casFeat_parent;
  /** @generated */
  final int     casFeatCode_parent;
  /** @generated */ 
  public int getParent(int addr) {
        if (featOkTst && casFeat_parent == null)
      jcas.throwFeatMissing("parent", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    return ll_cas.ll_getRefValue(addr, casFeatCode_parent);
  }
  /** @generated */    
  public void setParent(int addr, int v) {
        if (featOkTst && casFeat_parent == null)
      jcas.throwFeatMissing("parent", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    ll_cas.ll_setRefValue(addr, casFeatCode_parent, v);}
    
  
 
  /** @generated */
  final Feature casFeat_children;
  /** @generated */
  final int     casFeatCode_children;
  /** @generated */ 
  public int getChildren(int addr) {
        if (featOkTst && casFeat_children == null)
      jcas.throwFeatMissing("children", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    return ll_cas.ll_getRefValue(addr, casFeatCode_children);
  }
  /** @generated */    
  public void setChildren(int addr, int v) {
        if (featOkTst && casFeat_children == null)
      jcas.throwFeatMissing("children", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    ll_cas.ll_setRefValue(addr, casFeatCode_children, v);}
    
   /** @generated */
  public int getChildren(int addr, int i) {
        if (featOkTst && casFeat_children == null)
      jcas.throwFeatMissing("children", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_children), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_children), i);
  return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_children), i);
  }
   
  /** @generated */ 
  public void setChildren(int addr, int i, int v) {
        if (featOkTst && casFeat_children == null)
      jcas.throwFeatMissing("children", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_children), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_children), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_children), i, v);
  }
 
 
  /** @generated */
  final Feature casFeat_syntacticFunction;
  /** @generated */
  final int     casFeatCode_syntacticFunction;
  /** @generated */ 
  public String getSyntacticFunction(int addr) {
        if (featOkTst && casFeat_syntacticFunction == null)
      jcas.throwFeatMissing("syntacticFunction", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    return ll_cas.ll_getStringValue(addr, casFeatCode_syntacticFunction);
  }
  /** @generated */    
  public void setSyntacticFunction(int addr, String v) {
        if (featOkTst && casFeat_syntacticFunction == null)
      jcas.throwFeatMissing("syntacticFunction", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    ll_cas.ll_setStringValue(addr, casFeatCode_syntacticFunction, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Constituent_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_constituentType = jcas.getRequiredFeatureDE(casType, "constituentType", "uima.cas.String", featOkTst);
    casFeatCode_constituentType  = (null == casFeat_constituentType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_constituentType).getCode();

 
    casFeat_parent = jcas.getRequiredFeatureDE(casType, "parent", "uima.tcas.Annotation", featOkTst);
    casFeatCode_parent  = (null == casFeat_parent) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_parent).getCode();

 
    casFeat_children = jcas.getRequiredFeatureDE(casType, "children", "uima.cas.FSArray", featOkTst);
    casFeatCode_children  = (null == casFeat_children) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_children).getCode();

 
    casFeat_syntacticFunction = jcas.getRequiredFeatureDE(casType, "syntacticFunction", "uima.cas.String", featOkTst);
    casFeatCode_syntacticFunction  = (null == casFeat_syntacticFunction) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_syntacticFunction).getCode();

  }
}



    