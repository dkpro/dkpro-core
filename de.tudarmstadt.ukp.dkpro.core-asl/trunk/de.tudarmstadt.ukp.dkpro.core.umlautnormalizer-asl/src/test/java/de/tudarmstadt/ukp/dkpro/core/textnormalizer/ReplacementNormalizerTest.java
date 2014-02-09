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
package de.tudarmstadt.ukp.dkpro.core.textnormalizer;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.junit.Assert.assertEquals;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.castransformation.ApplyChangesAnnotator;
import de.tudarmstadt.ukp.dkpro.core.textnormalizer.ReplacementNormalizer.SrcSurroundings;
import de.tudarmstadt.ukp.dkpro.core.textnormalizer.ReplacementNormalizer.TargetSurroundings;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class ReplacementNormalizerTest 
{
    @Test
    public void testReplacementNormalizer() throws Exception
    {
	testEmoticonReplacement(":-):-("," lächeln  traurig ");
//	testInternetslangReplacement("AKA hdl AKA.", "Also known as Hab' dich lieb Also known as.");
    }

    public void testEmoticonReplacement(String input, String output) throws Exception 
    {
	AnalysisEngineDescription replace = createEngineDescription(
		ReplacementNormalizer.class,
		ReplacementNormalizer.PARAM_REPLACE_LOCATION, "src/main/resources/replaceLists/emoticons_de.txt",
		ReplacementNormalizer.PARAM_TARGET_SURROUNDINGS, TargetSurroundings.whitespace);

	AggregateBuilder ab = new AggregateBuilder();
	ab.add(replace);
	ab.add(createEngineDescription(ApplyChangesAnnotator.class),"source", "_InitialView", "target", "view1");

	AnalysisEngine engine = ab.createAggregate();
	JCas jcas = engine.newJCas();
	jcas.setDocumentText(input);	
	DocumentMetaData.create(jcas);
	engine.process(jcas);

	JCas view0 = jcas.getView("_InitialView");
	JCas view1 = jcas.getView("view1");

	System.out.println(view0.getDocumentText());
	System.out.println(view1.getDocumentText());

	assertEquals(output, view1.getDocumentText());
    }

    public void testInternetslangReplacement(String input, String output) throws Exception
    {
	AnalysisEngineDescription replace = createEngineDescription(
		ReplacementNormalizer.class,
		ReplacementNormalizer.PARAM_REPLACE_LOCATION, "src/main/resources/replaceLists/internetslang.txt",
		ReplacementNormalizer.PARAM_SRC_SURROUNDINGS, SrcSurroundings.anythingBesideAlphanumeric);

	AggregateBuilder ab = new AggregateBuilder();
	ab.add(createEngineDescription(BreakIteratorSegmenter.class), "_InitialView", "_InitialView");
	ab.add(replace);
	ab.add(createEngineDescription(	ApplyChangesAnnotator.class), 
		"source", "_InitialView", "target", "view1");

	AnalysisEngine engine = ab.createAggregate();
	JCas jcas = engine.newJCas();
	jcas.setDocumentText(input);	
	DocumentMetaData.create(jcas);
	engine.process(jcas);

	JCas view0 = jcas.getView("_InitialView");
	JCas view1 = jcas.getView("view1");

	System.out.println(view0.getDocumentText());
	System.out.println(view1.getDocumentText());

	assertEquals(output, view1.getDocumentText());
    }
}
