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
package de.tudarmstadt.ukp.dkpro.core.tokit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;
import static org.uimafit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;

public class ParagraphSplitterTest
{
	@Test
	public void paragraphSplitterTest_SingleLineBreaks()
		throws Exception
	{
		StringBuilder sb = new StringBuilder();
		sb.append("paragraph1");
		sb.append(System.getProperty("line.separator"));
		sb.append(System.getProperty("line.separator"));
		sb.append("paragraph2");
		sb.append(System.getProperty("line.separator"));
		sb.append("paragraph3");

		AnalysisEngine ae = createPrimitive(ParagraphSplitter.class,
				ParagraphSplitter.PARAM_SPLIT_PATTERN, ParagraphSplitter.SINGLE_LINE_BREAKS_PATTERN);

		JCas jcas = ae.newJCas();
		jcas.setDocumentLanguage("en");
		jcas.setDocumentText(sb.toString());
		ae.process(jcas);

		int i = 0;
		for (Paragraph paragraph : select(jcas, Paragraph.class)) {
			if (i == 0) {
				assertEquals("paragraph1", paragraph.getCoveredText());
			}
			else if (i == 1) {
				assertEquals("paragraph2", paragraph.getCoveredText());
			}
			else if (i == 2) {
				assertEquals("paragraph3", paragraph.getCoveredText());
			}
			else {
				fail("too many paragraphs");
			}
			i++;
		}
	}

	@Test
	public void paragraphSplitterTest_DoubleLineBreaks()
		throws Exception
	{
		StringBuilder sb = new StringBuilder();
		sb.append("paragraph1");
		sb.append(System.getProperty("line.separator"));
		sb.append(System.getProperty("line.separator"));
		sb.append("paragraph2");

		AnalysisEngine ae = createPrimitive(ParagraphSplitter.class);

		JCas jcas = ae.newJCas();
		jcas.setDocumentLanguage("en");
		jcas.setDocumentText(sb.toString());
		ae.process(jcas);

		int i = 0;
		for (Paragraph paragraph : select(jcas, Paragraph.class)) {
			if (i == 0) {
				assertEquals("paragraph1", paragraph.getCoveredText());
			}
			else if (i == 1) {
				assertEquals("paragraph2", paragraph.getCoveredText());
			}
			else {
				fail("too many paragraphs");
			}
			i++;
		}
	}
}
