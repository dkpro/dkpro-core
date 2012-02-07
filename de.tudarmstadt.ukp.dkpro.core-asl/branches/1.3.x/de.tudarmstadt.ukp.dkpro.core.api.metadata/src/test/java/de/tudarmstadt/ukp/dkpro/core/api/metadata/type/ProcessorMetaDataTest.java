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

package de.tudarmstadt.ukp.dkpro.core.api.metadata.type;

import static org.uimafit.factory.AnalysisEngineFactory.createAggregate;
import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.junit.Test;
import org.uimafit.component.CasAnnotator_ImplBase;


public class ProcessorMetaDataTest
{
	@Test
	public void test() throws Exception
	{
		AnalysisEngineDescription ae1 = createPrimitiveDescription(DummyAE.class);
		AnalysisEngineDescription ae2 = createPrimitiveDescription(DummyAE.class);
		AnalysisEngine ae = createAggregate(createAggregateDescription(ae1, ae2));

		CAS cas = ae.newCAS();
		ae.process(cas);

		for (ProcessorMetaData pmd : ProcessorMetaData.list(cas.getJCas())) {
			System.out.println("== Processor ==");
			System.out.println("Instance ID   : "+pmd.getInstanceId());
			System.out.println("Name          : "+pmd.getName());
			System.out.println("Version       : "+pmd.getVersion());
			System.out.println("Implementation: "+pmd.getAnnotatorImplementationName());
		}
	}

	public static class DummyAE extends CasAnnotator_ImplBase
	{
		@Override
		public void process(CAS aCAS)
			throws AnalysisEngineProcessException
		{
			ProcessorMetaData.create(aCAS, getContext());
		}
	}
}
