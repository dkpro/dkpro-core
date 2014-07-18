/**
 * Copyright 2007-2014
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

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.sentiment.type.StanfordSentimentAnnotation;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.component.CasDumpWriter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.util.CasCreationUtils;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Test for {@link de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSentimentAnalyzer}
 */
public class StanfordSentimentAnalyzerTest {

	@Test
	public void testSentiment() throws Exception {
		CAS cas = CasCreationUtils.createCas(TypeSystemDescriptionFactory.createTypeSystemDescription(), null, null);
		cas.setDocumentLanguage("en");
		cas.setDocumentText("I feel very very bad.");
		Sentence s = new Sentence(cas.getJCas(), 0, cas.getDocumentText().length());
		s.addToIndexes();

		SimplePipeline.runPipeline(cas,
				AnalysisEngineFactory.createEngineDescription(StanfordSentimentAnalyzer.class),
				AnalysisEngineFactory.createEngineDescription(CasDumpWriter.class)
		);

		StanfordSentimentAnnotation sentimentAnnotation = JCasUtil.select(cas.getJCas(),
				StanfordSentimentAnnotation.class).iterator().next();

		// more negative than positive
		assertTrue(sentimentAnnotation.getNegative() + sentimentAnnotation.getVeryNegative()
				> sentimentAnnotation.getPositive() + sentimentAnnotation.getVeryPositive());
	}
}