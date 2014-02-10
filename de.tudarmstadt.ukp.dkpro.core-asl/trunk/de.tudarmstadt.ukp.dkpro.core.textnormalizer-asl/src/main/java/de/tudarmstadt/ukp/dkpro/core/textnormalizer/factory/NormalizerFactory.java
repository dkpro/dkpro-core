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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.castransformation.ApplyChangesAnnotator;
import de.tudarmstadt.ukp.dkpro.core.jazzy.JazzyChecker;
import de.tudarmstadt.ukp.dkpro.core.textnormalizer.ReplacementFileNormalizer;
import de.tudarmstadt.ukp.dkpro.core.textnormalizer.ReplacementFileNormalizer.SrcSurroundings;
import de.tudarmstadt.ukp.dkpro.core.textnormalizer.ReplacementFileNormalizer.TargetSurroundings;
import de.tudarmstadt.ukp.dkpro.core.textnormalizer.frequency.CapitalizationNormalizer;
import de.tudarmstadt.ukp.dkpro.core.textnormalizer.frequency.ExpressiveLengtheningNormalizer;
import de.tudarmstadt.ukp.dkpro.core.textnormalizer.frequency.SharpSNormalizer;
import de.tudarmstadt.ukp.dkpro.core.textnormalizer.SpellCheckerNormalizer;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;

public class NormalizerFactory 
{
    int view_counter = 0;
    private final static String INITIAL_VIEW = "_InitialView";

    public AnalysisEngineDescription getSpellcorrection(String filepath) throws ResourceInitializationException 
    {
	AggregateBuilder ab = new AggregateBuilder();
	ab.add(createEngineDescription(BreakIteratorSegmenter.class), INITIAL_VIEW, getSourceView());
	ab.add(createEngineDescription(JazzyChecker.class, JazzyChecker.PARAM_MODEL_LOCATION, filepath), INITIAL_VIEW, getSourceView());
	ab.add(createEngineDescription(SpellCheckerNormalizer.class), INITIAL_VIEW, getSourceView());
	ab.add(createEngineDescription(ApplyChangesAnnotator.class), "source", getSourceView(), "target", getTargetView());	
	AnalysisEngineDescription aed = ab.createAggregateDescription();
	aed.setAnnotatorImplementationName("Spell");

	return aed;
    }

    public AnalysisEngineDescription getUmlautSharpSNormalization(ExternalResourceDescription frequencyProvider, int minFrequency) throws ResourceInitializationException 
    {
	AggregateBuilder ab = new AggregateBuilder();
	ab.add(createEngineDescription(BreakIteratorSegmenter.class), INITIAL_VIEW, getSourceView());
	ab.add(createEngineDescription(	SharpSNormalizer.class,
	        SharpSNormalizer.FREQUENCY_PROVIDER, frequencyProvider,
	        SharpSNormalizer.PARAM_MIN_FREQUENCY_THRESHOLD, minFrequency)
	        , INITIAL_VIEW, getSourceView());
	ab.add(createEngineDescription(ApplyChangesAnnotator.class), "source", getSourceView(), "target", getTargetView());
	AnalysisEngineDescription aed = ab.createAggregateDescription();
	aed.setAnnotatorImplementationName("Umlaute");

	return aed;
    }

    public AnalysisEngineDescription getReplacementNormalization(String filepath, SrcSurroundings src, TargetSurroundings target) throws ResourceInitializationException 
    {
	AggregateBuilder ab = new AggregateBuilder();
	ab.add(createEngineDescription(
		ReplacementFileNormalizer.class, 
		ReplacementFileNormalizer.PARAM_REPLACE_LOCATION, filepath,
		ReplacementFileNormalizer.PARAM_SRC_SURROUNDINGS, src,
		ReplacementFileNormalizer.PARAM_TARGET_SURROUNDINGS, target), INITIAL_VIEW, getSourceView());
	ab.add(createEngineDescription(ApplyChangesAnnotator.class), "source", getSourceView(), "target", getTargetView());	
	AnalysisEngineDescription aed = ab.createAggregateDescription();
//	aed.setAnnotatorImplementationName(new File(filepath).getName().split("\\")[0]);

	return aed;	
    }

    public AnalysisEngineDescription getExpressiveLengtheningNormalization(ExternalResourceDescription frequencyProvider) throws ResourceInitializationException 
    {
	AggregateBuilder ab = new AggregateBuilder();
	ab.add(createEngineDescription(BreakIteratorSegmenter.class), INITIAL_VIEW, getSourceView());
	ab.add(createEngineDescription(ExpressiveLengtheningNormalizer.class,ExpressiveLengtheningNormalizer.FREQUENCY_PROVIDER, frequencyProvider), INITIAL_VIEW, getSourceView());
	ab.add(createEngineDescription(ApplyChangesAnnotator.class), "source", getSourceView(), "target", getTargetView());
	AnalysisEngineDescription aed = ab.createAggregateDescription();
	aed.setAnnotatorImplementationName("Lengthening");

	return aed;
    }

    public AnalysisEngineDescription getCapitalizationNormalization(ExternalResourceDescription frequencyProvider) throws ResourceInitializationException 
    {
	AggregateBuilder ab = new AggregateBuilder();
	ab.add(createEngineDescription(BreakIteratorSegmenter.class), INITIAL_VIEW, getSourceView());
	ab.add(createEngineDescription(CapitalizationNormalizer.class, CapitalizationNormalizer.FREQUENCY_PROVIDER, frequencyProvider), INITIAL_VIEW, getSourceView());
	ab.add(createEngineDescription(ApplyChangesAnnotator.class), "source", getSourceView(), "target", getTargetView());
	AnalysisEngineDescription aed = ab.createAggregateDescription();
	aed.setAnnotatorImplementationName("Capitalization");

	return aed;
    }


    private String getSourceView() 
    {
	return (view_counter > 0) ? "view" + view_counter : INITIAL_VIEW;
    }

    private String getTargetView() 
    {
	return "view" + ++view_counter;
    }


}
