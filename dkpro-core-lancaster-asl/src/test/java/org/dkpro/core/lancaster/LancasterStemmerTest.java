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
package org.dkpro.core.lancaster;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpPosTagger;
import de.tudarmstadt.ukp.dkpro.core.testing.AssertAnnotations;
import de.tudarmstadt.ukp.dkpro.core.testing.DkproTestContext;
import de.tudarmstadt.ukp.dkpro.core.testing.TestRunner;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.junit.Rule;
import org.junit.Test;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

public class LancasterStemmerTest
{
    @Test
    public void testEnglish()
        throws Exception
    {
        runTest("en", "computers Computers deliberately", 
                new String[] {"comput", "comput", "delib"} );
        
        runTest("en", "We need a very complicated example sentence , which " +
                "contains as many constituents and dependencies as possible .",
                new String[] { "we", "need", "a", "very", "comply", "exampl", "sent", "",
                "which", "contain", "as", "many", "constitu", "and", "depend", "as", "poss",
                "" });
    }

    @Test
    public void testEnglishCaseInsensitive()
        throws Exception
    {
        runTest("en", "EDUCATIONAL Educational educational", 
                new String[] {"educ", "educ", "educ"});
    }

    @Test
    public void testEnglishCaseFiltered()
        throws Exception
    {
        String[] stems = { "educ" };
        String[] pos = { "NNS", "JJ", "NN", "NNS" };
        
        AnalysisEngineDescription aggregate = createEngineDescription(
                createEngineDescription(OpenNlpPosTagger.class),
                createEngineDescription(LancasterStemmer.class,
                        LancasterStemmer.PARAM_FILTER_FEATUREPATH, "pos/PosValue",
                        LancasterStemmer.PARAM_FILTER_CONDITION_OPERATOR, "EQUALS",
                        LancasterStemmer.PARAM_FILTER_CONDITION_VALUE, "JJ"));
        
        JCas result = TestRunner.runTest(aggregate, "en", "Babies educational sleep .s");

        AssertAnnotations.assertStem(stems, select(result, Stem.class));
        AssertAnnotations.assertPOS(null, pos, select(result, POS.class));
    }

    private JCas runTest(String aLanguage, String aText, String[] aStems, Object... aParams)
        throws Exception
    {
        JCas result = TestRunner.runTest(createEngineDescription(LancasterStemmer.class, aParams),
                aLanguage, aText);

        AssertAnnotations.assertStem(aStems, select(result, Stem.class));
        
        return result;
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
