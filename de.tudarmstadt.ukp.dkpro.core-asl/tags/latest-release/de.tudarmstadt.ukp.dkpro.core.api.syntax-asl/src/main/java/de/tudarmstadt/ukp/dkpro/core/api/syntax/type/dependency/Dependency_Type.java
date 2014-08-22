
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

/** A dependency relation between two tokens. The dependency annotation begin and end offsets correspond to those of the dependent.
 * Updated by JCasGen Sat Aug 10 16:38:05 CEST 2013
 * @generated */
public class Dependency_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Dependency_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Dependency_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Dependency(addr, Dependency_Type.this);
  			   Dependency_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Dependency(addr, Dependency_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Dependency.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency");
 
  /** @generated */
  final Feature casFeat_Governor;
  /** @generated */
  final int     casFeatCode_Governor;
  /** @generated */ 
  public int getGovernor(int addr) {
        if (featOkTst && casFeat_Governor == null)
      jcas.throwFeatMissing("Governor", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency");
    return ll_cas.ll_getRefValue(addr, casFeatCode_Governor);
  }
  /** @generated */    
  public void setGovernor(int addr, int v) {
        if (featOkTst && casFeat_Governor == null)
      jcas.throwFeatMissing("Governor", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency");
    ll_cas.ll_setRefValue(addr, casFeatCode_Governor, v);}
    
  
 
  /** @generated */
  final Feature casFeat_Dependent;
  /** @generated */
  final int     casFeatCode_Dependent;
  /** @generated */ 
  public int getDependent(int addr) {
        if (featOkTst && casFeat_Dependent == null)
      jcas.throwFeatMissing("Dependent", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency");
    return ll_cas.ll_getRefValue(addr, casFeatCode_Dependent);
  }
  /** @generated */    
  public void setDependent(int addr, int v) {
        if (featOkTst && casFeat_Dependent == null)
      jcas.throwFeatMissing("Dependent", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency");
    ll_cas.ll_setRefValue(addr, casFeatCode_Dependent, v);}
    
  
 
  /** @generated */
  final Feature casFeat_DependencyType;
  /** @generated */
  final int     casFeatCode_DependencyType;
  /** @generated */ 
  public String getDependencyType(int addr) {
        if (featOkTst && casFeat_DependencyType == null)
      jcas.throwFeatMissing("DependencyType", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency");
    return ll_cas.ll_getStringValue(addr, casFeatCode_DependencyType);
  }
  /** @generated */    
  public void setDependencyType(int addr, String v) {
        if (featOkTst && casFeat_DependencyType == null)
      jcas.throwFeatMissing("DependencyType", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency");
    ll_cas.ll_setStringValue(addr, casFeatCode_DependencyType, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Dependency_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_Governor = jcas.getRequiredFeatureDE(casType, "Governor", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token", featOkTst);
    casFeatCode_Governor  = (null == casFeat_Governor) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Governor).getCode();

 
    casFeat_Dependent = jcas.getRequiredFeatureDE(casType, "Dependent", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token", featOkTst);
    casFeatCode_Dependent  = (null == casFeat_Dependent) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Dependent).getCode();

 
    casFeat_DependencyType = jcas.getRequiredFeatureDE(casType, "DependencyType", "uima.cas.String", featOkTst);
    casFeatCode_DependencyType  = (null == casFeat_DependencyType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_DependencyType).getCode();

  }
}



    