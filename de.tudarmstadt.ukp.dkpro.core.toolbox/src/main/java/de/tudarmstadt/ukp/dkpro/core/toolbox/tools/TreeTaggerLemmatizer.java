/*******************************************************************************
 * Copyright 2011
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
package de.tudarmstadt.ukp.dkpro.core.toolbox.tools;

import static org.uimafit.factory.AnalysisEngineFactory.createAggregate;
import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.util.JCasUtil.select;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.core.treetagger.TreeTaggerPosLemmaTT4J;

public class TreeTaggerLemmatizer
{
    private final AnalysisEngine tagger;
    private final JCas jcas;
    
    public TreeTaggerLemmatizer()
        throws Exception
    {
        tagger = createAggregate(createAggregateDescription(
                createPrimitiveDescription(BreakIteratorSegmenter.class),
                createPrimitiveDescription(TreeTaggerPosLemmaTT4J.class)));
        
        jcas = tagger.newJCas();
    }
    
    public Collection<String> lemmatize(String text, String language)
        throws Exception
    {
        List<String> lemmas = new ArrayList<String>();

        jcas.reset();
        jcas.setDocumentLanguage(language);
        jcas.setDocumentText(text);
        tagger.process(jcas);

        for (Lemma l : select(jcas, Lemma.class)) {
            lemmas.add(l.getValue());
        }

        return lemmas;
    }
}