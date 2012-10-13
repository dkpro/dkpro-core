
/* First created by JCasGen Mon Nov 22 18:23:21 CET 2010 */
package de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency;

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
 * Updated by JCasGen Mon Nov 22 18:23:21 CET 2010
 * @generated */
public class Dependent_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Dependent_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Dependent_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Dependent(addr, Dependent_Type.this);
  			   Dependent_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Dependent(addr, Dependent_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Dependent.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependent");
 
  /** @generated */
  final Feature casFeat_Governor;
  /** @generated */
  final int     casFeatCode_Governor;
  /** @generated */ 
  public int getGovernor(int addr) {
        if (featOkTst && casFeat_Governor == null)
      jcas.throwFeatMissing("Governor", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependent");
    return ll_cas.ll_getRefValue(addr, casFeatCode_Governor);
  }
  /** @generated */    
  public void setGovernor(int addr, int v) {
        if (featOkTst && casFeat_Governor == null)
      jcas.throwFeatMissing("Governor", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependent");
    ll_cas.ll_setRefValue(addr, casFeatCode_Governor, v);}
    
  
 
  /** @generated */
  final Feature casFeat_Dependency;
  /** @generated */
  final int     casFeatCode_Dependency;
  /** @generated */ 
  public int getDependency(int addr) {
        if (featOkTst && casFeat_Dependency == null)
      jcas.throwFeatMissing("Dependency", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependent");
    return ll_cas.ll_getRefValue(addr, casFeatCode_Dependency);
  }
  /** @generated */    
  public void setDependency(int addr, int v) {
        if (featOkTst && casFeat_Dependency == null)
      jcas.throwFeatMissing("Dependency", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependent");
    ll_cas.ll_setRefValue(addr, casFeatCode_Dependency, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Dependent_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_Governor = jcas.getRequiredFeatureDE(casType, "Governor", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token", featOkTst);
    casFeatCode_Governor  = (null == casFeat_Governor) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Governor).getCode();

 
    casFeat_Dependency = jcas.getRequiredFeatureDE(casType, "Dependency", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency", featOkTst);
    casFeatCode_Dependency  = (null == casFeat_Dependency) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Dependency).getCode();

  }
}



    