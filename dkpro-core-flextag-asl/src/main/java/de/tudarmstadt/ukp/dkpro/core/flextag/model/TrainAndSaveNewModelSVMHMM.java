/*******************************************************************************
 * Copyright 2015
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
package de.tudarmstadt.ukp.dkpro.core.flextag.model;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.component.NoOpAnnotator;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.io.text.TextReader;
import de.tudarmstadt.ukp.dkpro.lab.Lab;
import de.tudarmstadt.ukp.dkpro.lab.task.Dimension;
import de.tudarmstadt.ukp.dkpro.lab.task.ParameterSpace;
import de.tudarmstadt.ukp.dkpro.tc.core.Constants;
import de.tudarmstadt.ukp.dkpro.tc.features.tcu.CurrentUnit;
import de.tudarmstadt.ukp.dkpro.tc.features.tcu.NextUnit;
import de.tudarmstadt.ukp.dkpro.tc.features.tcu.PrevUnit;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.SVMHMMAdapter;
import de.tudarmstadt.ukp.dkpro.tc.svmhmm.task.serialization.SaveModelSvmhmmBatchTask;

/**
 * Example class of how to train an own model
 */
public class TrainAndSaveNewModelSVMHMM implements Constants {

	static String corpus = null;
	static String experimentName = "trainAndSaveModel";
	static String languageCode = null;
	static File modelOutputFolder = null;

	static String homeFolder = null;

	public static void main(String[] args) throws Exception {
		corpus = args[0];
		languageCode = args[1];
		homeFolder = args[2];
		modelOutputFolder = new File(args[3]);
		modelOutputFolder.mkdirs();

		System.setProperty("DKPRO_HOME", homeFolder);

		ParameterSpace pSpace = getParameterSpace(Constants.FM_SEQUENCE,
				Constants.LM_SINGLE_LABEL);

		TrainAndSaveNewModelSVMHMM experiment = new TrainAndSaveNewModelSVMHMM();
		experiment.validation(pSpace);
	}

	@SuppressWarnings("unchecked")
	public static ParameterSpace getParameterSpace(String featureMode,
			String learningMode) throws Exception {

		// configure training and test data reader dimension
		Map<String, Object> dimReaders = new HashMap<String, Object>();

		Dimension<List<String>> dimFeatureSets = Dimension.create(
				DIM_FEATURE_SET,
				Arrays.asList(new String[] { CurrentUnit.class.getName(),
						NextUnit.class.getName(),
						PrevUnit.class.getName(),
				// TODO: Add further features here
				}));

		Dimension<List<String>> dimClassificationArgs = Dimension
				.create(DIM_CLASSIFICATION_ARGS,
						asList(new String[] { 
					// TODO: Set classification arguments								
						}));

		Dimension<List<Object>> dimPipelineParameters = Dimension.create(
				DIM_PIPELINE_PARAMS, Arrays.asList(new Object[] {
				// TODO: Set feature parameters
						}));

		dimReaders.put(DIM_READER_TRAIN, TextReader.class);
		dimReaders.put(DIM_READER_TRAIN_PARAMS, Arrays.asList(
				TextReader.PARAM_LANGUAGE, languageCode,
				TextReader.PARAM_SOURCE_LOCATION, corpus));

		ParameterSpace pSpace = new ParameterSpace(Dimension.createBundle(
				"readers", dimReaders), Dimension.create(DIM_LEARNING_MODE,
				learningMode), Dimension.create(DIM_FEATURE_MODE, featureMode),
				dimPipelineParameters, dimFeatureSets, dimClassificationArgs);

		return pSpace;
	}

	protected void validation(ParameterSpace pSpace) throws Exception {
		SaveModelSvmhmmBatchTask batch = new SaveModelSvmhmmBatchTask(
				experimentName, modelOutputFolder, SVMHMMAdapter.class,
				getPreprocessing());
		batch.setParameterSpace(pSpace);

		// Run
		Lab.getInstance().run(batch);
	}

	protected AnalysisEngineDescription getPreprocessing()
			throws ResourceInitializationException {
		return createEngineDescription(NoOpAnnotator.class);
	}
}
