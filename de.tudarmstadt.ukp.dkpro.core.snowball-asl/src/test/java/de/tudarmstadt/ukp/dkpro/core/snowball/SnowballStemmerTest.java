/*******************************************************************************
 * Copyright 2010
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
package de.tudarmstadt.ukp.dkpro.core.snowball;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;

public class SnowballStemmerTest
{
    @Test
    public void testGerman()
        throws Exception
    {
        runTest("de", "Automobile Fenster", 
                new String[] {"Automobil", "Fenst"} );
    }

    @Test
    public void testEnglish()
        throws Exception
    {
        runTest("en", "computers Computers deliberately", 
                new String[] {"comput", "Comput", "deliber"} );
        
        runTest("en", "We need a very complicated example sentence , which " +
                "contains as many constituents and dependencies as possible .",
                new String[] { "We", "need", "a", "veri", "complic", "exampl", "sentenc", ",", 
                "which", "contain", "as", "mani", "constitu", "and", "depend", "as", "possibl", 
                "." });
    }

    @Test
    public void testEnglishCaseInsensitive()
        throws Exception
    {
        runTest("en", "EDUCATIONAL Educational educational", 
                new String[] {"educ", "educ", "educ"},
                SnowballStemmer.PARAM_LOWER_CASE, true);
    }

    @Test
    public void testEnglishCaseSensitive()
        throws Exception
    {
        runTest("en", "EDUCATIONAL Educational educational", 
                new String[] {"EDUCATIONAL", "Educat", "educ"},
                SnowballStemmer.PARAM_LOWER_CASE, false);
    }

    private JCas runTest(String aLanguage, String aText, String[] aStems, Object... aParams)
        throws Exception
    {
        JCas result = TestRunner.runTest(createEngineDescription(SnowballStemmer.class, aParams),
                aLanguage, aText);

        AssertAnnotations.assertStem(aStems, select(result, Stem.class));
        
        return result;
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void printSeparator()
    {
        System.out.println("\n=== " + name.getMethodName() + " =====================");
    }
}
