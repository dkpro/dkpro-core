/**
 * Copyright 2013
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.tudarmstadt.ukp.dkpro.core.stanfordnlp;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.toText;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.coref.type.CoreferenceChain;

/**
 * @author Richard Eckart de Castilho
 */
public class StanfordCoreferenceResolverTest
{
	@Test
	public void test()
		throws Exception
	{
		// Coreference resolution requires the parser and the NER to run before
		AnalysisEngine aggregate = createEngine(createEngineDescription(
				createEngineDescription(StanfordSegmenter.class),
				createEngineDescription(StanfordParser.class,
						StanfordParser.PARAM_WRITE_CONSTITUENT, true,
						StanfordParser.PARAM_WRITE_DEPENDENCY, true,
						StanfordParser.PARAM_WRITE_PENN_TREE, true,
						StanfordParser.PARAM_WRITE_POS, true),
				createEngineDescription(
						StanfordNamedEntityRecognizer.class),
				createEngineDescription(StanfordCoreferenceResolver.class)));

		// Set up a simple example
		JCas jcas = aggregate.newJCas();
		jcas.setDocumentLanguage("en");
		jcas.setDocumentText("John bought a car. He is very happy with it.");
		aggregate.process(jcas);

		// Dump results
		List<CoreferenceChain> chains = new ArrayList<CoreferenceChain>(select(jcas, CoreferenceChain.class));
		for (CoreferenceChain chain : chains) {
			System.out.println("Chain: [" + StringUtils.join(toText(chain.links()), "] , [") + "]");
		}

		// Checks
		assertEquals(2, chains.size());
		assertEquals(asList("John", "He"), toText(chains.get(0).links()));
		assertEquals(asList("a car", "it"), toText(chains.get(1).links()));
	}
}
