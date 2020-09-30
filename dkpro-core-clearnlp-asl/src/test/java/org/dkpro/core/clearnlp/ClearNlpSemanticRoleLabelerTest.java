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
package org.dkpro.core.clearnlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.opennlp.OpenNlpPosTagger;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.TestRunner;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.semantics.type.SemPred;

public class ClearNlpSemanticRoleLabelerTest
{
    static final String documentEnglish = "We need a very complicated example sentence , which "
            + "contains as many constituents and dependencies as possible .";

    @Test
    public void testEnglish()
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 3000000000l);

        JCas jcas = runTest("en", null, documentEnglish);

        String[] predicates = {
                "contains (contain.01): [(A0:sentence)(A1:as)(R-A0:which)]",
                "need (need.01): [(A0:We)(A1:sentence)]" };

        AssertAnnotations.assertSemPred(predicates, select(jcas, SemPred.class));
    }

    @Test
    public void testEnglishExpand()
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 3000000000l);

        JCas jcas = runTest("en", null, documentEnglish,
                ClearNlpSemanticRoleLabeler.PARAM_EXPAND_ARGUMENTS, true);

        String[] predicates = {
                "contains (contain.01): ["
                + "(A0:a very complicated example sentence , which contains as many constituents and dependencies as possible)"
                + "(A1:as many constituents and dependencies as possible)"
                + "(R-A0:which)]",
                "need (need.01): ["
                + "(A0:We)"
                + "(A1:a very complicated example sentence , which contains as many constituents and dependencies as possible)]"
        };

        AssertAnnotations.assertSemPred(predicates, select(jcas, SemPred.class));
    }
    
    @Test
    public void testEnglishExpand2()
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 3000000000l);

        JCas jcas = runTest("en", null, "The man was sued by Jacqueline Kennedy Onassis .",
                ClearNlpSemanticRoleLabeler.PARAM_EXPAND_ARGUMENTS, true);

        String[] predicates = { "sued (sue.01): [(A0:by Jacqueline Kennedy Onassis)(A1:The man)]" };

        AssertAnnotations.assertSemPred(predicates, select(jcas, SemPred.class));
    }
    @Test
    public void testEnglishMayo()
        throws Exception
    {
        Assume.assumeTrue(Runtime.getRuntime().maxMemory() > 3000000000l);

        JCas jcas = runTest("en", "mayo", documentEnglish);

        String[] predicates = {
                "contains (contain.01): [(A0:sentence)(A1:as)(R-A0:which)]",
                "need (need.01): [(A0:We)(A1:sentence)]" };

        AssertAnnotations.assertSemPred(predicates, select(jcas, SemPred.class));
    }

    private JCas runTest(String aLanguage, String aVariant, String aText, Object... aExtraParams)
        throws Exception
    {
        Object[] params = new Object[] {
                ClearNlpParser.PARAM_VARIANT, aVariant,
                ClearNlpParser.PARAM_PRINT_TAGSET, true};
        params = ArrayUtils.addAll(params, aExtraParams);
        
        AnalysisEngineDescription engine = createEngineDescription(
                createEngineDescription(OpenNlpPosTagger.class),
                createEngineDescription(ClearNlpLemmatizer.class),
                createEngineDescription(ClearNlpParser.class),
                createEngineDescription(ClearNlpSemanticRoleLabeler.class, params));

        return TestRunner.runTest(engine, aLanguage, aText);
    }


    @Rule
    public DkproTestContext testContext = new DkproTestContext();

    @Before
    public void freeMemory()
    {
        Runtime.getRuntime().gc();
        Runtime.getRuntime().gc();
        Runtime.getRuntime().gc();
    }
}
