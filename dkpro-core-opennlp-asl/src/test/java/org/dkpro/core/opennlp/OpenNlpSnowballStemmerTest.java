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
package org.dkpro.core.opennlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.dkpro.core.testing.AssertAnnotations.assertPOS;
import static org.dkpro.core.testing.AssertAnnotations.assertStem;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.TestRunner;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem;

public class OpenNlpSnowballStemmerTest
{
    @Test
    public void testGerman() throws Exception
    {
        runTest("de", "Automobile Fenster", new String[] { "Automobil", "Fenst" });
    }

    @Test
    public void testEnglish() throws Exception
    {
        runTest("en", "computers Computers deliberately",
                new String[] { "comput", "Comput", "deliber" });

        runTest("en",
                "We need a very complicated example sentence , which "
                        + "contains as many constituents and dependencies as possible .",
                new String[] { "We", "need", "a", "veri", "complic", "exampl", "sentenc", ",",
                        "which", "contain", "as", "mani", "constitu", "and", "depend", "as",
                        "possibl", "." });
    }

    @Test
    public void testLanguageParameterOverridesDocumentLanguage() throws Exception
    {
        // Document language is English, but the German stemmer must be used
        runTest("en", "Automobile Fenster", new String[] { "Automobil", "Fenst" },
                OpenNlpSnowballStemmer.PARAM_LANGUAGE, "de");
    }

    @Test
    public void testEnglishCaseInsensitive() throws Exception
    {
        runTest("en", "EDUCATIONAL Educational educational",
                new String[] { "educ", "educ", "educ" }, OpenNlpSnowballStemmer.PARAM_LOWER_CASE,
                true);
    }

    @Test
    public void testEnglishCaseSensitive() throws Exception
    {
        runTest("en", "EDUCATIONAL Educational educational",
                new String[] { "EDUCATIONAL", "Educat", "educ" },
                OpenNlpSnowballStemmer.PARAM_LOWER_CASE, false);
    }

    @Test
    public void testEnglishCaseFiltered() throws Exception
    {
        String[] stems = { "educ" };
        String[] pos = { "NNS", "JJ", "NN", "NNS" };

        AnalysisEngineDescription aggregate = createEngineDescription(
                createEngineDescription(OpenNlpPosTagger.class),
                createEngineDescription(OpenNlpSnowballStemmer.class,
                        OpenNlpSnowballStemmer.PARAM_LOWER_CASE, true,
                        OpenNlpSnowballStemmer.PARAM_FILTER_FEATUREPATH, "pos/PosValue",
                        OpenNlpSnowballStemmer.PARAM_FILTER_CONDITION_OPERATOR, "EQUALS",
                        OpenNlpSnowballStemmer.PARAM_FILTER_CONDITION_VALUE, "JJ"));

        var result = TestRunner.runTest(aggregate, "en", "Babies educational sleep .s");

        assertStem(stems, select(result, Stem.class));
        assertPOS(null, pos, select(result, POS.class));
    }

    private JCas runTest(String aLanguage, String aText, String[] aStems, Object... aParams)
        throws Exception
    {
        var result = TestRunner.runTest(
                createEngineDescription(OpenNlpSnowballStemmer.class, aParams), aLanguage, aText);

        assertStem(aStems, select(result, Stem.class));

        return result;
    }
}
