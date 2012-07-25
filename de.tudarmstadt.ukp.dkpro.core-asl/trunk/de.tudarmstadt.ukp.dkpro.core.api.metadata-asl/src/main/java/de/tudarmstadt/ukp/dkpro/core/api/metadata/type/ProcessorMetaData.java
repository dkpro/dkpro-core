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

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.UimaContextAdmin;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.cas.TOP;


/** Information about a CAS processor such as a reader, analysis engine, multiplier, or consumer.
 * Updated by JCasGen Sun Jan 30 11:59:24 CET 2011
 * XML source: /Users/bluefire/UKP/Workspaces/dkpro-primary/de.tudarmstadt.ukp.dkpro.core-asl/de.tudarmstadt.ukp.dkpro.core.api.metadata/src/main/resources/desc/type/DocumentMetaData.xml
 * @generated */
public class ProcessorMetaData extends TOP {
  /** @generated
   * @ordered
   */
  public final static int typeIndexID = JCasRegistry.register(ProcessorMetaData.class);
  /** @generated
   * @ordered
   */
  public final static int type = typeIndexID;
  /** @generated  */
  @Override
public              int getTypeIndexID() {return typeIndexID;}

  /** Never called.  Disable default constructor
   * @generated */
  protected ProcessorMetaData() {}

  /** Internal - constructor used by generator
   * @generated */
  public ProcessorMetaData(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }

  /** @generated */
  public ProcessorMetaData(JCas jcas) {
    super(jcas);
    readObject();
  }

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
  @generated modifiable */
  private void readObject() {}



  //*--------------*
  //* Feature: instanceId

