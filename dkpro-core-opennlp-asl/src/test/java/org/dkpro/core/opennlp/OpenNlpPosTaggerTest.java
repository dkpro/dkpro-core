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
package org.dkpro.core.opennlp;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.pipeline.SimplePipeline.runPipeline;
import static org.apache.uima.fit.util.JCasUtil.select;
import static org.dkpro.core.testing.AssertAnnotations.assertPOS;

import java.io.File;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.resources.ResourceObjectProviderBase;
import org.dkpro.core.testing.AssertAnnotations;
import org.dkpro.core.testing.AssumeResource;
import org.dkpro.core.testing.TestRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class OpenNlpPosTaggerTest
{
    @Test
    public void simpleExample()
        throws Exception
    {
        // NOTE: This file contains Asciidoc markers for partial inclusion of this file in the 
        // documentation. Do not remove these tags!
        // tag::example[]
        JCas jcas = JCasFactory.createText("This is a test", "en");
        
        runPipeline(jcas,
                createEngineDescription(OpenNlpSegmenter.class),
                createEngineDescription(OpenNlpPosTagger.class));
        
        for (Token t : select(jcas, Token.class)) {
            System.out.printf("%s %s%n", t.getCoveredText(), t.getPos().getPosValue());
        }
        // end::example[]
        
        assertPOS(
                new String[] { "POS_DET", "POS_VERB", "POS_DET", "POS_NOUN" }, 
                new String[] { "DT", "VBZ", "DT", "NN" },
                select(jcas, POS.class));
    }
    
    @Test
    public void testEnglishAutoLoad(@TempDir File testOutput)
        throws Exception
    {
        String oldModelCache = System.setProperty(ResourceObjectProviderBase.PROP_REPO_CACHE, 
                new File(testOutput, "models").getPath());
        String oldOfflineMode = System.setProperty(ResourceObjectProviderBase.PROP_REPO_OFFLINE, 
                ResourceObjectProviderBase.FORCE_AUTO_LOAD);
        
        try {
            TestRunner.autoloadModelsOnNextTestRun();
            runTest("en", null, "This is a test .",
                    new String[] { "DT",   "VBZ", "DT",  "NN",   "." },
                    new String[] { "POS_DET", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" });
        }
        finally {
            if (oldModelCache != null) {
                System.setProperty(ResourceObjectProviderBase.PROP_REPO_CACHE, oldModelCache);
            }
            else {
                System.getProperties().remove(ResourceObjectProviderBase.PROP_REPO_CACHE);
            }
            if (oldOfflineMode != null) {
                System.setProperty(ResourceObjectProviderBase.PROP_REPO_OFFLINE, oldOfflineMode);
            }
            else {
                System.getProperties().remove(ResourceObjectProviderBase.PROP_REPO_OFFLINE);
            }
        }
    }

    @Test
    public void testEnglishManualURI(@TempDir File testOutput)
        throws Exception
    {
        String oldModelCache = System.setProperty(ResourceObjectProviderBase.PROP_REPO_CACHE, 
                new File(testOutput, "models").getPath());
        String oldOfflineMode = System.setProperty(ResourceObjectProviderBase.PROP_REPO_OFFLINE, 
                ResourceObjectProviderBase.FORCE_AUTO_LOAD);
        
        try {
            TestRunner.autoloadModelsOnNextTestRun();
            
            String[] tagClasses = { "POS_DET", "POS_VERB", "POS_DET", "POS_NOUN", "POS_PUNCT" };
            String[] tags = { "DT",   "VBZ", "DT",  "NN",   "." };
            
            AnalysisEngine engine = createEngine(OpenNlpPosTagger.class,
                    OpenNlpPosTagger.PARAM_MODEL_ARTIFACT_URI, "mvn:de.tudarmstadt.ukp.dkpro.core:de.tudarmstadt.ukp.dkpro.core.opennlp-model-tagger-en-maxent:20120616.1",
                    OpenNlpPosTagger.PARAM_VARIANT, "maxent",
                    OpenNlpPosTagger.PARAM_PRINT_TAGSET, true);

            JCas jcas = TestRunner.runTest(engine, "en", "This is a test .");

            AssertAnnotations.assertPOS(tagClasses, tags, select(jcas, POS.class));
        }
        finally {
            if (oldModelCache != null) {
                System.setProperty(ResourceObjectProviderBase.PROP_REPO_CACHE, oldModelCache);
            }
            else {
                System.getProperties().remove(ResourceObjectProviderBase.PROP_REPO_CACHE);
            }
            if (oldOfflineMode != null) {
                System.setProperty(ResourceObjectProviderBase.PROP_REPO_OFFLINE, oldOfflineMode);
            }
            else {
                System.getProperties().remove(ResourceObjectProviderBase.PROP_REPO_OFFLINE);
            }
        }
    }

    private JCas runTest(String language, String variant, String testDocument, String[] tags,
            String[] tagClasses)
        throws Exception
    {
        AssumeResource.assumeResource(OpenNlpPosTagger.class, "tagger", language, variant);

        AnalysisEngine engine = createEngine(OpenNlpPosTagger.class,
                OpenNlpPosTagger.PARAM_VARIANT, variant,
                OpenNlpPosTagger.PARAM_PRINT_TAGSET, true);

        JCas jcas = TestRunner.runTest(engine, language, testDocument);

        AssertAnnotations.assertPOS(tagClasses, tags, select(jcas, POS.class));
        
        return jcas;
    }
}
