/*
 * Copyright 2017
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
package de.tudarmstadt.ukp.dkpro.core.langdetect;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.junit.Assert.assertEquals;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.langdetect.LangDetectLanguageIdentifier;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class LangDetectLanguageIdentifierTest
{
    @Test
    public void testLanguageDetectionEnglish()
        throws Exception
    {
        JCas cas = runTest("This is sentence in any language");

        assertEquals("en", cas.getDocumentLanguage());
    }

    @Test
    public void testLanguageDetectionGerman()
        throws Exception
    {
        JCas cas = runTest("Zehn zottige Ziegen ziehen zehn Zentner Zucker zum Zoo");

        assertEquals("de", cas.getDocumentLanguage());
    }

    @Test
    public void testLanguageDetectionJapanese()
        throws Exception
    {
        JCas cas = runTest("やまない雨はない");

        assertEquals("ja", cas.getDocumentLanguage());
    }

    private JCas runTest(String text)
        throws Exception
    {
        AnalysisEngine engine = createEngine(LangDetectLanguageIdentifier.class);

        JCas aJCas = TestRunner.runTest(engine, "en", text);

        return aJCas;
    }
    
    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