  /** getter for instanceId - gets Unique identifier for this processor instance. If the same processor is applied multiple times to the CAS in different configurations, each application has a unique instance ID. In a clustered environment, each cluster node running a processor in a particular configuration should produce the same instanceId.
   * @generated */
  public String getInstanceId() {
    if (ProcessorMetaData_Type.featOkTst && ((ProcessorMetaData_Type)jcasType).casFeat_instanceId == null) {
		jcasType.jcas.throwFeatMissing("instanceId", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.ProcessorMetaData");
	}
    return jcasType.ll_cas.ll_getStringValue(addr, ((ProcessorMetaData_Type)jcasType).casFeatCode_instanceId);}

  /** setter for instanceId - sets Unique identifier for this processor instance. If the same processor is applied multiple times to the CAS in different configurations, each application has a unique instance ID. In a clustered environment, each cluster node running a processor in a particular configuration should produce the same instanceId.
   * @generated */
  public void setInstanceId(String v) {
    if (ProcessorMetaData_Type.featOkTst && ((ProcessorMetaData_Type)jcasType).casFeat_instanceId == null) {
		jcasType.jcas.throwFeatMissing("instanceId", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.ProcessorMetaData");
	}
    jcasType.ll_cas.ll_setStringValue(addr, ((ProcessorMetaData_Type)jcasType).casFeatCode_instanceId, v);}


  //*--------------*
  //* Feature: name

  /** getter for name - gets Name of the processor as per the 'name' field in the processor meta data.
   * @generated */
  public String getName() {
    if (ProcessorMetaData_Type.featOkTst && ((ProcessorMetaData_Type)jcasType).casFeat_name == null) {
		jcasType.jcas.throwFeatMissing("name", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.ProcessorMetaData");
	}
    return jcasType.ll_cas.ll_getStringValue(addr, ((ProcessorMetaData_Type)jcasType).casFeatCode_name);}

  /** setter for name - sets Name of the processor as per the 'name' field in the processor meta data.
   * @generated */
  public void setName(String v) {
    if (ProcessorMetaData_Type.featOkTst && ((ProcessorMetaData_Type)jcasType).casFeat_name == null) {
		jcasType.jcas.throwFeatMissing("name", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.ProcessorMetaData");
	}
    jcasType.ll_cas.ll_setStringValue(addr, ((ProcessorMetaData_Type)jcasType).casFeatCode_name, v);}


  //*--------------*
  //* Feature: version

  /** getter for version - gets Version of the processor as per the 'version' field in the processor meta data.
   * @generated */
  public String getVersion() {
    if (ProcessorMetaData_Type.featOkTst && ((ProcessorMetaData_Type)jcasType).casFeat_version == null) {
		jcasType.jcas.throwFeatMissing("version", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.ProcessorMetaData");
	}
    return jcasType.ll_cas.ll_getStringValue(addr, ((ProcessorMetaData_Type)jcasType).casFeatCode_version);}

  /** setter for version - sets Version of the processor as per the 'version' field in the processor meta data.
   * @generated */
  public void setVersion(String v) {
    if (ProcessorMetaData_Type.featOkTst && ((ProcessorMetaData_Type)jcasType).casFeat_version == null) {
		jcasType.jcas.throwFeatMissing("version", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.ProcessorMetaData");
	}
    jcasType.ll_cas.ll_setStringValue(addr, ((ProcessorMetaData_Type)jcasType).casFeatCode_version, v);}


  //*--------------*
  //* Feature: annotatorImplementationName

  /** getter for annotatorImplementationName - gets Java class implementing the processor.
   * @generated */
  public String getAnnotatorImplementationName() {
    if (ProcessorMetaData_Type.featOkTst && ((ProcessorMetaData_Type)jcasType).casFeat_annotatorImplementationName == null) {
		jcasType.jcas.throwFeatMissing("annotatorImplementationName", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.ProcessorMetaData");
	}
    return jcasType.ll_cas.ll_getStringValue(addr, ((ProcessorMetaData_Type)jcasType).casFeatCode_annotatorImplementationName);}

  /** setter for annotatorImplementationName - sets Java class implementing the processor.
   * @generated */
  public void setAnnotatorImplementationName(String v) {
    if (ProcessorMetaData_Type.featOkTst && ((ProcessorMetaData_Type)jcasType).casFeat_annotatorImplementationName == null) {
		jcasType.jcas.throwFeatMissing("annotatorImplementationName", "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.ProcessorMetaData");
	}
    jcasType.ll_cas.ll_setStringValue(addr, ((ProcessorMetaData_Type)jcasType).casFeatCode_annotatorImplementationName, v);}


  /**
   * Create a processor meta data entry in the CAS for the current processor if none already exists.
   */
  public static FeatureStructure create(CAS aCas, UimaContext aContext)
  {
	  // Collect data
	  UimaContextAdmin ctxAdmin = (UimaContextAdmin) aContext;
	  String instanceId = ctxAdmin.getUniqueName();
	  String implName = ctxAdmin.getManagementInterface().getName();

	  // Check if metadata for the current processor is already present
	  Type casType = aCas.getTypeSystem().getType(ProcessorMetaData.class.getName());
	  Feature feature = casType.getFeatureByBaseName("instanceId");
	  FSIterator<FeatureStructure> i = aCas.getIndexRepository().getAllIndexedFS(casType);
	  while (i.hasNext()) {
		  FeatureStructure epmd = i.next();
		  if (instanceId.equals(epmd.getStringValue(feature))) {
			  return epmd;
		  }
	  }

	  // If not, add it
	  FeatureStructure pmd = aCas.createFS(casType);
	  pmd.setStringValue(casType.getFeatureByBaseName("instanceId"), instanceId);
	  pmd.setStringValue(casType.getFeatureByBaseName("annotatorImplementationName"), implName);
	  aCas.addFsToIndexes(pmd);

	  return pmd;
  }

  /**
   * Create a processor meta data entry in the CAS for the current processor if none already exists.
   */
  public static ProcessorMetaData create(JCas aJCas, UimaContext aContext)
  {
	  // Collect data
	  UimaContextAdmin ctxAdmin = (UimaContextAdmin) aContext;
	  String instanceId = ctxAdmin.getUniqueName();
	  String implName = ctxAdmin.getManagementInterface().getName();

	  // Check if metadata for the current processor is already present
	  Type casType = aJCas.getCasType(ProcessorMetaData.type);
	  FSIterator<FeatureStructure> i = aJCas.getFSIndexRepository().getAllIndexedFS(casType);
	  while (i.hasNext()) {
		  ProcessorMetaData epmd = (ProcessorMetaData) i.next();
		  if (instanceId.equals(epmd.getInstanceId())) {
			  return epmd;
		  }
	  }

	  // If not, add it
	  ProcessorMetaData pmd = new ProcessorMetaData(aJCas);
	  pmd.setInstanceId(instanceId);
	  pmd.setAnnotatorImplementationName(implName);
	  pmd.addToIndexes();

	  return pmd;
  }

  public static List<ProcessorMetaData> list(JCas aJCas)
  {
	  List<ProcessorMetaData> pmds = new ArrayList<ProcessorMetaData>();

	  Type casType = aJCas.getCasType(ProcessorMetaData.type);
	  FSIterator<FeatureStructure> i = aJCas.getFSIndexRepository().getAllIndexedFS(casType);
	  while (i.hasNext()) {
		  pmds.add((ProcessorMetaData) i.next());
	  }

	  return pmds;
  }
}

