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
package de.tudarmstadt.ukp.dkpro.core.io.graf;

import java.io.OutputStream;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.xces.graf.api.IGraph;
import org.xces.graf.io.GrafRenderer;
import org.xces.graf.io.IRenderer;
import org.xces.graf.uima.GraphFactory;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;

@TypeCapability(
        inputs={
                "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData"})

public class GrafWriter
extends JCasFileWriter_ImplBase
{
	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		IRenderer renderer = null;
		try (OutputStream docOS = getOutputStream(aJCas, ".xml");) {
			// Convert CAS
			GraphFactory grafFactory = new GraphFactory();
			IGraph graph = grafFactory.createGraph(aJCas.getCas());

			// Write CAS
			renderer = new GrafRenderer(docOS);
			renderer.render(graph);
		}
		catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
		finally {
			if (renderer != null) {
				renderer.close();
			}
		}
	}
}
