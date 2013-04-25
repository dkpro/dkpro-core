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
package de.tudarmstadt.ukp.dkpro.core.testing;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.TypeCapability;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

/**
 * Removes fields from the document meta data which may be different depending on the machine a
 * test is run on.
 *
 * @author Richard Eckart de Castilho
 */

@TypeCapability(
        inputs={"de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData"},
        outputs={"de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData"})

public class DocumentMetaDataStripper
	extends JCasAnnotator_ImplBase
{
	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		try {
			DocumentMetaData meta = DocumentMetaData.get(aJCas);
			meta.setDocumentBaseUri(null);
			meta.setDocumentUri(null);
			meta.setCollectionId(null);
		}
		catch (IllegalArgumentException e) {
			// No metadata in the CAS.
		}
	}
}
