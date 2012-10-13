/*******************************************************************************
 * Copyright 2010
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
 ******************************************************************************/
/* First created by JCasGen Sun Jan 30 11:59:24 CET 2011 */
package de.tudarmstadt.ukp.dkpro.core.api.metadata.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.cas.TOP_Type;

/** Information about a CAS processor such as a reader, analysis engine, multiplier, or consumer.
 * Updated by JCasGen Sun Jan 30 11:59:24 CET 2011
 * @generated */
public class ProcessorMetaData_Type extends TOP_Type {
  /** @generated */
  @Override
protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator =
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (ProcessorMetaData_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = ProcessorMetaData_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new ProcessorMetaData(addr, ProcessorMetaData_Type.this);
  			   ProcessorMetaData_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        }
			else {
				return new ProcessorMetaData(addr, ProcessorMetaData_Type.this);
			}
  	  }
    };
  /** @generated */
  public final static int typeIndexID = ProcessorMetaData.typeIndexID;
  /** @generated
     @modifiable */
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.ukp.dkpro.core.api.metadata.type.ProcessorMetaData");

  /** @generated */
  final Feature casFeat_instanceId;
  /** @generated */
  final int     casFeatCode_instanceId;
  /** @generated */
  public String getInstanceId(int addr) {
        if (featOkTst && casFeat_instanceId == null) {
			jcas.throwFeatMissing("instanceId", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.ProcessorMetaData");
		}
    return ll_cas.ll_getStringValue(addr, casFeatCode_instanceId);
  }
  /** @generated */
  public void setInstanceId(int addr, String v) {
        if (featOkTst && casFeat_instanceId == null) {
			jcas.throwFeatMissing("instanceId", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.ProcessorMetaData");
		}
    ll_cas.ll_setStringValue(addr, casFeatCode_instanceId, v);}



  /** @generated */
  final Feature casFeat_name;
  /** @generated */
  final int     casFeatCode_name;
  /** @generated */
  public String getName(int addr) {
        if (featOkTst && casFeat_name == null) {
			jcas.throwFeatMissing("name", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.ProcessorMetaData");
		}
    return ll_cas.ll_getStringValue(addr, casFeatCode_name);
  }
  /** @generated */
  public void setName(int addr, String v) {
        if (featOkTst && casFeat_name == null) {
			jcas.throwFeatMissing("name", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.ProcessorMetaData");
		}
    ll_cas.ll_setStringValue(addr, casFeatCode_name, v);}



  /** @generated */
  final Feature casFeat_version;
  /** @generated */
  final int     casFeatCode_version;
  /** @generated */
  public String getVersion(int addr) {
        if (featOkTst && casFeat_version == null) {
			jcas.throwFeatMissing("version", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.ProcessorMetaData");
		}
    return ll_cas.ll_getStringValue(addr, casFeatCode_version);
  }
  /** @generated */
  public void setVersion(int addr, String v) {
        if (featOkTst && casFeat_version == null) {
			jcas.throwFeatMissing("version", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.ProcessorMetaData");
		}
    ll_cas.ll_setStringValue(addr, casFeatCode_version, v);}



  /** @generated */
  final Feature casFeat_annotatorImplementationName;
  /** @generated */
  final int     casFeatCode_annotatorImplementationName;
  /** @generated */
  public String getAnnotatorImplementationName(int addr) {
        if (featOkTst && casFeat_annotatorImplementationName == null) {
			jcas.throwFeatMissing("annotatorImplementationName", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.ProcessorMetaData");
		}
    return ll_cas.ll_getStringValue(addr, casFeatCode_annotatorImplementationName);
  }
  /** @generated */
  public void setAnnotatorImplementationName(int addr, String v) {
        if (featOkTst && casFeat_annotatorImplementationName == null) {
			jcas.throwFeatMissing("annotatorImplementationName", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.ProcessorMetaData");
		}
    ll_cas.ll_setStringValue(addr, casFeatCode_annotatorImplementationName, v);}





  /** initialize variables to correspond with Cas Type and Features
	* @generated */
  public ProcessorMetaData_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());


    casFeat_instanceId = jcas.getRequiredFeatureDE(casType, "instanceId", "uima.cas.String", featOkTst);
    casFeatCode_instanceId  = (null == casFeat_instanceId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_instanceId).getCode();


    casFeat_name = jcas.getRequiredFeatureDE(casType, "name", "uima.cas.String", featOkTst);
    casFeatCode_name  = (null == casFeat_name) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_name).getCode();


    casFeat_version = jcas.getRequiredFeatureDE(casType, "version", "uima.cas.String", featOkTst);
    casFeatCode_version  = (null == casFeat_version) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_version).getCode();


    casFeat_annotatorImplementationName = jcas.getRequiredFeatureDE(casType, "annotatorImplementationName", "uima.cas.String", featOkTst);
    casFeatCode_annotatorImplementationName  = (null == casFeat_annotatorImplementationName) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_annotatorImplementationName).getCode();

  }
}



