

/* First created by JCasGen Tue Jul 19 10:46:33 CEST 2011 */
package de.tudarmstadt.ukp.dkpro.core.io.jwpl.type;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** Database configuration for the connection to the database where the CAS data was retrieved.
 * Updated by JCasGen Tue Jul 19 10:59:42 CEST 2011
 * XML source: /home/oferschke/workspaces/workspace/de.tudarmstadt.ukp.dkpro.core.io.jwpl-asl/src/main/resources/desc/type/DatabaseConfiguration.xml
 * @generated */
public class DBConfig extends Annotation {
  /** @generated
   * @ordered 
   */
  public final static int typeIndexID = JCasRegistry.register(DBConfig.class);
  /** @generated
   * @ordered 
   */
  public final static int type = typeIndexID;
  /** @generated  */
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected DBConfig() {}
    
  /** Internal - constructor used by generator 
   * @generated */
  public DBConfig(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated */
  public DBConfig(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated */  
  public DBConfig(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}
     
 
    
  //*--------------*
  //* Feature: Host

  /** getter for Host - gets DB Host
   * @generated */
  public String getHost() {
    if (DBConfig_Type.featOkTst && ((DBConfig_Type)jcasType).casFeat_Host == null)
      jcasType.jcas.throwFeatMissing("Host", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DBConfig_Type)jcasType).casFeatCode_Host);}
    
  /** setter for Host - sets DB Host 
   * @generated */
  public void setHost(String v) {
    if (DBConfig_Type.featOkTst && ((DBConfig_Type)jcasType).casFeat_Host == null)
      jcasType.jcas.throwFeatMissing("Host", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig");
    jcasType.ll_cas.ll_setStringValue(addr, ((DBConfig_Type)jcasType).casFeatCode_Host, v);}    
   
    
  //*--------------*
  //* Feature: DB

  /** getter for DB - gets Database
   * @generated */
  public String getDB() {
    if (DBConfig_Type.featOkTst && ((DBConfig_Type)jcasType).casFeat_DB == null)
      jcasType.jcas.throwFeatMissing("DB", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DBConfig_Type)jcasType).casFeatCode_DB);}
    
  /** setter for DB - sets Database 
   * @generated */
  public void setDB(String v) {
    if (DBConfig_Type.featOkTst && ((DBConfig_Type)jcasType).casFeat_DB == null)
      jcasType.jcas.throwFeatMissing("DB", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig");
    jcasType.ll_cas.ll_setStringValue(addr, ((DBConfig_Type)jcasType).casFeatCode_DB, v);}    
   
    
  //*--------------*
  //* Feature: User

  /** getter for User - gets Username
   * @generated */
  public String getUser() {
    if (DBConfig_Type.featOkTst && ((DBConfig_Type)jcasType).casFeat_User == null)
      jcasType.jcas.throwFeatMissing("User", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DBConfig_Type)jcasType).casFeatCode_User);}
    
  /** setter for User - sets Username 
   * @generated */
  public void setUser(String v) {
    if (DBConfig_Type.featOkTst && ((DBConfig_Type)jcasType).casFeat_User == null)
      jcasType.jcas.throwFeatMissing("User", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig");
    jcasType.ll_cas.ll_setStringValue(addr, ((DBConfig_Type)jcasType).casFeatCode_User, v);}    
   
    
  //*--------------*
  //* Feature: Password

  /** getter for Password - gets User password
   * @generated */
  public String getPassword() {
    if (DBConfig_Type.featOkTst && ((DBConfig_Type)jcasType).casFeat_Password == null)
      jcasType.jcas.throwFeatMissing("Password", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DBConfig_Type)jcasType).casFeatCode_Password);}
    
  /** setter for Password - sets User password 
   * @generated */
  public void setPassword(String v) {
    if (DBConfig_Type.featOkTst && ((DBConfig_Type)jcasType).casFeat_Password == null)
      jcasType.jcas.throwFeatMissing("Password", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig");
    jcasType.ll_cas.ll_setStringValue(addr, ((DBConfig_Type)jcasType).casFeatCode_Password, v);}    
   
    
  //*--------------*
  //* Feature: Language

  /** getter for Language - gets Wikipedia Language Versions
   * @generated */
  public String getLanguage() {
    if (DBConfig_Type.featOkTst && ((DBConfig_Type)jcasType).casFeat_Language == null)
      jcasType.jcas.throwFeatMissing("Language", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DBConfig_Type)jcasType).casFeatCode_Language);}
    
  /** setter for Language - sets Wikipedia Language Versions 
   * @generated */
  public void setLanguage(String v) {
    if (DBConfig_Type.featOkTst && ((DBConfig_Type)jcasType).casFeat_Language == null)
      jcasType.jcas.throwFeatMissing("Language", "de.tudarmstadt.ukp.dkpro.core.io.jwpl.type.DBConfig");
    jcasType.ll_cas.ll_setStringValue(addr, ((DBConfig_Type)jcasType).casFeatCode_Language, v);}    
  }

    