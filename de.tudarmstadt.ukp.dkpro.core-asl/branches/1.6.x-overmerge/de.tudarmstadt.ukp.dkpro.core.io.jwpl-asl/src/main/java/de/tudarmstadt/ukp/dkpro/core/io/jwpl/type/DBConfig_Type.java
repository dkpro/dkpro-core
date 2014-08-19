
/* First created by JCasGen Tue Jul 19 10:46:33 CEST 2011 */
package de.tudarmstadt.ukp.dkpro.core.io.jwpl.type;

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

/** Database configuration for the connection to the database where the CAS data was retrieved.
 * Updated by JCasGen Tue Jul 19 10:59:42 CEST 2011
 * @generated */
public class DBConfig_Type extends Annotation_Type {
  /** @generated */
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (DBConfig_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = DBConfig_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new DBConfig(addr, DBConfig_Type.this);
  			   DBConfig_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new DBConfig(addr, DBConfig_Type.this);
  	  }
    };
  /** @generated */
  public final static int typeIndexID = DBConfig.typeIndexID;
  /** @generated 
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig");
 
  /** @generated */
  final Feature casFeat_Host;
  /** @generated */
  final int     casFeatCode_Host;
  /** @generated */ 
  public String getHost(int addr) {
        if (featOkTst && casFeat_Host == null)
      jcas.throwFeatMissing("Host", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig");
    return ll_cas.ll_getStringValue(addr, casFeatCode_Host);
  }
  /** @generated */    
  public void setHost(int addr, String v) {
        if (featOkTst && casFeat_Host == null)
      jcas.throwFeatMissing("Host", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig");
    ll_cas.ll_setStringValue(addr, casFeatCode_Host, v);}
    
  
 
  /** @generated */
  final Feature casFeat_DB;
  /** @generated */
  final int     casFeatCode_DB;
  /** @generated */ 
  public String getDB(int addr) {
        if (featOkTst && casFeat_DB == null)
      jcas.throwFeatMissing("DB", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig");
    return ll_cas.ll_getStringValue(addr, casFeatCode_DB);
  }
  /** @generated */    
  public void setDB(int addr, String v) {
        if (featOkTst && casFeat_DB == null)
      jcas.throwFeatMissing("DB", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig");
    ll_cas.ll_setStringValue(addr, casFeatCode_DB, v);}
    
  
 
  /** @generated */
  final Feature casFeat_User;
  /** @generated */
  final int     casFeatCode_User;
  /** @generated */ 
  public String getUser(int addr) {
        if (featOkTst && casFeat_User == null)
      jcas.throwFeatMissing("User", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig");
    return ll_cas.ll_getStringValue(addr, casFeatCode_User);
  }
  /** @generated */    
  public void setUser(int addr, String v) {
        if (featOkTst && casFeat_User == null)
      jcas.throwFeatMissing("User", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig");
    ll_cas.ll_setStringValue(addr, casFeatCode_User, v);}
    
  
 
  /** @generated */
  final Feature casFeat_Password;
  /** @generated */
  final int     casFeatCode_Password;
  /** @generated */ 
  public String getPassword(int addr) {
        if (featOkTst && casFeat_Password == null)
      jcas.throwFeatMissing("Password", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig");
    return ll_cas.ll_getStringValue(addr, casFeatCode_Password);
  }
  /** @generated */    
  public void setPassword(int addr, String v) {
        if (featOkTst && casFeat_Password == null)
      jcas.throwFeatMissing("Password", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig");
    ll_cas.ll_setStringValue(addr, casFeatCode_Password, v);}
    
  
 
  /** @generated */
  final Feature casFeat_Language;
  /** @generated */
  final int     casFeatCode_Language;
  /** @generated */ 
  public String getLanguage(int addr) {
        if (featOkTst && casFeat_Language == null)
      jcas.throwFeatMissing("Language", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig");
    return ll_cas.ll_getStringValue(addr, casFeatCode_Language);
  }
  /** @generated */    
  public void setLanguage(int addr, String v) {
        if (featOkTst && casFeat_Language == null)
      jcas.throwFeatMissing("Language", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig");
    ll_cas.ll_setStringValue(addr, casFeatCode_Language, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public DBConfig_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_Host = jcas.getRequiredFeatureDE(casType, "Host", "uima.cas.String", featOkTst);
    casFeatCode_Host  = (null == casFeat_Host) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Host).getCode();

 
    casFeat_DB = jcas.getRequiredFeatureDE(casType, "DB", "uima.cas.String", featOkTst);
    casFeatCode_DB  = (null == casFeat_DB) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_DB).getCode();

 
    casFeat_User = jcas.getRequiredFeatureDE(casType, "User", "uima.cas.String", featOkTst);
    casFeatCode_User  = (null == casFeat_User) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_User).getCode();

 
    casFeat_Password = jcas.getRequiredFeatureDE(casType, "Password", "uima.cas.String", featOkTst);
    casFeatCode_Password  = (null == casFeat_Password) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Password).getCode();

 
    casFeat_Language = jcas.getRequiredFeatureDE(casType, "Language", "uima.cas.String", featOkTst);
    casFeatCode_Language  = (null == casFeat_Language) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Language).getCode();

  }
}



    