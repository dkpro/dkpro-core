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
import org.apache.uima.cas.CAS;
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
    private int view_counter = 0;

    public AnalysisEngineDescription getSpellcorrection(String aModelLocation)
        throws ResourceInitializationException
    {
    	AggregateBuilder ab = new AggregateBuilder();
    	ab.add(createEngineDescription(BreakIteratorSegmenter.class), 
    	        CAS.NAME_DEFAULT_SOFA, getSourceView());
    	ab.add(createEngineDescription(JazzyChecker.class, 
    	        JazzyChecker.PARAM_MODEL_LOCATION, aModelLocation), 
    	        CAS.NAME_DEFAULT_SOFA, getSourceView());
    	ab.add(createEngineDescription(SpellCheckerNormalizer.class), 
    	        CAS.NAME_DEFAULT_SOFA, getSourceView());
    	ab.add(createEngineDescription(ApplyChangesAnnotator.class), 
    	        ApplyChangesAnnotator.VIEW_SOURCE, getSourceView(), 
    	        ApplyChangesAnnotator.VIEW_TARGET, getTargetView());	
    	AnalysisEngineDescription aed = ab.createAggregateDescription();
    	aed.setAnnotatorImplementationName("Spell");
    
    	return aed;
    }

    public AnalysisEngineDescription getUmlautSharpSNormalization(
            ExternalResourceDescription aFrequencyProvider, int aMinFrequency)
        throws ResourceInitializationException
    {
    	AggregateBuilder ab = new AggregateBuilder();
    	ab.add(createEngineDescription(BreakIteratorSegmenter.class), 
    	        CAS.NAME_DEFAULT_SOFA, getSourceView());
    	ab.add(createEngineDescription(	SharpSNormalizer.class,
    	        SharpSNormalizer.FREQUENCY_PROVIDER, aFrequencyProvider,
    	        SharpSNormalizer.PARAM_MIN_FREQUENCY_THRESHOLD, aMinFrequency), 
    	        CAS.NAME_DEFAULT_SOFA, getSourceView());
    	ab.add(createEngineDescription(ApplyChangesAnnotator.class), 
                ApplyChangesAnnotator.VIEW_SOURCE, getSourceView(), 
                ApplyChangesAnnotator.VIEW_TARGET, getTargetView());    
    	AnalysisEngineDescription aed = ab.createAggregateDescription();
    	aed.setAnnotatorImplementationName("Umlaute");
    
    	return aed;
    }

    public AnalysisEngineDescription getReplacementNormalization(String aModelLocation,
            SrcSurroundings aSrc, TargetSurroundings aTarget)
        throws ResourceInitializationException
    {
    	AggregateBuilder ab = new AggregateBuilder();
    	ab.add(createEngineDescription(
    		ReplacementFileNormalizer.class, 
    		ReplacementFileNormalizer.PARAM_MODEL_LOCATION, aModelLocation,
    		ReplacementFileNormalizer.PARAM_SRC_SURROUNDINGS, aSrc,
    		ReplacementFileNormalizer.PARAM_TARGET_SURROUNDINGS, aTarget), 
    		CAS.NAME_DEFAULT_SOFA, getSourceView());
    	ab.add(createEngineDescription(ApplyChangesAnnotator.class), 
                ApplyChangesAnnotator.VIEW_SOURCE, getSourceView(), 
                ApplyChangesAnnotator.VIEW_TARGET, getTargetView());    
    	AnalysisEngineDescription aed = ab.createAggregateDescription();
    //	aed.setAnnotatorImplementationName(new File(filepath).getName().split("\\")[0]);
    
    	return aed;	
    }

    public AnalysisEngineDescription getExpressiveLengtheningNormalization(
            ExternalResourceDescription aFrequencyProvider)
        throws ResourceInitializationException
    {
    	AggregateBuilder ab = new AggregateBuilder();
    	ab.add(createEngineDescription(BreakIteratorSegmenter.class), 
    	        CAS.NAME_DEFAULT_SOFA, getSourceView());
    	ab.add(createEngineDescription(ExpressiveLengtheningNormalizer.class,
    	        ExpressiveLengtheningNormalizer.FREQUENCY_PROVIDER, aFrequencyProvider), 
    	        CAS.NAME_DEFAULT_SOFA, getSourceView());
    	ab.add(createEngineDescription(ApplyChangesAnnotator.class), 
                ApplyChangesAnnotator.VIEW_SOURCE, getSourceView(), 
                ApplyChangesAnnotator.VIEW_TARGET, getTargetView());    
    	AnalysisEngineDescription aed = ab.createAggregateDescription();
    	aed.setAnnotatorImplementationName("Lengthening");
    
    	return aed;
    }

    public AnalysisEngineDescription getCapitalizationNormalization(
            ExternalResourceDescription aFrequencyProvider)
        throws ResourceInitializationException
    {
    	AggregateBuilder ab = new AggregateBuilder();
    	ab.add(createEngineDescription(BreakIteratorSegmenter.class), 
    	        CAS.NAME_DEFAULT_SOFA, getSourceView());
    	ab.add(createEngineDescription(CapitalizationNormalizer.class, 
    	        CapitalizationNormalizer.FREQUENCY_PROVIDER, aFrequencyProvider), 
    	        CAS.NAME_DEFAULT_SOFA, getSourceView());
    	ab.add(createEngineDescription(ApplyChangesAnnotator.class), 
                ApplyChangesAnnotator.VIEW_SOURCE, getSourceView(), 
                ApplyChangesAnnotator.VIEW_TARGET, getTargetView());    
    	AnalysisEngineDescription aed = ab.createAggregateDescription();
    	aed.setAnnotatorImplementationName("Capitalization");
    
    	return aed;
    }

    protected String getSourceView()
    {
        return (view_counter > 0) ? "view" + view_counter : CAS.NAME_DEFAULT_SOFA;
    }

    protected String getTargetView()
    {
        return "view" + ++view_counter;
    }

    public String getOutputView()
    {
        return "view" + view_counter;
    }
}
