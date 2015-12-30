/*******************************************************************************
 * Copyright 2014
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
package de.tudarmstadt.ukp.dkpro.core.cogroo;

import java.util.Locale;

import org.cogroo.analyzer.Analyzer;
import org.cogroo.analyzer.ComponentFactory;
import org.cogroo.text.Document;
import org.cogroo.text.impl.DocumentImpl;
import org.junit.Test;

public class SimpleTest
{
    @Test
    public void lala()
    {
        ComponentFactory factory = ComponentFactory.create(Locale.forLanguageTag("pt-BR"));
        Analyzer sentenceDetector = factory.createSentenceDetector();
        Analyzer tokenizer = factory.createTokenizer();
        Analyzer nameFinder = factory.createNameFinder();
        Analyzer contractionFinder = factory.createContractionFinder();
        Analyzer posTagger = factory.createPOSTagger();
        Analyzer featurizer = factory.createFeaturizer();
        Analyzer lemmatizer = factory.createLemmatizer();
        Analyzer chunker = factory.createChunker();
        Analyzer headFinder = factory.createHeadFinder();
        Analyzer shallowParser = factory.createShallowParser();
        
        Document doc = new DocumentImpl();
        doc.setText("Este é um test. Queria saber mais.");
        
        sentenceDetector.analyze(doc);;
        tokenizer.analyze(doc);
        nameFinder.analyze(doc);
        contractionFinder.analyze(doc);
        posTagger.analyze(doc);
        lemmatizer.analyze(doc);
        featurizer.analyze(doc);
        chunker.analyze(doc);
        headFinder.analyze(doc);
        shallowParser.analyze(doc);
        
        System.out.println(doc.getSentences());
    }
}
