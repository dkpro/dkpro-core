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
package de.tudarmstadt.ukp.dkpro.core.textmarker;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitive;
import static org.apache.uima.fit.factory.TypeSystemDescriptionFactory.createTypeSystemDescription;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.component.xwriter.CASDumpWriter;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TextMarkerTest
{
	@Test
	public void test() throws Exception
	{
		AnalysisEngine tm = createPrimitive(TextMarker.class,
				// Specify type system explicitly because there is no signature which accepts
				// only priorities
				createTypeSystemDescription(),
				// Load the type priorities needed by TextMarker, uimaFIT doesn't support automatic
				// priority detection yet
				TextMarker.getTypePriorities(),
				// Load script in "Java" notation, with "." as package separator and no extension.
				// File needs to be located in the path specified below with ending ".tm".
				TextMarker.MAIN_SCRIPT, "textmarker.TokensToSentence",
				// Path(s) where the scripts are located
				TextMarker.SCRIPT_PATHS, new String[] { "src/test/resources" } );

		// Create a CAS from the AE so it has the required type priorities
		JCas jcas = tm.newJCas();

		// Fill the CAS with some tokens
		JCasBuilder builder = new JCasBuilder(jcas);
		builder.add("This", Token.class);
		builder.add(" ");
		builder.add("is", Token.class);
		builder.add(" ");
		builder.add("a", Token.class);
		builder.add(" ");
		builder.add("test", Token.class);
		builder.add(".", Token.class);
		builder.close();

		// Apply the script
		tm.process(jcas);

		AnalysisEngine dumper = createPrimitive(CASDumpWriter.class);
		dumper.process(jcas);
	}
}
