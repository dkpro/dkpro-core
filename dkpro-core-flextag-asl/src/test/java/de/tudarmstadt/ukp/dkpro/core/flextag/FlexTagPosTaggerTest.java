/**
 * Copyright 2007-2018
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
package de.tudarmstadt.ukp.dkpro.core.flextag;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.util.JCasUtil.select;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.AssumeResource;
import org.dkpro.core.testing.DkproTestContext;
import org.dkpro.core.testing.TestRunner;
import org.junit.Rule;
import org.junit.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;

public class FlexTagPosTaggerTest
{

    @Test
    public void testEnglish()
        throws Exception
    {
        runTest("en", "wsj0-18", "This is a test . \n",
                new String[] { "DT", "VBZ", "DT", "NN", "." },
                new String[] { "POS_DET", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" });

    }

    @Test
    public void testGerman()
        throws Exception
    {
        runTest("de", "tiger", "Das ist ein Test .",
                new String[] { "PDS", "VAFIN", "ART", "NN", "$." },
                new String[] { "POS_PRON", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" });

    }

    private void runTest(String language, String variant, String testDocument, String[] tags,
            String[] tagClasses)
                throws Exception
    {
        AssumeResource.assumeResource(FlexTagPosTagger.class,
                "de/tudarmstadt/ukp/dkpro/core/flextag", "tagger", language, variant);

        JCas aJCas = runTest(language, variant, testDocument);

        AssertAnnotations.assertPOS(tagClasses, tags, select(aJCas, POS.class));
    }

    private JCas runTest(String aLanguage, String aVariant, String aText)
        throws Exception
    {
        AssumeResource.assumeResource(FlexTagPosTagger.class,
                "de/tudarmstadt/ukp/dkpro/core/flextag", "tagger", aLanguage, aVariant);

        AnalysisEngine engine = createEngine(FlexTagPosTagger.class, FlexTagPosTagger.PARAM_VARIANT,
                aVariant, FlexTagPosTagger.PARAM_LANGUAGE, aLanguage);
        return TestRunner.runTest("4711", engine, aLanguage, aText);
    }

    @Rule
    public DkproTestContext testContext = new DkproTestContext();
}
