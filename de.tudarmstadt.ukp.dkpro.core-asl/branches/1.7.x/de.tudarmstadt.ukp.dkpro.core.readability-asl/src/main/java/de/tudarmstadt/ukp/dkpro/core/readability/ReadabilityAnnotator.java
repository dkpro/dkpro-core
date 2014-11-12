/*******************************************************************************
 * Copyright 2013
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
package de.tudarmstadt.ukp.dkpro.core.readability;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.readability.measure.ReadabilityMeasures;
import de.tudarmstadt.ukp.dkpro.core.readability.measure.ReadabilityMeasures.Measures;
import de.tudarmstadt.ukp.dkpro.core.type.ReadabilityScore;

/**
 * Assign a set of popular readability scores to the text.
 *
 * @author zesch, zhu
 */
public class ReadabilityAnnotator 
    extends JCasAnnotator_ImplBase
{
    
    private ReadabilityMeasures readability;
    
    @Override
    public void initialize(UimaContext context)
        throws ResourceInitializationException
    {
        super.initialize(context);

        this.readability = new ReadabilityMeasures();
    }

    
    @Override
    public void process(JCas jcas)
        throws AnalysisEngineProcessException
    {
        
        if (jcas.getDocumentLanguage() != null) {
            readability.setLanguage(jcas.getDocumentLanguage());
        }
        
        int nrofSentences = 0;
        List<String> words = new ArrayList<String>();
        
        for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
            nrofSentences++;
            
            List<Token> tokens = JCasUtil.selectCovered(Token.class, sentence);

            for (Token token : tokens) {
                words.add(token.getCoveredText());
            }
        }
        
        for (Measures measure : Measures.values()) {
            ReadabilityScore score = new ReadabilityScore(jcas);
            score.setMeasureName(measure.name());
            score.setScore(this.readability.getReadabilityScore(measure, words, nrofSentences));
            score.addToIndexes();
        }
    }
}