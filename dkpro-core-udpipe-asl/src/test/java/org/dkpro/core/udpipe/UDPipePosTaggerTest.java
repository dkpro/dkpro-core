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
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.resources.PlatformDetector;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.AssumeResource;
import org.dkpro.core.testing.TestRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;

public class UDPipePosTaggerTest
{
    @BeforeEach
    public void prepare()
    {
        PlatformDetector pd = new PlatformDetector();
        assumeTrue(asList("linux-x86_32", "linux-x86_64", "osx-x86_64", "windows-x86_32",
                "windows-x86_64").contains(pd.getPlatformId()), "Unsupported platform");
    }

    @Test
    public void testNorwegian() throws Exception
    {
        runTest("no", null,
                "Magnus Carlsen trengte bare de fire partiene med lynsjakk for å slå utfordreren Sergej Karjakin.",
                new String[] { "PROPN", "PROPN", "VERB", "ADV", "DET", "NUM", "NOUN", "ADP", "NOUN",
                        "ADP", "PART", "VERB", "NOUN", "PROPN", "PROPN" },
                new String[] { "POS_PROPN", "POS_PROPN", "POS_VERB", "POS_ADV", "POS_DET",
                        "POS_NUM", "POS_NOUN", "POS_ADP", "POS_NOUN", "POS_ADP", "POS_PART",
                        "POS_VERB", "POS_NOUN", "POS_PROPN", "POS_PROPN" });
    }

    @Test
    public void testEnglish() throws Exception
    {
        runTest("en", null, "This is a test .", new String[] { "DT", "VBZ", "DT", "NN", "." },
                new String[] { "POS_DET", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" });

        runTest("en", null, "A neural net .", new String[] { "DT", "JJ", "NN", "." },
                new String[] { "POS_DET", "POS_ADJ", "POS_NOUN", "POS_PUNCT" });

        runTest("en", null, "John is purchasing oranges .",
                new String[] { "NNP", "VBZ", "VBG", "NNS", "." },
                new String[] { "POS_PROPN", "POS_VERB", "POS_VERB", "POS_NOUN", "POS_PUNCT" });
    }

    private JCas runTest(String language, String aVariant, String testDocument, String[] tags,
            String[] tagClasses)
        throws Exception
    {
        String variant = aVariant != null ? aVariant : "ud";
        AssumeResource.assumeResource(UDPipePosTagger.class, "tagger", language, variant);

        AnalysisEngine engine = createEngine(UDPipePosTagger.class, UDPipePosTagger.PARAM_VARIANT,
                variant);

        JCas jcas = TestRunner.runTest(engine, language, testDocument);

        AssertAnnotations.assertPOS(tagClasses, tags, select(jcas, POS.class));

        return jcas;
    }
}
