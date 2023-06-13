/*
 * Licensed to the Technische Universität Darmstadt under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Technische Universität Darmstadt 
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dkpro.core.udpipe;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.resources.PlatformDetector;
import org.dkpro.core.testing.AssertAnnotations;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class UDPipeSegmenterTest
{
    @BeforeEach
    public void prepare()
    {
        PlatformDetector pd = new PlatformDetector();
        Assumptions.assumeTrue(asList("linux-x86_32", "linux-x86_64",
                "osx-x86_64", "windows-x86_32", "windows-x86_64").contains(pd.getPlatformId()), "Unsupported platform");
    }
  
    @Test
    public void testNorwegian()
        throws Exception
    {
        runTest("no", null, "Storbritannia drøyer ikke. Storbritannia starter den formelle prosessen for utmelding av EU 29. mars, opplyser statsminister Theresa Mays kontor.",
                new String[] { 
                        "Storbritannia drøyer ikke.",
                        "Storbritannia starter den formelle prosessen for utmelding av EU 29. "
                        + "mars, opplyser statsminister Theresa Mays kontor." },
                new String[] { "Storbritannia", "drøyer", "ikke", ".", "Storbritannia", "starter",
                        "den", "formelle", "prosessen", "for", "utmelding", "av", "EU", "29.",
                        "mars", ",", "opplyser", "statsminister", "Theresa", "Mays", "kontor",
                        "." });

    }
    
    @Test
    public void testEnglish()
        throws Exception
    {
        runTest("en", null,
                "Good morning Mr. President. I would love to welcome you to S.H.I.E.L.D. 2.0.",
                new String[] { "Good morning Mr. President.",
                        "I would love to welcome you to S.H.I.E.L.D. 2.0." },
                new String[] { "Good", "morning", "Mr.", "President", ".", "I", "would", "love",
                        "to", "welcome", "you", "to", "S.H.I.E.L.D.", "2.0", "." });

    }
        
    private void runTest(String language, String aVariant, String testDocument, String[] sExpected,
            String[] tExpected)
        throws Exception
    {
        String variant = aVariant != null ? aVariant : "ud";
        AnalysisEngine engine = createEngine(UDPipeSegmenter.class,
                UDPipeSegmenter.PARAM_VARIANT, variant);
        
        JCas jcas = JCasFactory.createJCas();
        jcas.setDocumentLanguage(language);
        jcas.setDocumentText(testDocument);

        engine.processAndOutputNewCASes(jcas);

        AssertAnnotations.assertSentence(sExpected, select(jcas, Sentence.class));
        AssertAnnotations.assertToken(tExpected, select(jcas, Token.class));
        
    }
}
