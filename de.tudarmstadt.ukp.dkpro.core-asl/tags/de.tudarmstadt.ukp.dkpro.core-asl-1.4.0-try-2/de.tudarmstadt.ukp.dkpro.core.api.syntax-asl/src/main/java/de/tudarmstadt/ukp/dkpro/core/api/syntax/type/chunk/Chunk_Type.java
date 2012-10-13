
/* First created by JCasGen Fri Nov 19 23:18:25 CET 2010 */
package de.tudarmstadt.ukp.dkpro.core.api.syntax.type.chunk;

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
 * Updated by JCasGen Fri Nov 19 23:18:25 CET 2010
 * @generated */
public class Chunk_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Chunk_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Chunk_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Chunk(addr, Chunk_Type.this);
  			   Chunk_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Chunk(addr, Chunk_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = Chunk.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.type.synunit.chunk.Chunk");
 
  /** @generated */
  final Feature casFeat_chunkValue;
  /** @generated */
  final int     casFeatCode_chunkValue;
  /** @generated */ 
  public String getChunkValue(int addr) {
        if (featOkTst && casFeat_chunkValue == null)
      jcas.throwFeatMissing("chunkValue", "de.tudarmstadt.ukp.dkpro.core.type.synunit.chunk.Chunk");
    return ll_cas.ll_getStringValue(addr, casFeatCode_chunkValue);
  }
  /** @generated */    
  public void setChunkValue(int addr, String v) {
        if (featOkTst && casFeat_chunkValue == null)
      jcas.throwFeatMissing("chunkValue", "de.tudarmstadt.ukp.dkpro.core.type.synunit.chunk.Chunk");
    ll_cas.ll_setStringValue(addr, casFeatCode_chunkValue, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public Chunk_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_chunkValue = jcas.getRequiredFeatureDE(casType, "chunkValue", "uima.cas.String", featOkTst);
    casFeatCode_chunkValue  = (null == casFeat_chunkValue) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_chunkValue).getCode();

  }
}



    