/*
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
 */

package org.dkpro.core.textnormalizer.annotations;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.TestRunner;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TrailingCharacterRemoverTest
{
    @Test
    public void basicTest()
        throws UIMAException
    {
        String inputText = "Ein Text)( mitA Fehlern( . und9-9 a komischen Tokens.";
        String[] tokensExpected = { "Ein", "Text", "mit", "Fehlern", ".", "und", "a", "komischen",
                "Tokens." };

        AnalysisEngineDescription engine = createEngineDescription(TrailingCharacterRemover.class);
        JCas jcas = TestRunner.runTest(engine, "de", inputText);
        
        AssertAnnotations.assertToken(tokensExpected, select(jcas, Token.class));
    }

    @Test
    public void minTokenLengthTest()
        throws UIMAException
    {
        int minimumTokenLength = 3;
        String inputText = "Ein T-- mit komischen) To. a";
        String[] tokensExpected = { "Ein", "mit", "komischen", "To.", "a" };

        AnalysisEngineDescription engine = createEngineDescription(TrailingCharacterRemover.class,
                TrailingCharacterRemover.PARAM_MIN_TOKEN_LENGTH, minimumTokenLength);
        JCas jcas = TestRunner.runTest(engine, "de", inputText);
        
        AssertAnnotations.assertToken(tokensExpected, select(jcas, Token.class));
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
