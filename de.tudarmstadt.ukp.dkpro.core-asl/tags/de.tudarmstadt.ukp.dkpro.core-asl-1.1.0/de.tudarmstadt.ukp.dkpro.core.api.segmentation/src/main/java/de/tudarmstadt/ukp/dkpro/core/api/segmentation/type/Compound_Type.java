
/* First created by JCasGen Sun Nov 21 13:42:50 CET 2010 */
package de.tudarmstadt.ukp.dkpro.core.api.segmentation.type;

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
 * Updated by JCasGen Thu Dec 09 22:13:11 CET 2010
 * @generated */
public class Compound_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Compound_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Compound_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Compound(addr, Compound_Type.this);
  			   Compound_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Compound(addr, Compound_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Compound.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound");
 
  /** @generated */
  final Feature casFeat_part1;
  /** @generated */
  final int     casFeatCode_part1;
  /** @generated */ 
  public String getPart1(int addr) {
        if (featOkTst && casFeat_part1 == null)
      jcas.throwFeatMissing("part1", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound");
    return ll_cas.ll_getStringValue(addr, casFeatCode_part1);
  }
  /** @generated */    
  public void setPart1(int addr, String v) {
        if (featOkTst && casFeat_part1 == null)
      jcas.throwFeatMissing("part1", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound");
    ll_cas.ll_setStringValue(addr, casFeatCode_part1, v);}
    
  
 
  /** @generated */
  final Feature casFeat_part2;
  /** @generated */
  final int     casFeatCode_part2;
  /** @generated */ 
  public String getPart2(int addr) {
        if (featOkTst && casFeat_part2 == null)
      jcas.throwFeatMissing("part2", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound");
    return ll_cas.ll_getStringValue(addr, casFeatCode_part2);
  }
  /** @generated */    
  public void setPart2(int addr, String v) {
        if (featOkTst && casFeat_part2 == null)
      jcas.throwFeatMissing("part2", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Compound");
    ll_cas.ll_setStringValue(addr, casFeatCode_part2, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Compound_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_part1 = jcas.getRequiredFeatureDE(casType, "part1", "uima.cas.String", featOkTst);
    casFeatCode_part1  = (null == casFeat_part1) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_part1).getCode();

 
    casFeat_part2 = jcas.getRequiredFeatureDE(casType, "part2", "uima.cas.String", featOkTst);
    casFeatCode_part2  = (null == casFeat_part2) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_part2).getCode();

  }
}



    