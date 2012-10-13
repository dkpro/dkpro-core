/*******************************************************************************
 * Copyright 2012
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
package de.tudarmstadt.ukp.dkpro.core.api.resources;

import java.io.IOException;
import java.util.Properties;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;

public abstract class CasConfigurableProviderBase<M> extends ResourceObjectProviderBase<M>
{
	private String language;
	
	public void configure(CAS aCas) throws AnalysisEngineProcessException
	{
		try {
			language = aCas.getDocumentLanguage();
			super.configure();
		}
		catch (IOException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

	@Override
	protected Properties getProperties()
	{
		Properties props = new Properties();
		if (language != null) {
			props.setProperty(LANGUAGE, language);
		}
		
		return props;
	}
}
