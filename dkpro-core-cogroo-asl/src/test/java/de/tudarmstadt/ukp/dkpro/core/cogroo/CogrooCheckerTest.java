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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations.*;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.anomaly.type.GrammarAnomaly;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class CogrooCheckerTest
{
    @Test
    public void test()
        throws Exception
    {
        JCas jcas = runTest("pt",
                "Fomos levados à crer que os menino são burro de doer. As menina chegaram.");
        
        String[] anomalies = {
                    "[ 14, 15] GrammarAnomaly (Não acontece crase antes de verbo.)",
                    "[ 25, 34] GrammarAnomaly (Os artigos concordam com o substantivo a que se referem.)",
                    "[ 54, 63] GrammarAnomaly (Os artigos concordam com o substantivo a que se referem.)",
                    "[ 64, 72] GrammarAnomaly (Verificou-se erro de concordância entre o sujeito e o verbo.)" };
        
        assertAnomaly(anomalies, select(jcas, GrammarAnomaly.class));
    }

    private JCas runTest(String aLanguage, String aText)
        throws UIMAException
    {
        AnalysisEngineDescription checker = createEngineDescription(CogrooChecker.class);

        return TestRunner.runTest(checker, aLanguage, aText);
    }
}
