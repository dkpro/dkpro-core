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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.*;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import de.tudarmstadt.ukp.dkpro.core.toolbox.core.Sentence;

public class Segmenter
{

    private final AnalysisEngineDescription segmenter;

    public Segmenter()
        throws Exception
    {
        segmenter = createEngineDescription(BreakIteratorSegmenter.class);
    }

    public Collection<String> tokenize(String text, String language)
        throws Exception
    {
        AnalysisEngine engine = createEngine(segmenter);
        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage(language);
        jcas.setDocumentText(text);
        engine.process(jcas);

        return CasUtil.toText(select(jcas, Token.class));
    }

    public Collection<Sentence> sentenceSplit(String text, String language)
        throws Exception
    {
        List<Sentence> sentences = new ArrayList<Sentence>();

        AnalysisEngine engine = createEngine(segmenter);
        JCas jcas = engine.newJCas();
        jcas.setDocumentLanguage(language);
        jcas.setDocumentText(text);
        engine.process(jcas);

        for (de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence s : select(jcas,
                de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence.class)) {
            List<String> tokens = CasUtil.toText(selectCovered(jcas, Token.class, s));
            sentences.add(new Sentence(tokens));
        }

        return sentences;
    }
}