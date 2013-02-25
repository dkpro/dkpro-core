/*******************************************************************************
 * Copyright 2013
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
package de.tudarmstadt.ukp.dkpro.core.frequency.tfidf.model;

import static org.uimafit.factory.ExternalResourceFactory.bindResource;

import java.io.File;
import java.io.ObjectInputStream;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.*;

/**
 * Shared {@link DfModel}.
 *
 * @author Mateusz Parzonka
 *
 */
public class SharedDfModel
	implements DfModel, SharedResourceObject
{
	private static final long serialVersionUID = 7021982756860220030L;

	// We decorate the used DfModel with SharedResourceObject-behavior.
	private DfModel dfModel;

	/**
	 * Binds a {@link DfModel} to the given {@link AnalysisEngineDescription}.
	 * The model is deserialized from file stream.
	 *
	 * @param aaed
	 *            An aggregate {@link AnalysisEngineDescription}. The binding
	 *            does not work with primitive descriptions.
	 * @param pathToResource
	 *            The path from where the resource is loaded.
	 * @param params
	 *            Optional parameters.
	 * @throws ResourceInitializationException
	 */
	public static void bindTo(AnalysisEngineDescription aaed,
			String pathToResource, Object... params)
		throws ResourceInitializationException
	{
		try {
			bindResource(aaed, DfModel.class.getName(), SharedDfModel.class,
					new File(pathToResource).toURI()
							.toURL().toString(), params);
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void load(DataResource aData)
		throws ResourceInitializationException
	{
		try {
			dfModel = (DfModel) new ObjectInputStream(aData.getInputStream())
					.readObject();
		}
		catch (Exception e) {
			throw new ResourceInitializationException(e);
		}

	}

	@Override
	public int getDf(String term)
	{
		return dfModel.getDf(term);
	}

	@Override
	public int getDocumentCount()
	{
		return dfModel.getDocumentCount();
	}

	@Override
	public String getFeaturePath()
	{
		return dfModel.getFeaturePath();
	}

	@Override
	public boolean getLowercase()
	{
		return dfModel.getLowercase();
	}

}
