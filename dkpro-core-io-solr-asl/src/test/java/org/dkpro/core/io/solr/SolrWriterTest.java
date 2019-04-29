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
package org.dkpro.core.io.solr;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.io.solr.SolrWriter;
import org.dkpro.core.io.text.StringReader;
import org.junit.Test;

/**
 * Test(s) for {@link SolrWriter}.
 *
 *
 */
public class SolrWriterTest
{
    /**
     * Try to initialize with a non-responding Solr server.
     *
     * @throws UIMAException
     * @throws IOException
     */
    @Test(expected = ResourceInitializationException.class)
    public void testFailInitServer()
        throws UIMAException, IOException
    {
        String text = "text";
        String solrUrl = "http://noSolrServerHere:8983/solr";

        CollectionReaderDescription reader = createReaderDescription(StringReader.class,
                StringReader.PARAM_DOCUMENT_TEXT, text,
                StringReader.PARAM_LANGUAGE, "en");

        AnalysisEngineDescription writer = createEngineDescription(SolrWriter.class,
                SolrWriter.PARAM_TARGET_LOCATION, solrUrl);

        SimplePipeline.runPipeline(reader, writer);
    }

}
