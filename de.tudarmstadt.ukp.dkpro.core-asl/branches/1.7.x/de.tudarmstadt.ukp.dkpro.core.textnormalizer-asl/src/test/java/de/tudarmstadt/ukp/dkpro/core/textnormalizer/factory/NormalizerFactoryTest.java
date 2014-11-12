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
package de.tudarmstadt.ukp.dkpro.core.textnormalizer.factory;

import static org.apache.uima.fit.factory.ExternalResourceFactory.createExternalResourceDescription;
import static org.junit.Assert.assertEquals;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ExternalResourceDescription;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.frequency.resources.Web1TFrequencyCountResource;
import de.tudarmstadt.ukp.dkpro.core.textnormalizer.ReplacementFileNormalizer.SrcSurroundings;
import de.tudarmstadt.ukp.dkpro.core.textnormalizer.ReplacementFileNormalizer.TargetSurroundings;

public class NormalizerFactoryTest
{

    private ExternalResourceDescription frequencyProvider;

    @Before
    public void init(){
        frequencyProvider = createExternalResourceDescription(
                Web1TFrequencyCountResource.class,
                Web1TFrequencyCountResource.PARAM_MIN_NGRAM_LEVEL, "1",
                Web1TFrequencyCountResource.PARAM_MAX_NGRAM_LEVEL, "1",
                Web1TFrequencyCountResource.PARAM_INDEX_PATH, "src/test/resources/jweb1t");  
    }

    @Test
    public void testNormalizerFactory() throws Exception
    {
	test(
		"GMBH +++ Gewerkschaftb +++ HDL +++ :-)", 
		"GmbH +++ Gewerkschaft +++ Hab' dich lieb +++  lächeln "
		);
    }    


    public void test(String input, String output) throws Exception
    {
	NormalizerFactory nf = new NormalizerFactory();	
	AnalysisEngineDescription normalizeSharpSUmlaute 	= nf.getUmlautSharpSNormalization(frequencyProvider,0);
	AnalysisEngineDescription normalizeRepitions 		= nf.getExpressiveLengtheningNormalization(frequencyProvider);
	AnalysisEngineDescription normalizeCapitalization 	= nf.getCapitalizationNormalization(frequencyProvider);
	AnalysisEngineDescription normalizeInternetslang 	= nf.getReplacementNormalization("src/main/resources/replaceLists/internetslang.txt", SrcSurroundings.ONLY_ALPHANIMERIC, TargetSurroundings.NOTHING);
	AnalysisEngineDescription normalizeSpelling 		= nf.getSpellcorrection("src/test/resources/dictionary/ngerman");
	AnalysisEngineDescription normalizeEmoticons 		= nf.getReplacementNormalization("src/main/resources/replaceLists/emoticons_de.txt", SrcSurroundings.IRRELEVANT, TargetSurroundings.WHITESPACE);

	AggregateBuilder ab = new AggregateBuilder();	
	ab.add(normalizeSharpSUmlaute);
	ab.add(normalizeRepitions);
	ab.add(normalizeCapitalization);
	ab.add(normalizeInternetslang);
	ab.add(normalizeSpelling);
	ab.add(normalizeEmoticons);

	AnalysisEngine engine = ab.createAggregate();
	JCas jcas = engine.newJCas();
	jcas.setDocumentText(input);	
	DocumentMetaData.create(jcas);
	engine.process(jcas);

	JCas view0 = jcas.getView("_InitialView");
	JCas view1 = jcas.getView("view1");
	JCas view2 = jcas.getView("view2");
	JCas view3 = jcas.getView("view3");
	JCas view4 = jcas.getView("view4");
	JCas view5 = jcas.getView("view5");
	JCas view6 = jcas.getView("view6");

	System.out.println("Original       :" + view0.getDocumentText());	
	System.out.println("Umlaute        :" + view1.getDocumentText());
	System.out.println("Repitions      :" + view2.getDocumentText());
	System.out.println("Capitalization :" + view3.getDocumentText());
	System.out.println("Internetslang  :" + view4.getDocumentText());
	System.out.println("Spelling       :" + view5.getDocumentText());
	System.out.println("Emoticons      :" + view6.getDocumentText());
	System.out.println("Perfect        :" + output);

	assertEquals(output, view6.getDocumentText());


    }

}
