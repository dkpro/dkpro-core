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
package org.dkpro.core.testing;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngine;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.testing.factory.TokenBuilder;
import org.apache.uima.fit.util.LifeCycleUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.core.api.resources.ResourceObjectProviderBase;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

public class TestRunner
{
    /**
     * Run an analysis engine using a document. The document is automatically split into tokens
     * based on spaces and into sentences based on newline. Make sure the punctuation is surrounded
     * by spaces!
     * 
     * @param aEngine
     *            an analysis engine description.
     * @param aLanguage
     *            a language code.
     * @param aDocument
     *            the text to process.
     * @return a JCas.
     * @throws UIMAException
     *             if an exception occurs.
     * @see TokenBuilder
     */
    public static JCas runTest(AnalysisEngineDescription aEngine, String aLanguage,
            String aDocument)
        throws UIMAException
    {
        return runTest(createEngine(aEngine), aLanguage, aDocument);
    }

    /**
     * Run an analysis engine using a document. The document is automatically split into tokens
     * based on spaces and into sentences based on newline. Make sure the punctuation is surrounded
     * by spaces!
     * 
     * @param aDocumentId
     *            a document ID.
     * @param aEngine
     *            an analysis engine description.
     * @param aLanguage
     *            a language code.
     * @param aDocument
     *            the text to process.
     * @return a JCas.
     * @throws UIMAException
     *             if an exception occurs.
     * @see TokenBuilder
     */
    public static JCas runTest(String aDocumentId, AnalysisEngineDescription aEngine,
            String aLanguage, String aDocument)
        throws UIMAException
    {
        var engine = createEngine(aEngine);
        try {
            return runTest(aDocumentId, engine, aLanguage, aDocument);
        }
        finally {
            if (engine != null) {
                LifeCycleUtil.collectionProcessComplete(engine);
                LifeCycleUtil.destroy(engine);
            }
        }
    }

    /**
     * Run an analysis engine using a document. The document is automatically split into tokens
     * based on spaces and into sentences based on newline. Make sure the punctuation is surrounded
     * by spaces!
     * 
     * @param aEngine
     *            an analysis engine description.
     * @param aLanguage
     *            a language code.
     * @param aDocument
     *            the text to process.
     * @return a JCas.
     * @throws UIMAException
     *             if an exception occurs.
     * @see TokenBuilder
     */
    public static JCas runTest(AnalysisEngine aEngine, String aLanguage, String aDocument)
        throws UIMAException
    {
        return runTest(null, aEngine, aLanguage, aDocument);
    }

    /**
     * Run an analysis engine using a document. The document is automatically split into tokens
     * based on spaces and into sentences based on newline. Make sure the punctuation is surrounded
     * by spaces!
     * 
     * @param aDocumentId
     *            a document ID.
     * @param aEngine
     *            an analysis engine description.
     * @param aLanguage
     *            a language code.
     * @param aDocument
     *            the text to process.
     * @return a JCas.
     * @throws UIMAException
     *             if an exception occurs.
     * @see TokenBuilder
     */
    public static JCas runTest(String aDocumentId, AnalysisEngine aEngine, String aLanguage,
            String aDocument)
        throws UIMAException
    {
        // No automatic downloading from repository during testing. This makes sure we fail if
        // models are not properly added as test dependencies.
        if (offline) {
            System.setProperty(ResourceObjectProviderBase.PROP_REPO_OFFLINE, "true");
        }
        offline = true;

        JCas jcas = aEngine.newJCas();

        if (aDocumentId != null) {
            DocumentMetaData meta = DocumentMetaData.create(jcas);
            meta.setDocumentId(aDocumentId);
        }

        jcas.setDocumentLanguage(aLanguage);

        TokenBuilder<Token, Sentence> tb = new TokenBuilder<Token, Sentence>(Token.class,
                Sentence.class);
        tb.buildTokens(jcas, aDocument);

        aEngine.process(jcas);

        // DkproTestContext context = DkproTestContext.get();
        // if (context != null) {
        // File folder = new File("target/test-output/" + context.getTestOutputFolderName());
        // if (!folder.exists()) {
        // FileUtils.deleteQuietly(folder);
        // }
        // folder.mkdirs();
        //
        // try (OutputStream docOS = new FileOutputStream(new File(folder, "output.xmi"))) {
        // XmiCasSerializer.serialize(jcas.getCas(), null, docOS, true, null);
        // }
        // catch (Exception e) {
        // throw new AnalysisEngineProcessException(e);
        // }
        // }

        AssertAnnotations.assertValid(jcas);

        return jcas;
    }

    private static boolean offline = true;

    public static void autoloadModelsOnNextTestRun()
    {
        offline = false;
    }
}
