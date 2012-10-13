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
package de.tudarmstadt.ukp.dkpro.core.castransformation.alignment;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Test;
import org.uimafit.factory.AggregateBuilder;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.castransformation.ApplyChangesAnnotator;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class NormalizerTest {

	@Test
	public void test() throws Exception {

		AggregateBuilder builder = new AggregateBuilder();
		builder.add(createPrimitiveDescription(BreakIteratorSegmenter.class));
		builder.add(createPrimitiveDescription(UmlautNormalizer.class));
		builder.add(
				createPrimitiveDescription(ApplyChangesAnnotator.class),
				"source", "_InitialView",
				"target", "umlaut_cleaned"
		);

		AnalysisEngine engine = builder.createAggregate();

		String text = "Buechsenoeffner koennen oefter benuetzt werden. Neuerscheinungen muss der kaeufer kaufen. Schon zum Fruehstueck traf er auf den Maerchenerzaehler, den Uebergeek und den Ueberraschungeioeffner. Sein Oeuvre ist beeindruckend.";
		JCas jcas = engine.newJCas();
		jcas.setDocumentText(text);
		DocumentMetaData.create(jcas);

		engine.process(jcas);

		JCas view = jcas.getView("umlaut_cleaned");
		System.out.println(view.getDocumentText());
	}
}
