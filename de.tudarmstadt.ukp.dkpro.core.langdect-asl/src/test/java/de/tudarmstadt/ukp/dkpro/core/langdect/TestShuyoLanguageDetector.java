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
package de.tudarmstadt.ukp.dkpro.core.langdect;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.junit.Assert.*;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class TestShuyoLanguageDetector
{

    @Test
    public void testLanguageDetectionEnglish() throws Exception
    {
        JCas cas = runTest("This is sentence in any language");
        DocumentMetaData metaData = JCasUtil.selectSingle(cas, DocumentMetaData.class);
        
        assertEquals("en", metaData.getLanguage());
    }
    
    @Test
    public void testLanguageDetectionGerman() throws Exception
    {
        JCas cas = runTest("Zehn zottige Ziegen ziehen zehn Zentner Zucker zum Zoo");
        DocumentMetaData metaData = JCasUtil.selectSingle(cas, DocumentMetaData.class);
        
        assertEquals("de", metaData.getLanguage());
    }
    
    @Test
    public void testLanguageDetectionJapanese() throws Exception
    {
        JCas cas = runTest("やまない雨はない");
        DocumentMetaData metaData = JCasUtil.selectSingle(cas, DocumentMetaData.class);
        
        assertEquals("ja", metaData.getLanguage());
    }

    private JCas runTest(String text)
        throws Exception
    {
        AnalysisEngine engine = createEngine(LanguageDetectorShuyo.class
                );

        JCas aJCas = TestRunner.runTest(engine, "en", text);

        return aJCas;
    }
}
