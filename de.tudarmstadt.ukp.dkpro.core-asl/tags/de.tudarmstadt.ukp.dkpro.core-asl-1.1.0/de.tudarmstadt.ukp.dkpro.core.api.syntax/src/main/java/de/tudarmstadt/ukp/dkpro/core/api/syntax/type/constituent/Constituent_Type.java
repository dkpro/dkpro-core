
/* First created by JCasGen Mon Nov 22 18:16:12 CET 2010 */
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
 * Updated by JCasGen Fri Feb 11 14:39:28 CET 2011
 * @generated */
public class Constituent_Type extends Annotation_Type {
  /** @generated */
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
  public final static int typeIndexID = Constituent.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
 
  /** @generated */
  final Feature casFeat_ConstituentType;
  /** @generated */
  final int     casFeatCode_ConstituentType;
  /** @generated */ 
  public String getConstituentType(int addr) {
        if (featOkTst && casFeat_ConstituentType == null)
      jcas.throwFeatMissing("ConstituentType", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    return ll_cas.ll_getStringValue(addr, casFeatCode_ConstituentType);
  }
  /** @generated */    
  public void setConstituentType(int addr, String v) {
        if (featOkTst && casFeat_ConstituentType == null)
      jcas.throwFeatMissing("ConstituentType", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    ll_cas.ll_setStringValue(addr, casFeatCode_ConstituentType, v);}
    
  
 
  /** @generated */
  final Feature casFeat_Parent;
  /** @generated */
  final int     casFeatCode_Parent;
  /** @generated */ 
  public int getParent(int addr) {
        if (featOkTst && casFeat_Parent == null)
      jcas.throwFeatMissing("Parent", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    return ll_cas.ll_getRefValue(addr, casFeatCode_Parent);
  }
  /** @generated */    
  public void setParent(int addr, int v) {
        if (featOkTst && casFeat_Parent == null)
      jcas.throwFeatMissing("Parent", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    ll_cas.ll_setRefValue(addr, casFeatCode_Parent, v);}
    
  
 
  /** @generated */
  final Feature casFeat_Children;
  /** @generated */
  final int     casFeatCode_Children;
  /** @generated */ 
  public int getChildren(int addr) {
        if (featOkTst && casFeat_Children == null)
      jcas.throwFeatMissing("Children", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    return ll_cas.ll_getRefValue(addr, casFeatCode_Children);
  }
  /** @generated */    
  public void setChildren(int addr, int v) {
        if (featOkTst && casFeat_Children == null)
      jcas.throwFeatMissing("Children", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    ll_cas.ll_setRefValue(addr, casFeatCode_Children, v);}
    
   /** @generated */
  public int getChildren(int addr, int i) {
        if (featOkTst && casFeat_Children == null)
      jcas.throwFeatMissing("Children", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_Children), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_Children), i);
  return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_Children), i);
  }
   
  /** @generated */ 
  public void setChildren(int addr, int i, int v) {
        if (featOkTst && casFeat_Children == null)
      jcas.throwFeatMissing("Children", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_Children), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_Children), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_Children), i, v);
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

 
    casFeat_ConstituentType = jcas.getRequiredFeatureDE(casType, "ConstituentType", "uima.cas.String", featOkTst);
    casFeatCode_ConstituentType  = (null == casFeat_ConstituentType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_ConstituentType).getCode();

 
    casFeat_Parent = jcas.getRequiredFeatureDE(casType, "Parent", "uima.tcas.Annotation", featOkTst);
    casFeatCode_Parent  = (null == casFeat_Parent) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Parent).getCode();

 
    casFeat_Children = jcas.getRequiredFeatureDE(casType, "Children", "uima.cas.FSArray", featOkTst);
    casFeatCode_Children  = (null == casFeat_Children) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Children).getCode();

 
    casFeat_syntacticFunction = jcas.getRequiredFeatureDE(casType, "syntacticFunction", "uima.cas.String", featOkTst);
    casFeatCode_syntacticFunction  = (null == casFeat_syntacticFunction) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_syntacticFunction).getCode();

  }
}



    