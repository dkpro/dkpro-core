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
package de.tudarmstadt.ukp.dkpro.core.testing;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.testing.factory.TokenBuilder;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TestRunner
{
    /**
     * Run an analysis engine using a document. The document is automatically split into tokens and
     * sentenced based on spaces and dots. Make sure the dots are surrounded by spaces.
     * 
     * @see TokenBuilder
     */
    public static JCas runTest(AnalysisEngineDescription aEngine, String aLanguage, String aDocument)
        throws UIMAException
    {
        return runTest(createEngine(aEngine), aLanguage, aDocument);
    }

    /**
     * Run an analysis engine using a document. The document is automatically split into tokens and
     * sentenced based on spaces and dots. Make sure the dots are surrounded by spaces.
     * 
     * @see TokenBuilder
     */
    public static JCas runTest(String aDocumentId, AnalysisEngineDescription aEngine,
            String aLanguage, String aDocument)
        throws UIMAException
    {
        return runTest(aDocumentId, createEngine(aEngine), aLanguage, aDocument);
    }

    /**
     * Run an analysis engine using a document. The document is automatically split into tokens and
     * sentenced based on spaces and dots. Make sure the dots are surrounded by spaces.
     * 
     * @see TokenBuilder
     */
    public static JCas runTest(AnalysisEngine aEngine, String aLanguage, String aDocument)
        throws UIMAException
    {
        return runTest(null, aEngine, aLanguage, aDocument);
    }

    /**
     * Run an analysis engine using a document. The document is automatically split into tokens and
     * sentenced based on spaces and dots. Make sure the dots are surrounded by spaces.
     * 
     * @see TokenBuilder
     */
    public static JCas runTest(String aDocumentId, AnalysisEngine aEngine, String aLanguage,
            String aDocument)
        throws UIMAException
    {
        JCas jcas = aEngine.newJCas();

        if (aDocumentId != null) {
            DocumentMetaData meta = DocumentMetaData.create(jcas);
            meta.setDocumentId(aDocumentId);
        }

        jcas.setDocumentLanguage(aLanguage);

        TokenBuilder<Token, Sentence> tb = new TokenBuilder<Token, Sentence>(Token.class,
                Sentence.class);
        tb.buildTokens(jcas, aDocument);


        aEngine.process(jcas);
        
        return jcas;
    }
}
